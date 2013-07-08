package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeAPI;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.requests.AuthJsonObjectRequest;
import ru.rutube.RutubeAPI.requests.RequestListener;

/**
 * Created by tumbler on 22.06.13.
 */
public class Feed<FeedItemT extends FeedItem> {
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_TAG = Feed.class.getName();
    private static final int FEED_RESULT = 0;
    private final User mUser;
    private final String mFeedUrl;
    private int mLastPage;
    private int mPerPage;
    private Uri mContentUri;

    public Feed(User user, Uri feedUrl, Uri contentUri) {
        mUser = user;
        mFeedUrl = feedUrl.toString();
        mContentUri = contentUri;
        init();
    }

    private void init() {
        this.mLastPage = 0;
        this.mPerPage = 0;
    }

    Response.Listener<JSONObject> getFeedPageListener(final Context context, final RequestListener requestListener)
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Bundle bundle = parseFeedPage(context, response);
                    requestListener.onResult(FEED_RESULT, bundle);
                } catch (JSONException e) {
                    RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                    requestListener.onRequestError(FEED_RESULT, error);
                }
            }
        };
    }

    protected Bundle parseFeedPage(Context context, JSONObject response) throws JSONException {
        Bundle bundle = new Bundle();
        JSONArray data = response.getJSONArray("results");
        int perPage = response.getInt("per_page");
        bundle.putInt(Constants.Result.PER_PAGE, perPage);
        ContentValues[] feedItems = new ContentValues[data.length()];
        for (int i = 0; i < data.length(); ++i) {
            ContentValues row = new ContentValues();
            JSONObject data_item = data.getJSONObject(i);
            FeedItem item = FeedItemT.fromJSON(data_item);
            row.put(FeedContract.FeedColumns._ID, item.getVideoId());
            row.put(FeedContract.FeedColumns.TITLE, item.getTitle());
            row.put(FeedContract.FeedColumns.DESCRIPTION, item.getDescription());
            Log.d(LOG_TAG, "Date: " + sdf.format(item.getCreated()));
            row.put(FeedContract.FeedColumns.CREATED, sdf.format(item.getCreated()));
            row.put(FeedContract.FeedColumns.THUMBNAIL_URI, item.getThumbnailUri().toString());
            Author author = item.getAuthor();
            if (author != null) {
                row.put(FeedContract.FeedColumns.AUTHOR_ID, author.getId());
                row.put(FeedContract.FeedColumns.AUTHOR_NAME, author.getName());
                row.put(FeedContract.FeedColumns.AVATAR_URI, author.getAvatarUrl().toString());
            }
            feedItems[i] = row;
        }
        Log.d(LOG_TAG, "Inserting items: " + String.valueOf(feedItems));
        try {
            context.getContentResolver().bulkInsert(mContentUri, feedItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.getContentResolver().notifyChange(mContentUri, null);
        Log.d(LOG_TAG, "Operation finished");
        return bundle;

    }

    Response.ErrorListener getErrorListener(final RequestListener requestListener)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onVolleyError(error);
            }
        };
    }

    public JsonObjectRequest getFeedRequest(int page, Context context, RequestListener requestListener) {
        String fullUrl = String.format("%s?page=%d", mFeedUrl, page);
        JsonObjectRequest request = new AuthJsonObjectRequest(fullUrl, null,
                getFeedPageListener(context, requestListener),
                getErrorListener(requestListener), mUser.getToken());
        request.setShouldCache(true);
        return request;
    }
}
