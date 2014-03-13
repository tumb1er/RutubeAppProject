package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 11.03.14.
 */
public class TabSource implements Parcelable {
    private static final String LOG_TAG = TabSource.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final String JSON_URL = "url";
    private static final String JSON_ORDER_NUMBER = "order_number";
    private static final String JSON_CONTENT_TYPE = "content_type";
    private static final String JSON_ID = "id";
    private int mId;
    private int mOrderNumber;
    private int mContentTypeId;
    private String mLink;
    private int mTabId;

    public TabSource(int id, int orderNumber, int contentTypeId, String link, int tabId) {
        mLink = link;
        mOrderNumber = orderNumber;
        mContentTypeId = contentTypeId;
        mTabId = tabId;
        mId = id;
    }
    public static TabSource fromParcel(Parcel p) {
        String link = p.readString();
        int orderNumber = p.readInt();
        int contentTypeId = p.readInt();
        int tabId = p.readInt();
        int id = p.readInt();
        return new TabSource(id, orderNumber, contentTypeId, link, tabId);
    }

    public static TabSource fromJSON(JSONObject data, int tabId) throws JSONException {
        String link = data.getString(JSON_URL);
        int orderNumber = data.getInt(JSON_ORDER_NUMBER);
        JSONObject contentType = data.getJSONObject(JSON_CONTENT_TYPE);
        int contentTypeId = contentType.getInt(JSON_ID);
        int id = data.getInt(JSON_ID);
        return new TabSource(id, orderNumber, contentTypeId, link, tabId);
    }

    public static TabSource fromCursor(Cursor c) {
        String link = c.getString(c.getColumnIndex(FeedContract.TabSources.LINK));
        int id = c.getInt(c.getColumnIndex(FeedContract.TabSources._ID));
        int tabId = c.getInt(c.getColumnIndex(FeedContract.TabSources.TAB_ID));
        int contentTypeId = c.getInt(c.getColumnIndex(FeedContract.TabSources.CONTENT_TYPE_ID));
        int orderNumber = c.getInt(c.getColumnIndex(FeedContract.TabSources.ORDER_NUMBER));
        return new TabSource(id,orderNumber, contentTypeId, link, tabId);
    }

    public void fillRow(ContentValues row) {
        row.put(FeedContract.TabSources.TAB_ID, mTabId);
        row.put(FeedContract.TabSources._ID, mId);
        row.put(FeedContract.TabSources.LINK, mLink);
        row.put(FeedContract.TabSources.CONTENT_TYPE_ID, mContentTypeId);
        row.put(FeedContract.TabSources.ORDER_NUMBER, mOrderNumber);
    }

    /**
     * Parcelable implementation
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mLink);
        parcel.writeInt(mOrderNumber);
        parcel.writeInt(mContentTypeId);
        parcel.writeInt(mTabId);
        parcel.writeInt(mId);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator<TabSource> CREATOR
            = new Creator<TabSource>() {
        public TabSource createFromParcel(Parcel in) {
            return TabSource.fromParcel(in);
        }

        public TabSource[] newArray(int size) {
            return new TabSource[size];
        }
    };

    public String getLink() {
        return mLink;
    }
    public Uri getUri() {
        if (mLink.startsWith("http")) {
            return Uri.parse(mLink);
        }
        return Uri.parse(RutubeApp.getUrl(mLink));
    }

    public int getId() {
        return mId;
    }

    public void setTabId(int tabId) {
        mTabId = tabId;
    }
}
