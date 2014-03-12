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
public class ShowcaseTab implements Parcelable {
    private static final String LOG_TAG = ShowcaseTab.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final String JSON_TAB_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_SORT = "sort";
    private static final String JSON_ORDER_NUMBER = "order_number";
    private static final String JSON_TABS = "tabs";
    private final int mShowcaseId;
    private int mId;
    private int mOrderNumber;
    private String mName;
    private String mSort;

    public ShowcaseTab(int id, String name, String sort, int orderNumber, int showcaseId) {
        mId = id;
        mName = name;
        mSort = sort;
        mOrderNumber = orderNumber;
        mShowcaseId = showcaseId;
    }

    public static ShowcaseTab fromParcel(Parcel p) {
        int id = p.readInt();
        String name = p.readString();
        String sort = p.readString();
        int orderNumber = p.readInt();
        int showcaseId = p.readInt();
        return new ShowcaseTab(id, name, sort, orderNumber, showcaseId);
    }

    public static ShowcaseTab fromJSON(JSONObject data, int showcaseId) throws JSONException {
        int id = data.getInt(JSON_TAB_ID);
        String name = data.getString(JSON_NAME);
        String sort = data.getString(JSON_SORT);
        int orderNumber = data.getInt(JSON_ORDER_NUMBER);
        return new ShowcaseTab(id, name, sort, orderNumber, showcaseId);
    }

    public static ShowcaseTab fromCursor(Cursor c) {
        int showcaseID = c.getInt(c.getColumnIndex(FeedContract.ShowcaseTabs.SHOWCASE_ID));
        int id = c.getInt(c.getColumnIndex(FeedContract.ShowcaseTabs._ID));
        int orderNumber = c.getInt(c.getColumnIndex(FeedContract.ShowcaseTabs.ORDER_NUMBER));
        String name = c.getString(c.getColumnIndex(FeedContract.ShowcaseTabs.NAME));
        String sort = c.getString(c.getColumnIndex(FeedContract.ShowcaseTabs.SORT));

        return new ShowcaseTab(id, name, sort, orderNumber, showcaseID);
    }

    public static JsonObjectRequest getShowcaseRequest(Uri uri, int showcaseId, RequestListener requestListener) {
        assert uri!= null;
        if (D) Log.d(LOG_TAG, "Fetching: " + uri.toString());
        JsonObjectRequest request = new JsonObjectRequest(uri.toString(), null,
                getShowcaseListener(showcaseId, requestListener),
                getErrorListener(requestListener));
        request.setShouldCache(true);
        request.setTag(Requests.SHOWCASE);
        return request;
    }

    private static Response.Listener<JSONObject> getShowcaseListener(final int showcaseId,
                                                                     final RequestListener listener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    parseJSONResponse(response, showcaseId);
                    if (listener != null)
                        listener.onResult(Requests.SHOWCASE, null);
                } catch (JSONException e) {
                    if (listener != null)
                        listener.onRequestError(Requests.SHOWCASE,
                                new RequestListener.RequestError(e.getMessage()));
                }
            }
        };
    }

    protected static void parseJSONResponse(JSONObject response, int showcaseId) throws JSONException {
        JSONArray tabs = response.getJSONArray(JSON_TABS);
        ContentValues[] tabsItems = new ContentValues[tabs.length()];
        for (int i=0; i<tabs.length(); i++) {
            JSONObject data = tabs.getJSONObject(i);
            ShowcaseTab item = fromJSON(data, showcaseId);
            ContentValues row = fillRow(item);
            if (D) Log.d(LOG_TAG, "TAB: " + data.toString());
            tabsItems[i] = row;
        }
        Context context = RutubeApp.getInstance();
        try {
            Uri uri = FeedContract.ShowcaseTabs.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(showcaseId))
                    .build();
            assert uri != null;
            context.getContentResolver().delete(uri, null, null);
            context.getContentResolver().bulkInsert(uri, tabsItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.getContentResolver().notifyChange(FeedContract.ShowcaseTabs.CONTENT_URI, null);
        if (D) Log.d(LOG_TAG, "Operation finished");
    }

    private static ContentValues fillRow(ShowcaseTab item) {
        ContentValues row = new ContentValues();
        item.fillRow(row);
        return row;
    }

    private void fillRow(ContentValues row) {
        row.put(FeedContract.ShowcaseTabs.SHOWCASE_ID, mShowcaseId);
        row.put(FeedContract.ShowcaseTabs._ID, mId);
        row.put(FeedContract.ShowcaseTabs.NAME, mName);
        row.put(FeedContract.ShowcaseTabs.SORT, mSort);
        row.put(FeedContract.ShowcaseTabs.ORDER_NUMBER, mOrderNumber);
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
        parcel.writeInt(mId);
        parcel.writeString(mName);
        parcel.writeString(mSort);
        parcel.writeInt(mOrderNumber);
        parcel.writeInt(mShowcaseId);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator<ShowcaseTab> CREATOR
            = new Creator<ShowcaseTab>() {
        public ShowcaseTab createFromParcel(Parcel in) {
            return ShowcaseTab.fromParcel(in);
        }

        public ShowcaseTab[] newArray(int size) {
            return new ShowcaseTab[size];
        }
    };

    public String getName() {
        return mName;
    }
}
