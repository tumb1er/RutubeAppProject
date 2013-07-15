package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeAPI;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 16.06.13.
 */
public class Video {
    public final static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final String LOG_TAG = Video.class.getName();
    private static final String JSON_CREATED = "created_ts";
    private static final String JSON_AUTHOR = "author";
    private static final String JSON_TITLE = "title";
    private static final String JSON_DESCIPTION = "description";
    private static final String JSON_THUMBNAIL_URL = "thumbnail_url";
    private static final String JSON_VIDEO_ID = "id";
    private static final String JSON_VIDEO_URL = "video_url";
    public static final String URI_SIGNATURE = "p";
    private String mVideoId;
    private String mTitle;
    private String mDescription;
    private Date mCreated;
    private Uri mThumbnailUri;
    private Author mAuthor;
    private String mSignature;


    public Video(String videoId) {
        this(videoId, null, null, null, null, null, null);
    }

    public Video(String videoId, String signature) {
        this(videoId, signature, null, null, null, null, null);
    }

    protected Video(String videoId, String signature, String title, String description, Date created, Uri thumbnailUri, Author author){
        this.mVideoId = videoId;
        this.mTitle = title;
        this.mDescription = description;
        this.mCreated = created;
        this.mThumbnailUri = thumbnailUri;
        this.mAuthor = author;
        this.mSignature = signature;
    }
    private static Date parseDate(String data) {
        try {
            return dtf.parse(data);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    public static Video fromJSON(JSONObject data) throws JSONException {
        Date created = parseDate(data.getString(JSON_CREATED));
        JSONObject author_json = data.optJSONObject(JSON_AUTHOR);
        Author author = null;
        if (author_json != null)
            author = Author.fromJSON(author_json);
        String title = data.getString(JSON_TITLE);
        String description = data.getString(JSON_DESCIPTION);
        Uri thumbnailUri = Uri.parse(data.getString(JSON_THUMBNAIL_URL));
        String videoId = data.getString(JSON_VIDEO_ID);
        Log.d(FeedItem.class.getName(), "Created item: " + videoId + " " + String.valueOf(created));
        Uri videoUri = Uri.parse(data.getString(JSON_VIDEO_URL));
        return new Video(videoId, videoUri.getQueryParameter(URI_SIGNATURE), title, description,
                created, thumbnailUri, author);
    }

    protected Response.Listener<JSONObject> getTrackInfoListener(final RequestListener requestListener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    TrackInfo ti = parseTrackInfo(response);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.Result.TRACKINFO, ti);
                    requestListener.onResult(Requests.TRACK_INFO, bundle);
                } catch (JSONException e) {
                    RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                    requestListener.onRequestError(Requests.TRACK_INFO, error);
                }
            }
        };
    }

    protected Response.ErrorListener getErrorListener(final RequestListener requestListener){
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onVolleyError(error);
            }
        };
    }

    protected TrackInfo parseTrackInfo(JSONObject data) throws JSONException {
        Log.d(LOG_TAG, "Result: " + data.toString());
        return TrackInfo.fromJSON(data);
    }

    public JsonObjectRequest getTrackInfoRequest(Context context, RequestListener listener) {
        String trackInfoPath = String.format(context.getString(R.string.trackinfo_uri), mVideoId);
        String trackInfoUri = RutubeAPI.getUrl(context, trackInfoPath);
        if (mSignature != null)
            trackInfoUri += String.format("?p=%s", mSignature);
        JsonObjectRequest request = new JsonObjectRequest(trackInfoUri,
                null, getTrackInfoListener(listener), getErrorListener(listener));
        request.setShouldCache(true);
        request.setTag(Requests.TRACK_INFO);
        Log.d(LOG_TAG, "Trackinfo URL: " + trackInfoUri);
        return request;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Date getCreated() {
        return mCreated;
    }

    public Uri getThumbnailUri() {
        return mThumbnailUri;
    }

    public Author getAuthor() {
        return mAuthor;
    }
}
