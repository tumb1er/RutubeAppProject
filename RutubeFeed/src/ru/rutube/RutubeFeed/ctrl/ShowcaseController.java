package ru.rutube.RutubeFeed.ctrl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.content.FeedContentProvider;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.ShowcaseTab;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseController implements Parcelable {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = ShowcaseController.class.getName();
    private final Uri mShowcaseUri;
    private Context mContext;
    private RequestQueue mRequestQueue;
    private ShowcaseView mView;
    private boolean mAttached;
    private int mShowcaseId;
    private ArrayList<Integer> mTabIds;
    private RequestListener mShowcaseRequestListener = new RequestListener() {
        @Override
        public void onResult(int tag, Bundle result) {
            refreshTabs();
        }

        @Override
        public void onVolleyError(VolleyError error) {

        }

        @Override
        public void onRequestError(int tag, RequestError error) {

        }
    };

    private void refreshTabs() {
        Uri contentUri = FeedContract.ShowcaseTabs.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(mShowcaseId))
                .build();
        assert contentUri != null;
        String[] projection = FeedContentProvider.getProjection(contentUri);
        Cursor c = mContext.getContentResolver().query(contentUri, projection, null, null, null);
        assert c != null;
        c.moveToFirst();
        ArrayList<Integer> newTabs = new ArrayList<Integer>();
        ShowcaseTab[] tabs = new ShowcaseTab[c.getCount()];
        for (int i=0;i<c.getCount(); i++) {
            ShowcaseTab tab = ShowcaseTab.fromCursor(c);
            c.moveToNext();
            tabs[i] = tab;
            newTabs.add(tab.getId());
        }
        if (mAttached) {
            // Проверяем, если список вкладок изменился, то обновляем его.
            List<Integer> common = new ArrayList<Integer>(newTabs);
            common.removeAll(mTabIds);
            if (D) Log.d(LOG_TAG, "New - old " + String.valueOf(common));
            boolean changed = common.size() > 0;
            common = new ArrayList<Integer>(mTabIds);
            common.removeAll(newTabs);
            if (D) Log.d(LOG_TAG, "Old - new" + String.valueOf(common));
            if (D) Log.d(LOG_TAG, "Tabs: " + String.valueOf(mTabIds) + " vs " + String.valueOf(newTabs));
            if (newTabs.size() > 0 && (changed || common.size() > 0)) {
                mTabIds = newTabs;
                mView.initTabs(tabs);
            }
        }
    }

    public interface ShowcaseView {

        void initTabs(ShowcaseTab[] tabs);
    }

    public ShowcaseController(Uri showcaseUri, int showcaseId) {
        String showcaseSlug =  showcaseUri.getLastPathSegment();
        if (D) Log.d(LOG_TAG, "Showcase slug: " + showcaseSlug);
        mShowcaseUri = RutubeApp.formatApiUrl(R.string.showcase_api, showcaseSlug);
        if (D) Log.d(LOG_TAG, "Showcase uri path: " + mShowcaseUri.toString());
        mShowcaseId = showcaseId;
        mTabIds = new ArrayList<Integer>();
    }
    public ShowcaseController(Uri showcaseUri, int showcaseId, ArrayList<Integer> tabIds) {
        this(showcaseUri, showcaseId);
        mTabIds = tabIds;
    }
    public ShowcaseController(Uri showcaseUri) {
        this(showcaseUri, 0);
    }

    public void attach(Context context, ShowcaseView view){
        assert mContext != null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mAttached = true;
        refreshTabs();
        startRequests();
    }

    private void startRequests() {
        if (mShowcaseId == 0)
            mShowcaseId = queryShowcaseId();
        JsonObjectRequest request = ShowcaseTab.getShowcaseRequest(mShowcaseUri, mShowcaseId, mShowcaseRequestListener);
        mRequestQueue.add(request);
    }

    private int queryShowcaseId() {
        String path = mShowcaseUri.getEncodedPath();
        assert path != null;
        String link = path.replace("/api/", "");
        if (D)Log.d(LOG_TAG, "query showcase id: " + link);
        Cursor c = mContext.getContentResolver().query(
                FeedContract.Navigation.CONTENT_URI,
                new String[]{BaseColumns._ID},
                "link = ?",
                new String[]{link},
                null
                );
        assert c != null;
        if (c.getCount() == 0)
            return 0;
        c.moveToFirst();
        int showcaseId = c.getInt(0);
        if (D) Log.d(LOG_TAG, "Got showcase id: " + String.valueOf(showcaseId));
        c.close();
        return showcaseId;

    }

    public void detach() {
        mRequestQueue.cancelAll(Requests.SHOWCASE);
        mRequestQueue.stop();
        mRequestQueue = null;
        mContext = null;
        mView = null;
        mAttached = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mShowcaseUri.toString());
        parcel.writeInt(mShowcaseId);
        parcel.writeList(mTabIds);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<ShowcaseController> CREATOR
            = new Parcelable.Creator<ShowcaseController>() {
        public ShowcaseController createFromParcel(Parcel in) {
            return ShowcaseController.fromParcel(in);
        }

        public ShowcaseController[] newArray(int size) {
            return new ShowcaseController[size];
        }
    };

    private static ShowcaseController fromParcel(Parcel in) {
        if (D) Log.d(LOG_TAG, "From parcel!");
        String url = in.readString();
        int showcaseId = in.readInt();
        ArrayList<Integer> tabIds = in.readArrayList(Integer.class.getClassLoader());
        return new ShowcaseController(Uri.parse(url), showcaseId, tabIds);
    }
}
