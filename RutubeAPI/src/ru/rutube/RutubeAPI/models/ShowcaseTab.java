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
    private int mId;
    private int mOrderNumber;
    private String mName;
    private String mSort;

    public ShowcaseTab(int id, String name, String sort, int orderNumber) {
        mId = id;
        mName = name;
        mSort = sort;
        mOrderNumber = orderNumber;
    }

    public static ShowcaseTab fromParcel(Parcel p) {
        int id = p.readInt();
        String name = p.readString();
        String sort = p.readString();
        int orderNumber = p.readInt();
        return new ShowcaseTab(id, name, sort, orderNumber);
    }

    public static ShowcaseTab fromJSON(JSONObject data) throws JSONException {
        int id = data.getInt(JSON_TAB_ID);
        String name = data.getString(JSON_NAME);
        String sort = data.getString(JSON_SORT);
        int orderNumber = data.getInt(JSON_ORDER_NUMBER);
        return new ShowcaseTab(id, name, sort, orderNumber);
    }

    public static ShowcaseTab fromCursor(Cursor c) {
        // fixme
        return new ShowcaseTab(0, "", "", 0);
    }

    public static JsonObjectRequest getShowcaseRequest(Uri uri, RequestListener requestListener) {
        assert uri!= null;
        if (D) Log.d(LOG_TAG, "Fetching: " + uri.toString());
        JsonObjectRequest request = new JsonObjectRequest(uri.toString(), null,
                getShowcaseListener(requestListener),
                getErrorListener(requestListener));
        request.setShouldCache(true);
        request.setTag(Requests.SHOWCASE);
        return request;
    }

    private static Response.Listener<JSONObject> getShowcaseListener(final RequestListener listener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    parseJSONResponse(response);
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

    protected static void parseJSONResponse(JSONObject response) throws JSONException {
        JSONArray tabs = response.getJSONArray(JSON_TABS);
        ContentValues[] tabsItems = new ContentValues[tabs.length()];
        for (int i=0; i<tabs.length(); i++) {
            JSONObject data = tabs.getJSONObject(i);
            ShowcaseTab item = fromJSON(data);
            ContentValues row = fillRow(item);
            if (D) Log.d(LOG_TAG, "TAB: " + data.toString());
            tabsItems[i] = row;
        }
        Context context = RutubeApp.getInstance();
        try {
            // FIXME:
            //context.getContentResolver().delete(FeedContract.Navigation.CONTENT_URI, null, null);
            //context.getContentResolver().bulkInsert(FeedContract.Navigation.CONTENT_URI, tabsItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.getContentResolver().notifyChange(FeedContract.Navigation.CONTENT_URI, null);
        if (D) Log.d(LOG_TAG, "Operation finished");
    }

    private static ContentValues fillRow(ShowcaseTab item) {
        ContentValues row = new ContentValues();
        item.fillRow(row);
        return row;
    }

    private void fillRow(ContentValues row) {
//        row.put(FeedContract.Navigation.NAME, mName);
//        row.put(FeedContract.Navigation.TITLE, mTitle);
//        row.put(FeedContract.Navigation.LINK, mLink);
//        row.put(FeedContract.Navigation.POSITION, mPosition);
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
