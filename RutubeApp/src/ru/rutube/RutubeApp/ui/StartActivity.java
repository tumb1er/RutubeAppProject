package ru.rutube.RutubeApp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ScrollingTabContainerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.MainPageController;
import ru.rutube.RutubeApp.data.MainTabsAdapter;
import ru.rutube.RutubeApp.data.NavAdapter;
import ru.rutube.RutubeApp.ui.dialog.LoginDialogFragment;
import ru.rutube.RutubeFeed.feed.FeedFragmentFactory;
import ru.rutube.RutubeFeed.ui.FeedFragment;

import java.util.HashMap;

public class StartActivity extends ActionBarActivity implements MainPageController.MainPageView {
    private static final String LOG_TAG = StartActivity.class.getName();
    private static final String CONTROLLER = "controller";
    private static final int LOGIN_REQUEST_CODE = 1;
    private static final boolean D = BuildConfig.DEBUG;

    private static final HashMap<String, Integer> sFragmentClassMap = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> sFeedUriResourceIdMap = new HashMap<String, Integer>();
    static {
        sFragmentClassMap.put(MainPageController.TAB_EDITORS, FeedFragmentFactory.EDITORS);
        sFragmentClassMap.put(MainPageController.TAB_MY_VIDEO, FeedFragmentFactory.COMMON);
        sFragmentClassMap.put(MainPageController.TAB_SUBSCRIPTIONS, FeedFragmentFactory.SUBSCRIPTIONS);

        sFeedUriResourceIdMap.put(MainPageController.TAB_EDITORS, R.string.editors_uri);
        sFeedUriResourceIdMap.put(MainPageController.TAB_MY_VIDEO, R.string.my_video_uri);
        sFeedUriResourceIdMap.put(MainPageController.TAB_SUBSCRIPTIONS, R.string.subscription_uri);
    }

    private MainPageController mController;
    private HashMap<String, ActionBar.Tab> mTabMap = new HashMap<String, ActionBar.Tab>();

    ViewPager mViewPager;
    MainTabsAdapter mTabsAdapter;
    private MenuItem mLogoutItem;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ScrollingTabContainerView mTabBar;
    private ListView mDrawerList;


    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        boolean result = super.onCreatePanelMenu(featureId, menu);
        User user = User.load();
        mLogoutItem = menu.findItem(ru.rutube.RutubeFeed.R.id.menu_logout);
        if (mLogoutItem != null)
            mLogoutItem.setVisible(!user.isAnonymous());
        return result;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (id == ru.rutube.RutubeFeed.R.id.menu_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        if (D) Log.d(LOG_TAG, "Logging out");
        mController.logout();
        FeedFragment f = mTabsAdapter.getItem(MainPageController.TAB_MY_VIDEO);
        if (f!= null) f.logout();
        f = mTabsAdapter.getItem(MainPageController.TAB_SUBSCRIPTIONS);
        if (f!= null) f.logout();
    }

    @Override
    protected void onDestroy() {
        mController.detach();
        super.onDestroy();
    }

    public ScrollingTabContainerView getTabBar() {
        return mTabBar;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mController = savedInstanceState.getParcelable(CONTROLLER);
        else
            mController = new MainPageController();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
//        mViewPager = new ViewPager(this);
//        mViewPager.setId(R.id.feed_fragment_container);
//        mViewPager.setOffscreenPageLimit(3);
//        setContentView(mViewPager);
        mViewPager = (ViewPager)findViewById(R.id.content_frame);

        mTabBar = new ScrollingTabContainerView(this);
        mTabBar.setVisibility(View.VISIBLE);
//        ViewGroup.LayoutParams lp = mTabBar.getLayoutParams();
//        if (lp == null) {
//            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
//            mTabBar.setLayoutParams(lp);
//        } else {
//            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//            lp.height = ViewGroup.LayoutParams.FILL_PARENT;
//        }

        LinearLayout mTabFrame = (LinearLayout)findViewById(R.id.tabbar);
        int height = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_stacked_max_height);
        Log.d(LOG_TAG, String.format("WWW: %d", height));
        mTabBar.setContentHeight(height);
        mTabFrame.addView(mTabBar, 0);


        final ActionBar bar = getSupportActionBar();
//        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        mController.attach(this, this);
        mTabsAdapter = new MainTabsAdapter(this, mViewPager);
        mController.initTabs();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_closed  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("mTitle");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("mDrawerTitle");
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] items = {};
        mDrawerList.setAdapter(new NavAdapter(RutubeApp.getContext(), R.layout.drawer_list_item, items));
        TextView tv = (TextView)getLayoutInflater().inflate(R.layout.drawer_list_item, null);
        assert tv != null;
        tv.setText("Настройки");
        mDrawerList.addFooterView(tv);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainApplication.mainActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainApplication.activityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mController.onResume();
    }

    @Override
    public void showLoginDialog() {
        MainApplication.loginDialogStart(this);
        LoginDialogFragment.InputDialogFragmentBuilder builder = new LoginDialogFragment.InputDialogFragmentBuilder(this);
        builder.setOnDoneListener(new LoginDialogFragment.OnDoneListener() {
            @Override
            public void onFinishInputDialog(String emailText, String passwordText) {
                mController.startLoginRequests(emailText, passwordText);
            }
        }).setOnCancelListener(new LoginDialogFragment.OnCancelListener() {
            @Override
            public void onCancel() {
                mController.loginCanceled();

            }
        }).show();
    }

    @Override
    public void showError() {
        String message = getString(ru.rutube.RutubePlayer.R.string.failed_to_load_data);
        showError(message);
    }

    @Override
    public void onLogout() {
        if (mLogoutItem != null)
            mLogoutItem.setVisible(false);
        showError(getString(R.string.logouted));
    }

    @Override
    public void onLoginCanceled() {
        MainApplication.loginDialogFailed(this);
    }

    @Override
    public void onLoginSuccess() {
        if (mLogoutItem != null)
            mLogoutItem.setVisible(true);
        MainApplication.loginDialogSuccess(this);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Делает активной вкладку с тегом
     * @param tag
     */
    public void selectTab(String tag) {
        getSupportActionBar().selectTab(mTabMap.get(tag));
    }

    /**
     * Добавляет новую вкладку в таб-навигацию
     * @param title
     * @param tag
     */
    public void addFeedTab(String title, String tag) {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText(title);
        tab.setTag(tag);
        mTabMap.put(tag, tab);
        Bundle args = new Bundle();

        args.putParcelable(Constants.Params.FEED_URI,
                Uri.parse(RutubeApp.getUrl(sFeedUriResourceIdMap.get(tag))));
        args.putString(Constants.Params.FEED_TITLE, tag);
        mTabsAdapter.addTab(tab, sFragmentClassMap.get(tag), args);
    }

    /**
     * Делает активным фрагмент ленты с определенным тегом
     * @param tag тег фрагмента
     * @param feedUri uri ленты
     */
    public void showFeedFragment(String tag, Uri feedUri) {

        mTabsAdapter.setCurrentItem(tag);
        FeedFragment fragment = mTabsAdapter.getItem(tag);
        if (D) Log.d(LOG_TAG, "showFeedFragment: " + String.valueOf(fragment));
        if (fragment != null)

            fragment.checkLoadMore();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTROLLER, mController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(LOG_TAG, "onActivityResult");
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (D) Log.d(LOG_TAG, "loginSuccessful");
            mController.loginSuccessful();
        } else {
            if (D) Log.d(LOG_TAG, "smth strange");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onTabSelected(String tag) {
        mController.onTabSelected(tag);
    }

}
