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

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:07
 * To change this template use File | Settings | File Templates.
 */
public class PlayOptions implements Parcelable {
    public static final String VIDEO_BALANCER = "video_balancer";
    public static final String STREAM_TYPE_M3U8 = "m3u8";
    public static final String STREAM_TYPE_JSON = "json";
    public static final String JSON_TRACK_ID = "id";
    private static final String JSON_ACL_ACCESS = "acl_access";
    private static final String JSON_ALLOWED = "allowed";
    private static final String JSON_ACL_ERRCODE = "err_code";
    private static final String JSON_THUMBNAIL_URL = "thumbnail_url";
    private static final String JSON_RESULTS = "results";
    private static final String LOG_TAG = PlayOptions.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final String JSON_PLAYER_STUB = "player_stub";

    private Uri mBalancerUrl;
    private int mTrackId;
    private boolean mAclAllowed;
    private int mAclErrorCode;
    private Uri mThumbnailUri;
    private int mTrackInfoErrorCode;

    public PlayOptions(Uri balancerUrl, int trackId, boolean aclAllowed, int aclErrorCode,
                       Uri thumbnailUri, int trackInfoErrorCode) {
        mBalancerUrl = balancerUrl;
        mTrackId = trackId;
        mAclAllowed = aclAllowed;
        mAclErrorCode = aclErrorCode;
        mThumbnailUri = thumbnailUri;
        mTrackInfoErrorCode = trackInfoErrorCode;
    }

    public static PlayOptions fromParcel(Parcel parcel) {
        Uri balancerUrl = parcel.readParcelable(Uri.class.getClassLoader());
        int trackId = parcel.readInt();
        boolean[] aclAllowed = new boolean[1];
        parcel.readBooleanArray(aclAllowed);
        int aclErrorCode = parcel.readInt();
        Uri thumbnailUri = parcel.readParcelable(Uri.class.getClassLoader());
        int trackInfoErrorCode = parcel.readInt();
        return new PlayOptions(balancerUrl, trackId, aclAllowed[0], aclErrorCode, thumbnailUri,
                trackInfoErrorCode);
    }

    public static PlayOptions fromJSON(JSONObject data) throws JSONException {
        JSONObject balancer = data.getJSONObject(VIDEO_BALANCER);
        int trackId = data.getInt(JSON_TRACK_ID);
        JSONObject acl = data.optJSONObject(JSON_ACL_ACCESS);
        if (acl == null) {
            acl = new JSONObject();
        }
        boolean allowed = acl.optBoolean(JSON_ALLOWED, false);
        int aclErrorCode = acl.optInt(JSON_ACL_ERRCODE, 0);
        Uri thumbnailUri = Uri.parse(data.getString(JSON_THUMBNAIL_URL));
        JSONObject playerStub = data.optJSONObject(JSON_PLAYER_STUB);
        if (playerStub == null){
            playerStub = new JSONObject();
        }
        int trackInfoErrorCode = playerStub.optInt(JSON_TRACK_ID, 0);
        return new PlayOptions(Uri.parse(balancer.getString(STREAM_TYPE_M3U8)), trackId, allowed,
                aclErrorCode, thumbnailUri, trackInfoErrorCode);
    }

    // Parcelable implementation

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator<PlayOptions> CREATOR
            = new Creator<PlayOptions>() {
        public PlayOptions createFromParcel(Parcel in) {
            return PlayOptions.fromParcel(in);
        }

        public PlayOptions[] newArray(int size) {
            return new PlayOptions[size];
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
        boolean[] allowed = new boolean[1];
        allowed[0] = mAclAllowed;
        parcel.writeBooleanArray(allowed);
        parcel.writeInt(mAclErrorCode);
        parcel.writeParcelable(mThumbnailUri, i);
    }

    public Uri getBalancerUrl() {
        return mBalancerUrl;
    }

    public int getTrackId(){
        return mTrackId;
    }

    public boolean getAclAllowed() { return mAclAllowed; }

    public int getAclErrorCode() { return mAclErrorCode; }

    public int getTrackInfoErrorCode() { return mTrackInfoErrorCode; }

    public Uri getThumbnailUri() {return mThumbnailUri; }

    public JsonObjectRequest getMP4UrlRequest(Context context, RequestListener listener) {
        String balancerUrl = mBalancerUrl.toString().replace(STREAM_TYPE_M3U8, STREAM_TYPE_JSON);
        Uri uri = Uri.parse(balancerUrl).buildUpon()
                .appendQueryParameter("referer", context.getString(R.string.referer))
                .build();
        if (D) Log.d(LOG_TAG, "Balancer Url:" + uri.toString());
        assert uri != null;
        JsonObjectRequest request = new JsonObjectRequest(uri.toString(),
                null, getMP4UrlListener(listener), getErrorListener(Requests.BALANCER_JSON, listener));
        request.setShouldCache(false);
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
                    String[] files = new String[results.length()];
                    for (int i=0; i< results.length(); i++) {
                        files[i] = results.getString(i);
                    }
                    if (D) Log.d(LOG_TAG, "Put balancer urls: " + String.valueOf(files.length));
                    if (results.length() > 0) {
                        bundle.putStringArray(Constants.Result.MP4_URL, files);
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
