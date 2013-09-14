package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 04.05.13
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public class TagsFeedItem extends FeedItem {
    protected static final String JSON_TAGS = "all_tags";

    private List<VideoTag> tags;

    public TagsFeedItem(String title, String description, Date created, Uri thumbnailUri,
                        String videoId, Author author, List<VideoTag> tags) {
        super(title, description, created, thumbnailUri, videoId, author);
        this.tags = tags;
    }

    public TagsFeedItem(FeedItem item, List<VideoTag> tags) {
        this(item.getTitle(), item.getDescription(), item.getCreated(),
                item.getThumbnailUri(), item.getVideoId(), item.getAuthor(), tags);
    }

    public static TagsFeedItem fromParcel(Parcel parcel) {
        FeedItem item = FeedItem.fromParcel(parcel);
        ArrayList tags_list = parcel.readArrayList(VideoTag.class.getClassLoader());
        List<VideoTag> tags = (ArrayList<VideoTag>) tags_list;
        return new TagsFeedItem(item, tags);
    }

    public static TagsFeedItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        List<VideoTag> tags = parseTags(data);
        return new TagsFeedItem(item, tags);
    }

    protected static List<VideoTag> parseTags(JSONObject data) throws JSONException {
        JSONObject video = data.getJSONObject(JSON_VIDEO);
        JSONArray tags_json = video.getJSONArray(JSON_TAGS);
        ArrayList<VideoTag> result = new ArrayList<VideoTag>(tags_json.length());
        for (int i=0; i<tags_json.length(); i++) {
            result.set(i, VideoTag.fromJSON(tags_json.getJSONObject(i)));
        }
        return result;
    }

    // Parcelable implementation

    public static final Creator<TagsFeedItem> CREATOR
            = new Creator<TagsFeedItem>() {
        public TagsFeedItem createFromParcel(Parcel in) {
            return TagsFeedItem.fromParcel(in);
        }

        public TagsFeedItem[] newArray(int size) {
            return new TagsFeedItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeArray(tags.toArray());
    }
    public List<VideoTag> getTags() {
        return null;
    }

    public void fillRow(ContentValues row, int position) {
        fillRow(row);
    }

    public static TagsFeedItem fromCursor(Cursor c) {
        FeedItem item = FeedItem.fromCursor(c);
        return new TagsFeedItem(item, null);

    }

    protected void fillRow(ContentValues row) {
        super.fillRow(row);
    }
}
