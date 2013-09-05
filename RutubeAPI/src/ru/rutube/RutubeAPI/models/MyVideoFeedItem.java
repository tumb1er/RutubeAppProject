package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;

/**
 * Created by tumbler on 15.07.13.
 */
public class MyVideoFeedItem extends FeedItem {

    public static final String JSON_IS_HIDDEN = "is_hidden";
    public static final String JSON_VIDEO_URL = "video_url";
    public static final String URI_SIGNATURE = "p";
    private String mSignature;

    public MyVideoFeedItem(String title, String description, Date created, Uri thumbnailUri,
                           String videoId, Author author, String signature) {
        super(title, description, created, thumbnailUri, videoId, author);
        mSignature = signature;
    }

    public MyVideoFeedItem(FeedItem item, String signature) {
        this(item.getTitle(), item.getDescription(), item.getCreated(),
                item.getThumbnailUri(), item.getVideoId(), item.getAuthor(), signature);
    }

    public static MyVideoFeedItem fromParcel(Parcel parcel) {
        FeedItem item = FeedItem.fromParcel(parcel);
        String signature = parcel.readString();
        return new MyVideoFeedItem(item, signature);
    }

    public static MyVideoFeedItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        String signature = null;
        if (data.getBoolean(JSON_IS_HIDDEN)) {
            signature = parseSignature(data);
        }
        return new MyVideoFeedItem(item, signature);
    }

    public static MyVideoFeedItem fromCursor(Cursor c) {
        FeedItem item = FeedItem.fromCursor(c);
        String signature = c.getString(c.getColumnIndex(FeedContract.MyVideo.SIGNATURE));
        return new MyVideoFeedItem(item, signature);
    }

    public Uri getVideoUri(Context context) {
        String url;
        if (mSignature == null){
            url = String.format(RutubeApp.getUrl(R.string.video_page_uri),
                getVideoId());
            return Uri.parse(url);
        } else {
            url = String.format(RutubeApp.getUrl(R.string.video_private_uri),
                    getVideoId());
            return Uri.parse(url).buildUpon().appendQueryParameter(URI_SIGNATURE, mSignature).build();
        }
    }

    @Override
    public void fillRow(ContentValues row) {
        super.fillRow(row);
        row.put(FeedContract.MyVideo.SIGNATURE, mSignature);
    }



    public String getSignature() {
        return mSignature;
    }

    private static String parseSignature(JSONObject data) throws JSONException{
        Uri videoUri = Uri.parse(data.getString(JSON_VIDEO_URL));
        return videoUri.getQueryParameter(URI_SIGNATURE);
    }
}
