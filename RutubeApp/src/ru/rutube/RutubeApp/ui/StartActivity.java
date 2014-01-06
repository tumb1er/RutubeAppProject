package ru.rutube.RutubeApp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.MainPageController;
import ru.rutube.RutubeApp.data.MainTabsAdapter;
import ru.rutube.RutubeApp.ui.dialog.LoginDialogFragment;
import ru.rutube.RutubeApp.ui.feed.PlaEditorsFragment;
import ru.rutube.RutubeApp.ui.feed.PlaFeedFragment;
import ru.rutube.RutubeApp.ui.feed.PlaSubscriptionsFragment;
import ru.rutube.RutubeFeed.ctrl.FeedController;
import ru.rutube.RutubeFeed.ui.EditorsFeedFragment;
import ru.rutube.RutubeFeed.ui.FeedFragment;

import java.util.HashMap;

public class StartActivity extends ActionBarActivity implements MainPageController.MainPageView {
    private static final String LOG_TAG = StartActivity.class.getName();
    private static final String CONTROLLER = "controller";
    private static final int LOGIN_REQUEST_CODE = 1;
    private static final boolean D = BuildConfig.DEBUG;

    private static final HashMap<String, Class<?>> sFragmentClassMap = new HashMap<String, Class<?>>();
    private static final HashMap<String, Integer> sFeedUriResourceIdMap = new HashMap<String, Integer>();
    static {
        sFragmentClassMap.put(MainPageController.TAB_EDITORS, PlaEditorsFragment.class);
        sFragmentClassMap.put(MainPageController.TAB_MY_VIDEO, PlaFeedFragment.class);
        sFragmentClassMap.put(MainPageController.TAB_SUBSCRIPTIONS, PlaSubscriptionsFragment.class);

        sFeedUriResourceIdMap.put(MainPageController.TAB_EDITORS, R.string.editors_uri);
        sFeedUriResourceIdMap.put(MainPageController.TAB_MY_VIDEO, R.string.my_video_uri);
        sFeedUriResourceIdMap.put(MainPageController.TAB_SUBSCRIPTIONS, R.string.subscription_uri);
    }

    private MainPageController mController;
    private HashMap<String, ActionBar.Tab> mTabMap = new HashMap<String, ActionBar.Tab>();

    ViewPager mViewPager;
    MainTabsAdapter mTabsAdapter;
    private MenuItem mLogoutItem;


    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        boolean result = super.onCreatePanelMenu(featureId, menu);
        User user = User.load(this);
        mLogoutItem = menu.findItem(ru.rutube.RutubeFeed.R.id.menu_logout);
        if (mLogoutItem != null)
            mLogoutItem.setVisible(!user.isAnonymous());
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == ru.rutube.RutubeFeed.R.id.menu_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        if (D) Log.d(LOG_TAG, "Logging out");
        mController.logout();
        mTabsAdapter.getItem(MainPageController.TAB_MY_VIDEO).logout();
        mTabsAdapter.getItem(MainPageController.TAB_SUBSCRIPTIONS).logout();
    }

    @Override
    protected void onDestroy() {
        mController.detach();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mController = savedInstanceState.getParcelable(CONTROLLER);
        else
            mController = new MainPageController();
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.feed_fragment_container);
        mViewPager.setOffscreenPageLimit(3);
        setContentView(mViewPager);

        final ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        mController.attach(this, this);
        mTabsAdapter = new MainTabsAdapter(this, mViewPager);
        mController.initTabs();
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
        mLogoutItem.setVisible(false);
        showError(getString(R.string.logouted));
    }

    @Override
    public void onLoginCanceled() {
        MainApplication.loginDialogFailed(this);
    }

    @Override
    public void onLoginSuccess() {
        mLogoutItem.setVisible(true);
        MainApplication.loginDialogSuccess(this);
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.
                setTitle(android.R.string.dialog_alert_title).
                setMessage(message).
                create().
                show();
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
