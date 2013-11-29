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
public class AuthorFeedItem extends FeedItem {

    public static final String JSON_VIDEO_URL = "video_url";

    public AuthorFeedItem(String title, String description, Date created, Uri thumbnailUri,
                          String videoId, Author author, int duration) {
        super(title, description, created, thumbnailUri, videoId, author, duration);
    }

    public AuthorFeedItem(FeedItem item) {
        this(item.getTitle(), item.getDescription(), item.getCreated(), item.getThumbnailUri(),
                item.getVideoId(), item.getAuthor(), item.getDuration());
    }

    public static AuthorFeedItem fromJSON(JSONObject data) throws JSONException {
        return new AuthorFeedItem(FeedItem.fromJSON(data));
    }
}
