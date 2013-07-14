package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.content.Context;
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
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        ContentMatcher contentMatcher = ContentMatcher.from(context);
        Uri contentUri = contentMatcher.getContentUri(feedUri);
        mToken = token;
        mFeedUri = feedUri;
        mContentUri = contentUri;
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
            FeedItem item = FeedItemT.fromJSON(data_item);
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

    /**
     * Сохраняет запись из ленты в БД
     * @param item запись ленты
     * @return
     */
    protected ContentValues fillRow(FeedItem item) {
        ContentValues row = new ContentValues();

        row.put(FeedContract.FeedColumns._ID, item.getVideoId());
        row.put(FeedContract.FeedColumns.TITLE, item.getTitle());
        row.put(FeedContract.FeedColumns.DESCRIPTION, item.getDescription());
        row.put(FeedContract.FeedColumns.CREATED, sdf.format(item.getCreated()));
        row.put(FeedContract.FeedColumns.THUMBNAIL_URI, item.getThumbnailUri().toString());

        Author author = item.getAuthor();
        if (author != null) {
            row.put(FeedContract.FeedColumns.AUTHOR_ID, author.getId());
            row.put(FeedContract.FeedColumns.AUTHOR_NAME, author.getName());
            row.put(FeedContract.FeedColumns.AVATAR_URI, author.getAvatarUrl().toString());
        }

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
}
