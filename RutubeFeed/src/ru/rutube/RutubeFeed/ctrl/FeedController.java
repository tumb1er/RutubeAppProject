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
import ru.rutube.RutubeAPI.content.FeedContentProvider;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.Feed;
import ru.rutube.RutubeAPI.models.FeedItem;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;

/**
 * Created by tumbler on 08.07.13.
 */
public class FeedController implements Parcelable {

    /**
     * Контракт пользовательского интерфейса
     */
    public interface FeedView {
        public ListAdapter getListAdapter();
        public void setListAdapter(ListAdapter adapter);
        public void setRefreshing();
        public void doneRefreshing();
        public void showError();
        public LoaderManager getLoaderManager();
        public void openPlayer(Uri uri, Uri thumbnailUri);
    }

    private static final int LOADER_ID = 1;
    private static final String LOG_TAG = FeedController.class.getName();
    private Uri mFeedUri;
    private Feed mFeed;
    private Context mContext;
    private FeedView mView;
    private int mPerPage = 10;
    private RequestQueue mRequestQueue;
    private int mLoading = 0;
    private boolean mAttached = false;
    private int mLastItemsCount;


    public FeedController(Uri feedUri) {
        mContext = null;
        mView = null;
        mFeedUri = feedUri;
    }

    /**
     * Получает последние обновления ленты
     */
    public void refresh() {
        Log.d(LOG_TAG, "Refreshing");
        loadPage(1);
    }

    /**
     * По клику на элементе ленты открывает плеер
     * @param position индекс выбранного элемента
     */
    public void onListItemClick(int position) {
        Log.d(LOG_TAG, "onListItemClick");
        Cursor c = (Cursor) mView.getListAdapter().getItem(position);
        FeedItem item = Feed.loadFeedItem(mContext, c, mFeedUri);
        Uri uri = item.getVideoUri(mContext);
        mView.openPlayer(uri, item.getThumbnailUri());
    }

    /**
     * Присоединяется к контексту и пользовательскому интерфейсу,
     * инициализирует объекты, зависящие от активити.
     * @param context экземпляр активити
     * @param view пользовательский интерфейс
     */
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
        mAttached = true;
        loadPage(1);
    }

    /**
     * Отсоединяется от останавливаемой активити
     */
    public void detach() {
        mRequestQueue.stop();
        mRequestQueue = null;
        mContext = null;
        mView = null;
        mAttached = false;
    }

    // Реализация Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mFeedUri, i);
    }

    public static FeedController fromParcel(Parcel in) {
        Uri feedUri = in.readParcelable(Uri.class.getClassLoader());
        return new FeedController(feedUri);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<FeedController> CREATOR
            = new Parcelable.Creator<FeedController>() {
        public FeedController createFromParcel(Parcel in) {
            return FeedController.fromParcel(in);
        }

        public FeedController[] newArray(int size) {
            return new FeedController[size];
        }
    };

    /**
     * Настраивает адаптер данных
     * @return
     */
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


    /**
     * Обрабатывает события "нужно загрузить следующую страницу", приходящие от адаптера
     */
    private FeedCursorAdapter.LoadMoreListener loadMoreListener = new FeedCursorAdapter.LoadMoreListener(){

        @Override
        public void onLoadMore() {
            ListAdapter adapter = mView.getListAdapter();
            loadPage((adapter.getCount() + mPerPage) / mPerPage);
        }
    };

    /**
     * Обработчик ответа от API ленты
     */
    private RequestListener mLoadPageRequestListener = new RequestListener() {
        @Override
        public void onResult(int tag, Bundle result) {
            if (!mAttached)
                return;
            FeedCursorAdapter listAdapter = (FeedCursorAdapter)mView.getListAdapter();
            if (listAdapter.getCount() == 0)
                mContext.getContentResolver().notifyChange(mFeed.getContentUri(), null);
            mPerPage = result.getInt(Constants.Result.PER_PAGE);
            listAdapter.setPerPage(mPerPage);
            requestDone();
        }

        private void requestDone() {
            if (mLoading > 0) mLoading -= 1;
            if (mLoading == 0)
                mView.doneRefreshing();
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

    /**
     * Обработчик запросов к БД
     */
    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    mContext,
                    mFeed.getContentUri(),
                    FeedContentProvider.getProjection(mFeed.getContentUri()),
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            Log.d(LOG_TAG, "onLoadFinished " + String.valueOf(cursor.getCount()));
            ((CursorAdapter) mView.getListAdapter()).swapCursor(cursor);
            // Грузим следующую страницу только если кэш в БД невалидный
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

    /**
     * Запрашивает страницу API ленты
     * @param page номер страницы с 1
     */
    private void loadPage(int page) {
        if (mLoading != 0)
            return;
        mView.setRefreshing();
        mLoading += 1;
        mLastItemsCount = mView.getListAdapter().getCount();
        JsonObjectRequest request = mFeed.getFeedRequest(page, mContext, mLoadPageRequestListener);
        mRequestQueue.add(request);
    }

}
