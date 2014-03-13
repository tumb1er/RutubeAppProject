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
import ru.rutube.RutubeAPI.requests.AuthJsonObjectRequest;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 11.03.14.
 */
public class NaviItem implements Parcelable {
    private static final String LOG_TAG = NaviItem.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final String JSON_LINKS = "links";
    private static final String JSON_NAME = "name";
    private static final String JSON_TITLE = "title";
    private static final String JSON_LINK = "link";
    private static final String JSON_POSITION = "position";
    private String mName;
    private String mTitle;
    private String mLink;
    private int mPosition;
    private int mId;

    public NaviItem(String name, String title, String link, int position, int id) {
        mName = name;
        mTitle = title;
        if (link.startsWith("/")) {
            link = link.substring(1);
        }
        mLink = link;
        mPosition = position;
        mId = id;
    }
    public NaviItem(String name, String title, String link, int position) {
        this(name, title, link, position, 0);
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

    public static NaviItem fromCursor(Cursor c) {
        String name = c.getString(c.getColumnIndex(FeedContract.Navigation.NAME));
        String title = c.getString(c.getColumnIndex(FeedContract.Navigation.TITLE));
        String link = c.getString(c.getColumnIndex(FeedContract.Navigation.LINK));
        int position = c.getInt(c.getColumnIndex(FeedContract.Navigation.POSITION));
        int id = c.getInt(c.getColumnIndex(FeedContract.Navigation._ID));
        return new NaviItem(name, title, link, position, id);
    }

    public static JsonObjectRequest getNaviLinksRequest(RequestListener requestListener) {
        String uri = RutubeApp.getUrl(R.string.menu_links);
        assert uri!= null;
        if (D) Log.d(LOG_TAG, "Fetching: " + uri.toString());
        JsonObjectRequest request = new JsonObjectRequest(uri, null,
                getNaviLinksListener(requestListener),
                getErrorListener(requestListener));
        request.setShouldCache(true);
        request.setTag(Requests.MENU_LINKS);
        return request;
    }

    private static Response.Listener<JSONObject> getNaviLinksListener(final RequestListener listener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    parseJSONResponse(response);
                    if (listener != null)
                        listener.onResult(Requests.MENU_LINKS, null);
                } catch (JSONException e) {
                    if (listener != null)
                        listener.onRequestError(Requests.MENU_LINKS,
                                new RequestListener.RequestError(e.getMessage()));
                }
            }
        };
    }

    protected static void parseJSONResponse(JSONObject response) throws JSONException {
        JSONArray links = response.getJSONArray(JSON_LINKS);
        ContentValues[] naviItems = new ContentValues[links.length()];
        for (int i=0; i<links.length(); i++) {
            JSONObject data = links.getJSONObject(i);
            NaviItem item = fromJSON(data);
            ContentValues row = fillRow(item);
            if (D) Log.d(LOG_TAG, "NAVI: " + data.toString());
            naviItems[i] = row;
        }
        Context context = RutubeApp.getInstance();
        try {
            context.getContentResolver().delete(FeedContract.Navigation.CONTENT_URI, null, null);
            for (ContentValues cv: naviItems) {
                int updated = context.getContentResolver().update(FeedContract.Navigation.CONTENT_URI,
                        cv,
                        FeedContract.Navigation.LINK + " = ?",
                        new String[]{cv.getAsString(FeedContract.Navigation.LINK)});
                if (updated == 0) {
                    context.getContentResolver().insert(FeedContract.Navigation.CONTENT_URI, cv);
                }
            }
            context.getContentResolver().bulkInsert(FeedContract.Navigation.CONTENT_URI, naviItems);
        } catch (Exception e) {
            // TODO: обработать ошибку вставки в БД.
            e.printStackTrace();
        }
        context.getContentResolver().notifyChange(FeedContract.Navigation.CONTENT_URI, null);
        if (D) Log.d(LOG_TAG, "Operation finished");
    }

    private static ContentValues fillRow(NaviItem item) {
        ContentValues row = new ContentValues();
        item.fillRow(row);
        return row;
    }

    private void fillRow(ContentValues row) {
        row.put(FeedContract.Navigation.NAME, mName);
        row.put(FeedContract.Navigation.TITLE, mTitle);
        row.put(FeedContract.Navigation.LINK, mLink);
        row.put(FeedContract.Navigation.POSITION, mPosition);
    }

    private static Response.ErrorListener getErrorListener(final RequestListener listener)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (listener != null)
                    listener.onVolleyError(error);
            }
        };
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
        parcel.writeString(mName);
        parcel.writeString(mTitle);
        parcel.writeString(mLink);
        parcel.writeInt(mPosition);

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

    public String getLink() {
        return mLink;
    }
    public Uri getUri() {
        if (mLink.startsWith("http")) {
            return Uri.parse(mLink);
        }
        return Uri.parse(RutubeApp.getUrl(mLink));
    }

    public String getName() {
        return mName;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getId() {
        return mId;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }
}
