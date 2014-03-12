package ru.rutube.RutubeFeed.ui;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContentProvider;
import ru.rutube.RutubeAPI.content.FeedContract;
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
    }


}
