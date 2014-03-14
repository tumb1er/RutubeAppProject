package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.rutube.RutubeAPI.content.FeedContract;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 04.05.13
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public class TVShowFeedItem extends FeedItem {
    protected static final String JSON_TAGS = "all_tags";
    private static final String JSON_SEASON = "season";
    private static final String JSON_EPISODE = "episode";
    private static final String JSON_TYPE = "type";
    private static final String JSON_TYPE_ID = "id";

    private JSONObject metainfo;
    private int season;
    private int episode;
    private int type;
    private int tvshow_id;

    public TVShowFeedItem(String title, String description, Date created, Uri thumbnailUri,
                          String videoId, Author author, int duration, JSONObject metainfo,
                          int season, int episode, int type, int tvshow_id) {
        super(title, description, created, thumbnailUri, videoId, author, duration);
        this.metainfo = metainfo;
        this.season = season;
        this.episode = episode;
        this.type = type;
    }

    public TVShowFeedItem(FeedItem item, JSONObject metainfo, int season, int episode, int type,
                          int tvshow_id) {
        this(item.getTitle(), item.getDescription(), item.getCreated(), item.getThumbnailUri(),
                item.getVideoId(), item.getAuthor(), item.getDuration(), metainfo, season,
                episode, type, tvshow_id);
    }

    public static TVShowFeedItem fromParcel(Parcel parcel) {
        FeedItem item = FeedItem.fromParcel(parcel);
        int season = parcel.readInt();
        int episode = parcel.readInt();
        int type = parcel.readInt();
        int tvshow_id = parcel.readInt();
        return new TVShowFeedItem(item, null, season, episode, type, tvshow_id);
    }

    public static TVShowFeedItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        int season = data.optInt(JSON_SEASON, 0);
        int episode = data.optInt(JSON_EPISODE, 1);
        JSONObject typeData = data.optJSONObject(JSON_TYPE);
        int type = (typeData != null)?typeData.getInt(JSON_TYPE_ID): 0;

        return new TVShowFeedItem(item, null, season, episode, type, 0);
    }

    // Parcelable implementation

    public static final Creator<TVShowFeedItem> CREATOR
            = new Creator<TVShowFeedItem>() {
        public TVShowFeedItem createFromParcel(Parcel in) {
            return TVShowFeedItem.fromParcel(in);
        }

        public TVShowFeedItem[] newArray(int size) {
            return new TVShowFeedItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
    public void fillRow(ContentValues row, int position) {
        fillRow(row);
    }

    public static TVShowFeedItem fromCursor(Cursor c) {
        FeedItem item = FeedItem.fromCursor(c);
        int i = c.getColumnIndexOrThrow(FeedContract.TVShowVideo.METAINFO);
        String metainfo_json = c.getString(i);
        i = c.getColumnIndexOrThrow(FeedContract.TVShowVideo.SEASON);
        int season = c.getInt(i);
        i = c.getColumnIndexOrThrow(FeedContract.TVShowVideo.EPISODE);
        int episode = c.getInt(i);
        i = c.getColumnIndexOrThrow(FeedContract.TVShowVideo.TYPE);
        int type = c.getInt(i);
        i = c.getColumnIndexOrThrow(FeedContract.TVShowVideo.TVSHOW_ID);
        int tvshow_id = c.getInt(i);
        try {
            JSONObject metainfo = new JSONObject(metainfo_json);
            return new TVShowFeedItem(item, metainfo, season, episode, type, tvshow_id);
        } catch (JSONException e) {
            e.printStackTrace();
            return new TVShowFeedItem(item, null, season, episode, type, tvshow_id);
        }
    }

    protected void fillRow(ContentValues row) {
        super.fillRow(row);
        row.put(FeedContract.TVShowVideo.SEASON, season);
        row.put(FeedContract.TVShowVideo.EPISODE, episode);
        row.put(FeedContract.TVShowVideo.TYPE, type);
        row.put(FeedContract.TVShowVideo.TVSHOW_ID, tvshow_id);
    }

    public void setTVShowId(int id) {
        tvshow_id = id;
    }
}
