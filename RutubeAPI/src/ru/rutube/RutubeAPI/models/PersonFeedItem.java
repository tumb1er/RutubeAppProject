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
public class PersonFeedItem extends FeedItem {
    private int person_id;

    public PersonFeedItem(String title, String description, Date created, Uri thumbnailUri,
                          String videoId, Author author, int duration, int person_id) {
        super(title, description, created, thumbnailUri, videoId, author, duration);
        this.person_id = person_id;
    }

    public PersonFeedItem(FeedItem item, int person_id) {
        this(item.getTitle(), item.getDescription(), item.getCreated(), item.getThumbnailUri(),
                item.getVideoId(), item.getAuthor(), item.getDuration(), person_id);
    }

    public static PersonFeedItem fromParcel(Parcel parcel) {
        FeedItem item = FeedItem.fromParcel(parcel);
        int person_id = parcel.readInt();
        return new PersonFeedItem(item, person_id);
    }

    public static PersonFeedItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        return new PersonFeedItem(item, 0);
    }

    // Parcelable implementation

    public static final Creator<PersonFeedItem> CREATOR
            = new Creator<PersonFeedItem>() {
        public PersonFeedItem createFromParcel(Parcel in) {
            return PersonFeedItem.fromParcel(in);
        }

        public PersonFeedItem[] newArray(int size) {
            return new PersonFeedItem[size];
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

    public static PersonFeedItem fromCursor(Cursor c) {
        FeedItem item = FeedItem.fromCursor(c);
        int i = c.getColumnIndexOrThrow(FeedContract.PersonVideo.PERSON_ID);
        int person_id = c.getInt(i);
        return new PersonFeedItem(item, person_id);
    }

    protected void fillRow(ContentValues row) {
        super.fillRow(row);
        row.put(FeedContract.PersonVideo.PERSON_ID, person_id);
    }

    public void setPersonId(int id) {
        person_id = id;
    }
}
