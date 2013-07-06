package ru.rutube.RutubeAPI.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 04.05.13
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public class FeedItem implements Parcelable {
    public final static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    protected static final String JSON_TITLE = "title";
    protected static final String JSON_DESCIPTION = "description";
    protected static final String JSON_CREATED = "created_ts";
    protected static final String JSON_POSTING = "posting_ts";
    protected static final String JSON_VIDEO = "video";
    protected static final String JSON_THUMBNAIL_URL = "thumbnail_url";
    protected static final String JSON_VIDEO_ID = "id";

    private String title;
    private String description;
    private Date created;
    private Uri thumbnailUri;
    private String videoId;
    private Author author;

    public FeedItem(String title, String description, Date created, Uri thumbnailUri, String videoId, Author author) {
        this.title = title;
        this.description = description;
        this.created = created;
        this.thumbnailUri = thumbnailUri;
        this.videoId = videoId;
        this.author = author;
    }

    public static FeedItem fromParcel(Parcel parcel) {
        String title = parcel.readString();
        String description = parcel.readString();
        Date created;
        try {
            created = dtf.parse(parcel.readString());
        } catch (ParseException e) {
            created = new Date(0);
        } catch (NullPointerException e) {
            created = new Date(0);
        }
        Uri thumbnailUri = parcel.readParcelable(Uri.class.getClassLoader());
        String videoId = parcel.readString();
        boolean hasAuthor = parcel.readInt() > 0;
        Author author = null;
        if (hasAuthor) {
            author = Author.fromParcel(parcel);
        }
        return new FeedItem(title, description, created, thumbnailUri, videoId, author);
    }

    protected static Date getCreated(JSONObject data) {
        Log.d(FeedItem.class.getName(), "JSON: " + data.toString());

        String date;
        date = data.optString(JSON_POSTING);
        Log.d(FeedItem.class.getName(), "posting_ts "+ date);
        JSONObject video = data.optJSONObject(JSON_VIDEO);
        if (date.isEmpty() && video != null)
            date = video.optString(JSON_CREATED);
        if (date.isEmpty())
            date = data.optString(JSON_CREATED);
        try {
            Log.d(FeedItem.class.getName(), "created: " + date);
            return dtf.parse(date);
        } catch (ParseException e) {
            return new Date(0);
        } catch (NullPointerException e) {
            return new Date(0);
        }
    }

    public Author getAuthor() {
        return author;
    }
    public static Author getAuthor(JSONObject data) throws JSONException {
        try {
            return Author.fromJSON(data.getJSONObject("last_poster"));
        } catch(JSONException ignored) {}
        try {
            return Author.fromJSON(data.getJSONObject("author"));
        } catch(JSONException ignored) {}
        try {
            JSONObject video = data.getJSONObject("video");
            return Author.fromJSON(video.getJSONObject("author"));
        } catch (JSONException ignored) {}
        return null;
    }

    public static FeedItem fromJSON(JSONObject data) throws JSONException {
        Date created = getCreated(data);
        Author author = getAuthor(data);
        try {
            data = data.getJSONObject(JSON_VIDEO);
        } catch (JSONException e) {
        }
        String title = data.getString(JSON_TITLE);
        String description = data.getString(JSON_DESCIPTION);
        Uri thumbnailUri = Uri.parse(data.getString(JSON_THUMBNAIL_URL));
        String videoId = data.getString(JSON_VIDEO_ID);
        Log.d(FeedItem.class.getName(), "Created item: " + videoId + " " + String.valueOf(created));
        return new FeedItem(title, description, created, thumbnailUri, videoId, author);
    }

    // Parcelable implementation

    public static final Parcelable.Creator<FeedItem> CREATOR
            = new Parcelable.Creator<FeedItem>() {
        public FeedItem createFromParcel(Parcel in) {
            return FeedItem.fromParcel(in);
        }

        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getTitle());
        parcel.writeString(getDescription());
        parcel.writeString(dtf.format(getCreated()));
        parcel.writeParcelable(getThumbnailUri(), i);
        parcel.writeString(getVideoId());
        if (author != null) {
            parcel.writeInt(1);
            author.writeToParcel(parcel, i);
        } else {
            parcel.writeInt(0);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreated() {
        return created;
    }

    public Uri getThumbnailUri() {
        return thumbnailUri;
    }

    public String getVideoId() {
        return videoId;
    }
}
