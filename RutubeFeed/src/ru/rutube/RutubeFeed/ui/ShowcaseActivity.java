package ru.rutube.RutubeFeed.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.ActionBarPolicy;
import android.support.v7.internal.widget.ActionBarContainer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContentProvider;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.data.NavAdapter;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseActivity extends ActionBarActivity {
    private static final String LOG_TAG = ShowcaseActivity.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final int NAVI_LOADER = 0;

    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NaviLoaderCallbacks loaderCallbacks;
    private NavAdapter mNaviAdapter;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;


    private class ShowcaseFragmentCache {
        Map<Uri, Fragment> mFragmentMap = new HashMap<Uri, Fragment>();
        public Fragment getShowcaseFragment(Uri uri, int showcaseId) {
            if (D) Log.d(LOG_TAG, "get fragment for " + uri.toString());
            Fragment f = mFragmentMap.get(uri);
//            if (f != null) {
//                if (D)Log.d(LOG_TAG, "got from cache");
//                return f;
//            }
            if (D) Log.d(LOG_TAG, "creating new fragment");
            f = new ShowcaseFragment();
            Bundle args = new Bundle();
            args.putParcelable(Constants.Params.SHOWCASE_URI, uri);
            args.putInt(Constants.Params.SHOWCASE_ID, showcaseId);
            f.setArguments(args);
//            mFragmentMap.put(uri, f);
            return f;
        }
    }

    private ShowcaseFragmentCache mFragmentCache = new ShowcaseFragmentCache();

    private AdapterView.OnItemClickListener mOnNavigationClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            NavAdapter.ViewHolder holder = (NavAdapter.ViewHolder)view.getTag();
            Uri showcaseUri = holder.naviItem.getUri();
            int showcaseId = holder.naviItem.getId();
            if (D)Log.d(LOG_TAG, "On Nav Click: " + String.valueOf(showcaseId));
            navigateToShowcase(showcaseUri, showcaseId);
            mNaviAdapter.setCurrentItemPosition(i);
            mDrawerLayout.closeDrawers();
            mNaviAdapter.notifyDataSetChanged();
        }
    };

    public void navigateToShowcase(Uri showcaseUri, int showcaseId) {
        if (D) Log.d(LOG_TAG, "Navigating to " + String.valueOf(showcaseUri));
        TextView tv = (TextView)findViewById(R.id.uriTextView);
        tv.setText(showcaseUri.toString());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment f = mFragmentCache.getShowcaseFragment(showcaseUri, showcaseId);
        Fragment old = fm.findFragmentById(R.id.content_placeholder);
        if (old != null) {
            ft.remove(old);
        }
        ft.add(R.id.content_placeholder, f);
        ft.commit();
    }

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
            CursorAdapter adapter = (CursorAdapter) getNavAdapter();
            if (adapter != null)
                adapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            CursorAdapter adapter = getNavAdapter();
            if (adapter != null)
                adapter.swapCursor(null);

        }
    }

    public CursorAdapter getNavAdapter() {
        ListAdapter adapter = mDrawerList.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        }
        return (CursorAdapter)adapter;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showcase_activity);
        initNavigationToggle();

        mRequestQueue = Volley.newRequestQueue(this,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mImageLoader = new ImageLoader(mRequestQueue, RutubeApp.getBitmapCache());

        initNavigationAdapter();
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        Uri uri = getIntent().getData();
        navigateToShowcase(uri, 0);


        initCurrentNavItem();
    }

    public void initCurrentNavItem() {
        mNaviAdapter.setCurrentItemPosition(0);
    }

    protected void initNavigationToggle() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_closed
        );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    protected void initNavigationAdapter() {
        loaderCallbacks = new NaviLoaderCallbacks();
        getSupportLoaderManager().initLoader(NAVI_LOADER, null, loaderCallbacks);
        mDrawerList = (ListView) findViewById(R.id.nav_list_view);
        if (D) Log.d(LOG_TAG, "Set nav adapter");

        View v = getLayoutInflater().inflate(R.layout.authorized_header, null);
        NetworkImageView av = (NetworkImageView)v.findViewById(R.id.avatarImageView);
        av.setImageUrl("http://pic.rutube.ru/user/38/c6/38c686faf55899a177fd01b954888b8d.jpg?size=s", mImageLoader);
        mDrawerList.addHeaderView(v);
        NetworkImageView bg = (NetworkImageView)findViewById(R.id.nav_background);
        bg.setImageUrl("http://pic.rutube.ru/userappearance/2b/d9/2bd961be163a0648f66c52776647ea2f.jpeg", mImageLoader);
        bg.setVisibility(View.VISIBLE);
        // Пустые массивы в аргументах нужны чтобы не было NPE в swapCursor.
        mNaviAdapter = new NavAdapter(
                this,
                R.layout.drawer_list_item,
                null,
                new String[]{},
                new int[]{}, 0);
        mDrawerList.setAdapter(mNaviAdapter);
        mDrawerList.setOnItemClickListener(mOnNavigationClickListener);
    }


}
