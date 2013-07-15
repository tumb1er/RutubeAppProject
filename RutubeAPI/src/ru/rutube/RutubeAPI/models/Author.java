package ru.rutube.RutubeAPI.models;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.content.FeedContract;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 18.05.13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class Author implements Parcelable {
    protected static final String JSON_AVATAR = "avatar_url";
    protected static final String JSON_ID = "id";
    protected static final String JSON_NAME = "name";
    private Uri avatarUrl;
    private int id;
    private String name;

    public Uri getAvatarUrl() {
        return avatarUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Author(Uri avatarUri, int id, String name) {
        this.avatarUrl = avatarUri;
        this.id = id;
        this.name = name;
    }

    public static Author fromParcel(Parcel parcel) {
        Uri avatarUri = parcel.readParcelable(Uri.class.getClassLoader());
        int id = parcel.readInt();
        String name = parcel.readString();
        return new Author(avatarUri, id, name);
    }

    public static Author fromJSON(JSONObject data) throws JSONException {
        Uri avatarUri = Uri.parse(data.getString(JSON_AVATAR));
        int id = data.getInt(JSON_ID);
        String name = data.getString(JSON_NAME);
        return new Author(avatarUri, id, name);
    }

    // Parcelable implementation

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<Author> CREATOR
            = new Parcelable.Creator<Author>() {
        public Author createFromParcel(Parcel in) {
            return Author.fromParcel(in);
        }

        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(avatarUrl, i);
        parcel.writeInt(id);
        parcel.writeString(name);
    }

    public static Author fromCursor(Cursor c) {
        int authorID = c.getInt(c.getColumnIndex(FeedContract.FeedColumns.AUTHOR_ID));
        String authorName = c.getString(c.getColumnIndex(FeedContract.FeedColumns.AUTHOR_NAME));
        Uri avatarUri = Uri.parse(c.getString(c.getColumnIndex(FeedContract.FeedColumns.AVATAR_URI)));
        return new Author(avatarUri, authorID, authorName);
    }
}
