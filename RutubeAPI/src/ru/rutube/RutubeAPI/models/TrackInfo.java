package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.requests.AuthJsonObjectRequest;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:07
 * To change this template use File | Settings | File Templates.
 */
public class TrackInfo implements Parcelable {
    public static final String VIDEO_BALANCER = "video_balancer";
    public static final String STREAM_TYPE_M3U8 = "m3u8";
    public static final String STREAM_TYPE_JSON = "json";
    public static final String JSON_TRACK_ID = "track_id";
    private static final String JSON_RESULTS = "results";
    private static final String LOG_TAG = TrackInfo.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    private Uri mBalancerUrl;
    private int mTrackId;
    private String mTitle;

    public TrackInfo(Uri balancerUrl, int trackId, String title) {
        this.mBalancerUrl = balancerUrl;
        this.mTrackId = trackId;
        mTitle = title;
    }

    public static TrackInfo fromParcel(Parcel parcel) {
        Uri balancerUrl = parcel.readParcelable(Uri.class.getClassLoader());
        int trackId = parcel.readInt();
        String title = parcel.readString();
        return new TrackInfo(balancerUrl, trackId, title);
    }

    public static TrackInfo fromJSON(JSONObject data) throws JSONException {
        JSONObject balancer = data.getJSONObject(VIDEO_BALANCER);
        int trackId = data.getInt(JSON_TRACK_ID);
        String title = data.getString("title");
        return new TrackInfo(Uri.parse(balancer.getString(STREAM_TYPE_M3U8)), trackId, title);
    }

    // Parcelable implementation

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<TrackInfo> CREATOR
            = new Parcelable.Creator<TrackInfo>() {
        public TrackInfo createFromParcel(Parcel in) {
            return TrackInfo.fromParcel(in);
        }

        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mBalancerUrl, i);
        parcel.writeInt(mTrackId);
    }

    public String getTitle() {
        return mTitle;
    }

    public Uri getBalancerUrl() {
        return mBalancerUrl;
    }

    public int getTrackId(){
        return mTrackId;
    }

    public JsonObjectRequest getMP4UrlRequest(Context context, RequestListener listener) {
        String balancerUrl = mBalancerUrl.toString().replace(STREAM_TYPE_M3U8, STREAM_TYPE_JSON);
        Uri uri = Uri.parse(balancerUrl).buildUpon()
                .appendQueryParameter("referer", context.getString(R.string.referer))
                .build();
        if (D) Log.d(LOG_TAG, "Balancer Url:" + uri.toString());
        assert uri != null;
        JsonObjectRequest request = new JsonObjectRequest(uri.toString(),
                null, getMP4UrlListener(listener), getErrorListener(Requests.BALANCER_JSON, listener));
        request.setShouldCache(true);
        request.setTag(Requests.PLAY_OPTIONS);
        return request;
    }

    private Response.Listener<JSONObject> getMP4UrlListener(final RequestListener listener){
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (D) Log.d(LOG_TAG, "mp4Url response: " + String.valueOf(response));
                try {
                    JSONArray results = response.getJSONArray(JSON_RESULTS);
                    Bundle bundle = new Bundle();
                    if (results.length() > 0) {
                        bundle.putString(Constants.Result.MP4_URL, results.get(0).toString());
                    }
                    listener.onResult(Requests.BALANCER_JSON, bundle);
                } catch (JSONException e) {
                    RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                    listener.onRequestError(Requests.BALANCER_JSON, error);
                }
            }
        };
    }

    private Response.ErrorListener getErrorListener(final int tag, final RequestListener requestListener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (D) Log.d(LOG_TAG, "onErrorResponse");
                NetworkResponse networkResponse = error.networkResponse;
                Bundle bundle = new Bundle();
                if (networkResponse != null) {
                    bundle.putInt(Constants.Result.BALANCER_ERROR, networkResponse.statusCode);
                } else {
                    requestListener.onVolleyError(error);
                }
                requestListener.onResult(tag, bundle);
            }
        };
    }
}
