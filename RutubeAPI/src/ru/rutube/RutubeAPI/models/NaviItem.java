package ru.rutube.RutubeAPI.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tumbler on 11.03.14.
 */
public class NaviItem implements Parcelable {
    private static final String JSON_NAME = "name";
    private static final String JSON_TITLE = "title";
    private static final String JSON_LINK = "link";
    private static final String JSON_POSITION = "position";
    private String mName;
    private String mTitle;
    private String mLink;
    private int mPosition;

    public NaviItem(String name, String title, String link, int position) {
        mName = name;
        mTitle = title;
        mLink = link;
        mPosition = position;
    }

    public static NaviItem fromParcel(Parcel p) {
        String name = p.readString();
        String title = p.readString();
        String link = p.readString();
        int position = p.readInt();
        return new NaviItem(name, title, link, position);
    }

    public static NaviItem fromJSON(JSONObject data) throws JSONException {
        String name = data.getString(JSON_NAME);
        String title = data.getString(JSON_TITLE);
        String link = data.getString(JSON_LINK);
        int position = data.getInt(JSON_POSITION);
        return new NaviItem(name, title, link, position);
    }


    /**
     * Parcelable implementation
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<NaviItem> CREATOR
            = new Parcelable.Creator<NaviItem>() {
        public NaviItem createFromParcel(Parcel in) {
            return NaviItem.fromParcel(in);
        }

        public NaviItem[] newArray(int size) {
            return new NaviItem[size];
        }
    };
}
