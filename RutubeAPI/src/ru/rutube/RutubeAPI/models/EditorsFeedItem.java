package ru.rutube.RutubeAPI.models;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by tumbler on 21.07.13.
 */
public class EditorsFeedItem extends FeedItem {
    private static final String JSON_MESSAGE = "message";

    public EditorsFeedItem(String title, String description, Date created, Uri thumbnailUri,
                           String videoId, Author author, int duration) {
        super(title, description, created, thumbnailUri, videoId, author, duration);
    }
    public static EditorsFeedItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        String description = data.getString(JSON_MESSAGE);
        return new EditorsFeedItem(item.getTitle(), description, item.getCreated(),
                item.getThumbnailUri(), item.getVideoId(), item.getAuthor(), item.getDuration());
    }
}
