package ru.rutube.RutubeAPI.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tumbler on 14.09.13.
 */
public class VideoTag {
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
        String message = data.getString(JSON_COMMENT);
        return new VideoTag(id, tag, message);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();
        result.put(JSON_ID, mId);
        result.put(JSON_NAME, mTag);
        result.put(JSON_COMMENT, mMessage);
        return result;
    }
}
