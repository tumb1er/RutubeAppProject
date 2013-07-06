package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

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
 * Created by tumbler on 16.06.13.
 */
public class Video {
    public static final int VIDEO_RESULT = 1;
    public static final int TRACK_INFO_RESUILT = 2;

    private static final String LOG_TAG = Video.class.getName();
    private Context mContext;
    private RequestListener mRequestListener;
    private String mVideoId;


    public Video(String videoId, Context context, RequestListener listener) {
        this.mVideoId = videoId;
        this.mContext = context;
        this.mRequestListener = listener;
    }

    public Video(String videoId, Context context) {
        this(videoId, context, (RequestListener) context);
    }

    Response.Listener<JSONObject> getTrackInfoListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Trackinfo ti = parseTrackInfo(response);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.Result.TRACKINFO, ti);
                mRequestListener.onResult(TRACK_INFO_RESUILT, bundle);
            } catch (JSONException e) {
                RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                mRequestListener.onRequestError(TRACK_INFO_RESUILT, error);
            }
        }
    };
    Response.ErrorListener vollerErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mRequestListener.onVolleyError(error);
        }
    };

    protected Trackinfo parseTrackInfo(JSONObject data) throws JSONException {
        Log.d(LOG_TAG, "Result: " + data.toString());
        return Trackinfo.fromJSON(data);
    }

    public JsonObjectRequest getTrackInfoRequest() {
        String trackInfoPath = String.format(mContext.getString(R.string.trackinfo_uri), mVideoId);
        String trackInfoUri = RutubeAPI.getUrl(mContext, trackInfoPath);
        JSONObject requestData = new JSONObject();
        JsonObjectRequest request = new JsonObjectRequest(trackInfoUri,
                requestData, getTrackInfoListener, vollerErrorListener);
        request.setShouldCache(true);
        return request;
    }

}
