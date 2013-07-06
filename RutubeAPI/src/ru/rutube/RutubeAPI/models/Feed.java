package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeAPI;
import ru.rutube.RutubeAPI.requests.RequestListener;

/**
 * Created by tumbler on 22.06.13.
 */
public class Feed<FeedItemT> {
    private static final String LOG_TAG = Feed.class.getName();
    private static final int FEED_RESULT = 0;
    private final RequestListener mRequestListener;
    private final User mUser;
    private final String mFeedUrl;
    private final Context mContext;
    private int mLastPage;
    private int mPerPage;

    public Feed(User user, String feedUrl, Context context, RequestListener listener) {
        this.mUser = user;
        this.mFeedUrl = feedUrl;
        this.mRequestListener = listener;
        this.mContext = context;
        init();
    }

    private void init() {
        this.mLastPage = 0;
        this.mPerPage = 0;
    }

    Response.Listener<JSONObject> getFeedPageListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Bundle bundle = parseFeedPage(response);
                mRequestListener.onResult(FEED_RESULT, bundle);
            } catch (JSONException e) {
                RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                mRequestListener.onRequestError(FEED_RESULT, error);
            }
        }
    };

    protected Bundle parseFeedPage(JSONObject response) throws JSONException {
        return null;
    }

    Response.ErrorListener vollerErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mRequestListener.onVolleyError(error);
        }
    };

    public JsonObjectRequest getFeedRequest(int page) {
        String fullUrl = String.format("%s?page=%d", mFeedUrl, page);
        JsonObjectRequest request = new JsonObjectRequest(fullUrl, null, getFeedPageListener,
                vollerErrorListener);
        request.setShouldCache(true);
        return request;
    }
}
