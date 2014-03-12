package ru.rutube.RutubeFeed.ui;

import android.content.res.Configuration;
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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ru.rutube.RutubeAPI.BuildConfig;
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
    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final int NAVI_LOADER = 0;

    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NaviLoaderCallbacks loaderCallbacks;
    private NavAdapter mNaviAdapter;

    private class ShowcaseFragmentCache {
        Map<Uri, Fragment> mFragmentMap = new HashMap<Uri, Fragment>();
        public Fragment getShowcaseFragment(Uri uri) {
            Fragment f = mFragmentMap.get(uri);
            if (f != null)
                return f;
            f = new ShowcaseFragment();
            Bundle args = new Bundle();
            args.putParcelable(Constants.Params.SHOWCASE_URI, uri);
            f.setArguments(args);
            mFragmentMap.put(uri, f);
            return f;
        }
    }

    private ShowcaseFragmentCache mFragmentCache = new ShowcaseFragmentCache();

    private AdapterView.OnItemClickListener mOnNavigationClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            NavAdapter.ViewHolder holder = (NavAdapter.ViewHolder)view.getTag();
            Uri showcaseUri = holder.showcaseUri;
            navigateToShowcase(showcaseUri);
            mNaviAdapter.setCurrentItemPosition(i);
            mDrawerLayout.closeDrawers();
            mNaviAdapter.notifyDataSetChanged();
        }
    };

    public void navigateToShowcase(Uri showcaseUri) {
        TextView tv = (TextView)findViewById(R.id.uriTextView);
        tv.setText(showcaseUri.toString());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment f = mFragmentCache.getShowcaseFragment(showcaseUri);
        ft.replace(R.id.content_placeholder, f);
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
            CursorAdapter adapter = (CursorAdapter)mDrawerList.getAdapter();
            if (adapter != null)
                adapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            CursorAdapter adapter = (CursorAdapter)mDrawerList.getAdapter();
            if (adapter != null)
                adapter.swapCursor(null);

        }
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
        initNavigationAdapter();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Uri uri = getIntent().getData();
        navigateToShowcase(uri);

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
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        if (D) Log.d(LOG_TAG, "Set nav adapter");
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
