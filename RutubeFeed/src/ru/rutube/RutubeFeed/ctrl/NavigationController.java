package ru.rutube.RutubeFeed.ctrl;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContentProvider;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 11.03.14.
 */
public class NavigationController implements Parcelable, RequestListener {
    private static final String LOG_TAG = NavigationController.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final int NAVI_LOADER = 0;

    private Context mContext;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private NavigationView mView;

    private User mUser;
    private boolean mAttached;
    private NaviLoaderCallbacks loaderCallbacks;


    /**
     * Обработчик событий загрузки данных в адаптер навигационного меню
     */
    protected class NaviLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
            // Возвращает курсор для данных меню навигации
            return new CursorLoader(
                    RutubeApp.getContext(),
                    FeedContract.Navigation.CONTENT_URI,
                    FeedContentProvider.getProjection(FeedContract.Navigation.CONTENT_URI),
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            CursorAdapter adapter = mView.getNavAdapter();
            if (adapter != null)
                adapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            CursorAdapter adapter = mView.getNavAdapter();
            if (adapter != null)
                adapter.swapCursor(null);

        }
    }

    public interface NavigationView {
        CursorAdapter getNavAdapter();
        LoaderManager getSupportLoaderManager();
        void showHeader(User user);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    @Override
    public void onResult(int tag, Bundle result) {
        switch(tag) {
            case Requests.VISITOR:
                mUser = result.getParcelable(Constants.Result.USER);
                if (!mUser.isAnonymous()) {
                    JsonObjectRequest request = mUser.getProfileRequest(this);
                    mRequestQueue.add(request);
                }
                break;
            case Requests.USER_PROFILE:
                mUser = result.getParcelable(Constants.Result.USER);
                break;
            case Requests.TOKEN:
                requestVisitor();
                break;

        }
        mView.showHeader(mUser);
    }

    @Override
    public void onVolleyError(VolleyError error) {
        mView.showHeader(mUser);

    }

    @Override
    public void onRequestError(int tag, RequestError error) {
        mView.showHeader(mUser);
    }

    public void attach(Context context, NavigationView view) {
        assert mContext == null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mImageLoader = new ImageLoader(mRequestQueue, RutubeApp.getBitmapCache());
        mUser = User.fromContext();
        mAttached = true;
        initLoader();
        mView.showHeader(mUser);
        requestVisitor();
    }

    public void requestToken(String email, String password) {
        mRequestQueue.add(mUser.getTokenRequest(email, password, this));
    }

    private void initLoader() {
        loaderCallbacks = new NaviLoaderCallbacks();
        mView.getSupportLoaderManager().initLoader(NAVI_LOADER, null, loaderCallbacks);

    }

    private void requestVisitor() {
        JsonObjectRequest visitorRequest = mUser.getVisitorRequest(this);
        mRequestQueue.add(visitorRequest);
    }

    public void detach() {
        mContext = null;
        mRequestQueue.cancelAll(Requests.VISITOR);
        mRequestQueue.cancelAll(Requests.USER_PROFILE);
        mRequestQueue.stop();
        mAttached = false;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
