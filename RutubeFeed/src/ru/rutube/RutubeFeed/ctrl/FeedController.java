package ru.rutube.RutubeFeed.ctrl;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.Feed;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;

/**
 * Created by tumbler on 08.07.13.
 */
public class FeedController implements Parcelable {
    private final Uri mFeedUri;
    private static final int LOADER_ID = 1;

    public interface FeedView {
        public ListAdapter getListAdapter();
        public void setListAdapter(ListAdapter adapter);
        public void setRefreshing();
        public void doneRefreshing();
        public void showError();
        public LoaderManager getLoaderManager();
    }
    private static final String LOG_TAG = FeedController.class.getName();
    private static final String[] PROJECTION = {
            FeedContract.FeedColumns._ID,
            FeedContract.FeedColumns.TITLE,
            FeedContract.FeedColumns.DESCRIPTION,
            FeedContract.FeedColumns.CREATED,
            FeedContract.FeedColumns.THUMBNAIL_URI,
            FeedContract.FeedColumns.AUTHOR_NAME,
            FeedContract.FeedColumns.AVATAR_URI
    };

    private Feed mFeed;
    private Context mContext;
    private FeedView mView;
    private int mPerPage = 10;
    private RequestQueue mRequestQueue;
    private boolean mLoading = false;
    private boolean mAttached = false;


    public FeedController(Uri feedUri) {
        mContext = null;
        mView = null;
        mFeedUri = feedUri;
    }

    public void attach(Context context, FeedView view) {
        assert mContext == null;
        assert mView == null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
            new HttpClientStack(HttpTransport.getHttpClient()));
        mFeed = new Feed(mFeedUri, mContext);

        FeedCursorAdapter adapter = prepareFeedCursorAdapter();
        mView.getLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
        mView.setListAdapter(adapter);
        loadPage(1);
        Log.d(LOG_TAG, "Attached!");
        mAttached = true;
    }

    private FeedCursorAdapter prepareFeedCursorAdapter() {
        FeedCursorAdapter adapter = new FeedCursorAdapter(mContext,
                R.layout.feed_item,
                null,
                new String[]{FeedContract.FeedColumns.TITLE, FeedContract.FeedColumns.THUMBNAIL_URI},
                new int[]{R.id.titleTextView, R.id.thumbnailImageView},
                0);
        adapter.setLoadMoreListener(loadMoreListener);
        return adapter;
    }

    public  void detach() {
        mContext = null;
        mView = null;
        mRequestQueue = null;
        mAttached = false;
    }

    private FeedCursorAdapter.LoadMoreListener loadMoreListener = new FeedCursorAdapter.LoadMoreListener(){

        @Override
        public void onLoadMore() {
            ListAdapter adapter = mView.getListAdapter();
            loadPage((adapter.getCount() + mPerPage) / mPerPage);
        }
    };

    private RequestListener mLoadPageRequestListener = new RequestListener() {
        @Override
        public void onResult(int tag, Bundle result) {
            Log.d(LOG_TAG, "onRequestFinished");
            if (!mAttached)
                return;
            if (mView.getListAdapter().getCount() == 0)
                mContext.getContentResolver().notifyChange(mFeed.getContentUri(), null);
            mPerPage = result.getInt(Constants.Result.PER_PAGE);
            mView.doneRefreshing();
            mLoading = false;
        }

        @Override
        public void onVolleyError(VolleyError error) {
            mView.showError();
        }

        @Override
        public void onRequestError(int tag, RequestError error) {
            mView.showError();
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    mContext,
                    mFeed.getContentUri(),
                    PROJECTION,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            Log.d(LOG_TAG, "onLoadFinished " + String.valueOf(cursor.getCount()));
            ((CursorAdapter) mView.getListAdapter()).swapCursor(cursor);
            if (cursor.getCount() < mPerPage) {
                Log.d(LOG_TAG, "load more from olf");
                loadPage((cursor.getCount() + mPerPage) / mPerPage);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            ((CursorAdapter) mView.getListAdapter()).swapCursor(null);
        }
    };

    private void loadPage(int page) {
        mLoading = true;
        mView.setRefreshing();
        JsonObjectRequest request = mFeed.getFeedRequest(page, mContext, mLoadPageRequestListener);
        mRequestQueue.add(request);
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
