package ru.rutube.RutubeApp.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import ru.rutube.RutubeAPI.models.Auth;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeFeed.ui.FeedActivity;
import ru.rutube.RutubeFeed.ui.FeedFragment;

import java.util.HashMap;

public class StartActivity extends FeedActivity implements LoginFragment.LoginListener {
    private static final String LOG_TAG = StartActivity.class.getName();
    protected static final int LOGIN_REQUEST_CODE = 1;
    private static final String SELECTED_TAB = "SelectedTab";
    private static final String TAB_EDITORS = "editors";
    private static final String TAB_MY_VIDEO = "my_video";
    private static final String TAB_SUBSCRIPTIONS = "subscription";
    private static final String TAB_LOGIN = "login";
    private String selectedTab;
    private Auth auth;
    private HashMap<String, ActionBar.Tab> tabs = new HashMap<String, ActionBar.Tab>();
    private HashMap<String, Fragment> fragments = new HashMap<String, Fragment>();

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(R.layout.start_activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //layoutRedId = R.layout.start_activity;
        auth = Auth.from(this);
        getIntent().setData(buildFeedUri(R.string.editors_uri));
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            selectedTab = savedInstanceState.getString(SELECTED_TAB);
        else
            selectedTab = TAB_EDITORS;
//        setContentView(ru.rutube.RutubeFeed.R.layout.feed_activity);
        //processIntentUri();
        initTabs();
    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            Log.d(LOG_TAG, "Selected tab: " + selectedTab);
            Fragment f = getFragmentManager().findFragmentByTag(selectedTab);
            Fragment fragment = selectFeed(tag);
            Log.d(LOG_TAG, "Changed to tab: " + selectedTab);
            if (fragment != null) {
                if (f != null) {
                    ft.remove(f);

                    ft.add(R.id.feed_fragment_container, fragment, String.valueOf(tag));
                    Log.d(LOG_TAG, "Fragment replaced");

                } else {
                    ft.add(R.id.feed_fragment_container, fragment, String.valueOf(tag));
                    Log.d(LOG_TAG, "Fragment added");
                }
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    private Fragment selectFeed(Object tag) {
        Bundle bundle = new Bundle();
        int id = R.string.editors_uri;
        selectedTab = (String)tag;
        if (tag.equals(TAB_EDITORS)) {
            id = R.string.editors_uri;
        } else if (!auth.checkLoginState()) {

            selectedTab = TAB_LOGIN;
            Fragment loginFragment = fragments.get(selectedTab);
            if (loginFragment == null) {
             loginFragment = new LoginFragment();
                fragments.put(selectedTab, loginFragment);
            }
            return loginFragment;
        }
        if (tag.equals(TAB_MY_VIDEO)) {
            id = R.string.my_video_uri;
        }
        if (tag.equals(TAB_SUBSCRIPTIONS)) {
            id = R.string.subscription_uri;
        }
        bundle.putParcelable(Constants.Params.FEED_URI, buildFeedUri(id));
        Fragment fragment = fragments.get(selectedTab);
        if (fragment == null) {
            Log.d(LOG_TAG, "Creting new fragment");
            fragment = new FeedFragment();
            fragment.setArguments(bundle);
            fragments.put(selectedTab, fragment);
        }
        else {
            Log.d(LOG_TAG, "Got fragment from cache");
        }
        return fragment;

    }

    private void initTabs() {
        Log.d(LOG_TAG, "initTabs");
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        addTab(getString(ru.rutube.RutubeFeed.R.id.editors_feed), TAB_EDITORS);
        addTab(getString(ru.rutube.RutubeFeed.R.id.my_video), TAB_MY_VIDEO);
        addTab(getString(ru.rutube.RutubeFeed.R.id.subscriptions), TAB_SUBSCRIPTIONS);
        getActionBar().selectTab(tabs.get(selectedTab));
    }

    private ActionBar.Tab addTab(String title, String tag) {
        ActionBar.Tab tab = getActionBar().newTab();
        tab.setText(title);
        tab.setTabListener(tabListener);
        tab.setTag(tag);
        getActionBar().addTab(tab);
        tabs.put(tag, tab);
        return tab;
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
            processCurrentTab();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_TAB, selectedTab);
    }

    private void processCurrentTab() {
        Object tag = getActionBar().getSelectedTab().getTag();
        Log.d(LOG_TAG, "processCurrentTab " + String.valueOf(tag));
        Fragment fragment = selectFeed(tag);
        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.feed_fragment_container, fragment, tag.toString());
            ft.commit();
        }
    }

    @Override
    public void onLoginResult(int result) {
        if (result == RESULT_OK)
            processCurrentTab();
    }
}
