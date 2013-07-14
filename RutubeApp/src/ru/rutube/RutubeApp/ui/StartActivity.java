package ru.rutube.RutubeApp.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.MainPageController;
import ru.rutube.RutubeFeed.ui.FeedActivity;
import ru.rutube.RutubeFeed.ui.FeedFragment;

import java.util.HashMap;

public class StartActivity extends Activity implements MainPageController.MainPageView, ActionBar.TabListener {
    private static final String LOG_TAG = StartActivity.class.getName();
    private static final String CONTROLLER = "controller";

    private MainPageController mController;
    private HashMap<String, ActionBar.Tab> mTabMap = new HashMap<String, ActionBar.Tab>();
    private HashMap<String, Fragment> mFragmentMap = new HashMap<String, Fragment>();
    private FragmentTransaction mFragmentTransaction;
    private Fragment mCurrentFragment;

    private static final String SELECTED_TAB = "SelectedTab";
    protected static final int LOGIN_REQUEST_CODE = 1;
    private static final String TAB_LOGIN = "login";
    private String selectedTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mController = savedInstanceState.getParcelable(CONTROLLER);
        else
            mController = new MainPageController();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        mController.attach(this, this);
        initTabs();
    }

    public void selectTab(String tag) {
        getActionBar().selectTab(mTabMap.get(selectedTab));
    }

    private void initTabs() {
        Log.d(LOG_TAG, "initTabs");
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mController.initTabs();
        Log.d(LOG_TAG, "initTabs done");

    }

    public void addFeedTab(String title, String tag) {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText(title);
        tab.setTabListener(this);
        tab.setTag(tag);
        actionBar.addTab(tab);
        mTabMap.put(tag, tab);
    }

    @Override
    public void showFeedFragment(String tag, Uri feedUri) {
        Log.d(LOG_TAG, "showFeedFragment " + tag);
        Fragment fragment = mFragmentMap.get(tag);
        Boolean isNewFragment = false;
        if (fragment == null) {
            Log.d(LOG_TAG, "creating fragment for " + feedUri.toString());
            fragment = createFeedFragment(feedUri);
            isNewFragment = true;
            mFragmentMap.put(tag, fragment);
        }

        if (mFragmentTransaction != null){
            Log.d(LOG_TAG, "ft not null");
            if (mCurrentFragment != null){
                Log.d(LOG_TAG, "hiding");
                mFragmentTransaction.hide(mCurrentFragment);
            }
            if (isNewFragment)
            {
                mFragmentTransaction.add(R.id.feed_fragment_container, fragment);
                Log.d(LOG_TAG, "Added fragment");
            }
            else {
                mFragmentTransaction.show(fragment);
                Log.d(LOG_TAG, "Showed fragment");
            }
        }
        else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.feed_fragment_container, fragment, tag);
            ft.commit();
        }
        mCurrentFragment = fragment;
        Log.d(LOG_TAG, "showFeedFragment done");
    }

    private Fragment createFeedFragment(Uri feedUri) {
        Fragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.Params.FEED_URI, feedUri);
        fragment.setArguments(args);
        return fragment;
    }

    private Uri buildFeedUri(int resourceId) {
        Uri.Builder builder = Uri.parse(getString(R.string.base_uri)).buildUpon();
        builder.appendEncodedPath(getString(resourceId));
        return builder.build();
    }

    private void processIntentUri() {
        Intent intent = new Intent(this, FeedActivity.class);
        intent.setData(buildFeedUri(R.string.editors_uri));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            //processCurrentTab();
            // TODO: обработка процесса авторизации
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_TAB, selectedTab);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mFragmentTransaction = fragmentTransaction;
        String tag = (String)tab.getTag();
        mController.onTabSelected(tag);
        mFragmentTransaction = null;

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

//    @Override
//    public void onLoginResult(int result) {
//        if (result == RESULT_OK)
//            processCurrentTab();
//    }
}
