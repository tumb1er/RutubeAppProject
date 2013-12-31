package ru.rutube.RutubeApp.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;


import java.util.ArrayList;

import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeApp.BuildConfig;
import ru.rutube.RutubeApp.ui.StartActivity;

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
        private final Class<?> clss;
        private final Bundle args;

        TabInfo(Class<?> _class, Bundle _args) {
            clss = _class;
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

    public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
        TabInfo info = new TabInfo(clss, args);
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
        return Fragment.instantiate(mActivity, info.clss.getName(), info.args);
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

