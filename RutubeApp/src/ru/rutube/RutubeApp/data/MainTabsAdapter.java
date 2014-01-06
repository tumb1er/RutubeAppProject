package ru.rutube.RutubeApp.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;

import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeApp.BuildConfig;
import ru.rutube.RutubeApp.ui.StartActivity;
import ru.rutube.RutubeApp.ui.feed.PlaFeedFragmentFactory;
import ru.rutube.RutubeFeed.feed.FeedFragmentFactory;
import ru.rutube.RutubeFeed.ui.FeedFragment;

/**
 * Created by tumbler on 30.12.13.
 */
public class MainTabsAdapter extends FragmentStatePagerAdapter
    implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = MainTabsAdapter.class.getName();
    private final StartActivity mActivity;
    private final ActionBar mActionBar;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private final HashMap<String, FeedFragment> mFragments = new HashMap<String, FeedFragment>();
    private final FeedFragmentFactory mFragmentFactory = new PlaFeedFragmentFactory();
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Object tag = tab.getTag();
        for (int i=0; i<mTabs.size(); i++) {
            if (mTabs.get(i) == tag) {
                mViewPager.setCurrentItem(i);
            }
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public void setCurrentItem(String current_tag) {
        for (int i=0; i<mTabs.size(); i++) {
            TabInfo info = mTabs.get(i);
            String tag = info.args.getString(Constants.Params.FEED_TITLE);
            if (current_tag.equals(tag)) {
                mViewPager.setCurrentItem(i);
            }
        }
    }

    static final class TabInfo {
        private final int feedType;
        private final Bundle args;

        TabInfo(int _feedType, Bundle _args) {
            feedType = _feedType;
            args = _args;
        }
    }


    public MainTabsAdapter(StartActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());
        mActivity = activity;
        mActionBar = activity.getSupportActionBar();
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, int feedType, Bundle args) {
        TabInfo info = new TabInfo(feedType, args);
        tab.setTag(info);
        tab.setTabListener(this);
        mTabs.add(info);
        mActionBar.addTab(tab);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo info = mTabs.get(position);
        FeedFragment f = mFragmentFactory.getFeedFragment(info.feedType);
        f.setArguments(info.args);
        String tag = info.args.getString(Constants.Params.FEED_TITLE);
        mFragments.put(tag, f);
        return f;
    }

    public FeedFragment getItem(String tag) {
        return mFragments.get(tag);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (D) Log.d(LOG_TAG, "onPageSelected: " + String.valueOf(position));
        if (D) {
            TabInfo info = mTabs.get(position);
            String tag = info.args.getString(Constants.Params.FEED_TITLE);
            Log.d(LOG_TAG, "onPageSelected: " + tag);
            mActivity.onTabSelected(tag);
        }
        mActionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}

