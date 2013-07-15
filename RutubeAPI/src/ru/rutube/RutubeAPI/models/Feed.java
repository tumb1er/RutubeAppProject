package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import ru.rutube.RutubeAPI.content.ContentMatcher;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.requests.AuthJsonObjectRequest;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 22.06.13.
 */
public class Feed<FeedItemT extends FeedItem> {
    private static final String LOG_TAG = Feed.class.getName();
    private final String mToken;
    private final Uri mFeedUri;
    private final Uri mContentUri;

    /**
     * Конструктор объекта ленты
     * @param feedUri ссылка на API ленты
     * @param context
     */
    public Feed(Uri feedUri, Context context) {
        String token = User.loadToken(context);
        Uri contentUri = getContentUri(feedUri, context);
        mToken = token;
        mFeedUri = feedUri;
        mContentUri = contentUri;
    }

    private static Uri getContentUri(Uri feedUri, Context context) {
        ContentMatcher contentMatcher = ContentMatcher.from(context);
        return contentMatcher.getContentUri(feedUri);
    }

    /**
     * Конструирует запрос к API ленты
     * @param page номер страницы
     * @param context
     * @param requestListener
     * @return
     */
    public JsonObjectRequest getFeedRequest(int page, Context context, RequestListener requestListener) {
        String fullUrl = String.format("%s?page=%d", mFeedUri, page);
        JsonObjectRequest request = new AuthJsonObjectRequest(fullUrl, null,
                getFeedPageListener(context, requestListener),
                getErrorListener(requestListener), mToken);
        request.setShouldCache(true);
        request.setTag(Requests.FEED_PAGE);
        return request;
    }

    public Uri getContentUri() {
        return mContentUri;
    }

    /**
     * Разбирает ответ API ленты и сохраняет результаты в БД.
     * @param context
     * @param response
     * @return
     * @throws JSONException
     */
    protected Bundle parseFeedPage(Context context, JSONObject response) throws JSONException {
        Bundle bundle = new Bundle();
        int perPage = response.getInt("per_page");
        bundle.putInt(Constants.Result.PER_PAGE, perPage);

        JSONArray data = response.getJSONArray("results");
        ContentValues[] feedItems = new ContentValues[data.length()];
        for (int i = 0; i < data.length(); ++i) {
            JSONObject data_item = data.getJSONObject(i);
            FeedItem item = constructFeedItem(data_item);
            ContentValues row = fillRow(item);
            feedItems[i] = row;
        }
        Log.d(LOG_TAG, "Inserting items: " + Arrays.toString(feedItems));
        try {
            context.getContentResolver().bulkInsert(mContentUri, feedItems);
        } catch (Exception e) {
            // TODO: обработать ошибку вставки в БД.
            e.printStackTrace();
        }
        context.getContentResolver().notifyChange(mContentUri, null);
        Log.d(LOG_TAG, "Operation finished");
        return bundle;

    }

    private FeedItem constructFeedItem(JSONObject data_item) throws JSONException {
        if (mContentUri.equals(FeedContract.MyVideo.CONTENT_URI)) {
            Log.d(LOG_TAG, "Overriding FeedItem class, return MyVideoFeedItem");
            return MyVideoFeedItem.fromJSON(data_item);
        }
        return FeedItem.fromJSON(data_item);
    }

    /**
     * Сохраняет запись из ленты в БД
     * @param item запись ленты
     * @return
     */
    protected ContentValues fillRow(FeedItem item) {
        ContentValues row = new ContentValues();
        item.fillRow(row);
        return row;
    }

    /**
     * Конструирует прокси для обработки ответа от API ленты
     * @param context
     * @param requestListener
     * @return
     */
    private Response.Listener<JSONObject> getFeedPageListener(final Context context, final RequestListener requestListener)
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Bundle bundle = parseFeedPage(context, response);
                    requestListener.onResult(Requests.FEED_PAGE, bundle);
                } catch (JSONException e) {
                    RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                    requestListener.onRequestError(Requests.FEED_PAGE, error);
                }
            }
        };
    }

    /**
     * Конструирует прокси для обработки ошибок Volley
     * @param requestListener
     * @return
     */
    private Response.ErrorListener getErrorListener(final RequestListener requestListener)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onVolleyError(error);
            }
        };
    }

    public static FeedItem loadFeedItem(Context context, Cursor c, Uri feedUri) {
        Uri contentUri = getContentUri(feedUri, context);
        if (contentUri.equals(FeedContract.MyVideo.CONTENT_URI))
            return MyVideoFeedItem.fromCursor(c);
        return FeedItem.fromCursor(c);



    }
}
