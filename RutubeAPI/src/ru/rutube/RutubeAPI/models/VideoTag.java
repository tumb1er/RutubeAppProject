package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.R;

/**
 * Created by tumbler on 14.09.13.
 */
public class VideoTag implements Parcelable {
    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_COMMENT = "comment";
    private int mId;
    private String mTag;
    private String mMessage;

    public VideoTag(int id, String tag, String message) {
        mId = id;
        mTag = tag;
        mMessage = message;
    }

    public String getTag() {
        return mTag;
    }

    public String getMessage() {
        return mMessage;
    }

    public int getId() {return mId; }

    public static VideoTag fromJSON(JSONObject data) throws JSONException {
        int id = data.getInt(JSON_ID);
        String tag = data.getString(JSON_NAME);
        String message = data.optString(JSON_COMMENT);
        return new VideoTag(id, tag, message);
    }

    public static VideoTag fromParcel(Parcel p) {
        int id = p.readInt();
        String tag = p.readString();
        String message = p.readString();
        return new VideoTag(id, tag, message);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();
        result.put(JSON_ID, mId);
        result.put(JSON_NAME, mTag);
        result.put(JSON_COMMENT, mMessage);
        return result;
    }

    public String getHtml(Context context) {
        return String.format("<a href=\"%s/tags/video/%d/\">#%s</a>",
                context.getString(R.string.base_uri), mId, mTag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mId);
        parcel.writeString(mTag);
        parcel.writeString(mMessage);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<VideoTag> CREATOR
            = new Parcelable.Creator<VideoTag>() {
        public VideoTag createFromParcel(Parcel in) {
            return VideoTag.fromParcel(in);
        }

        public VideoTag[] newArray(int size) {
            return new VideoTag[size];
        }
    };
}
