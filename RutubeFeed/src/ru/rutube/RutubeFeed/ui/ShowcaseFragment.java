package ru.rutube.RutubeFeed.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ScrollingTabContainerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viewpagerindicator.TabPageIndicator;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.ShowcaseTab;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.ShowcaseController;
import ru.rutube.RutubeFeed.data.ShowcaseTabsViewPagerAdapter;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseFragment extends Fragment implements ShowcaseController.ShowcaseView,
        ActionBar.TabListener, ViewPager.OnPageChangeListener{
    private static final String CONTROLLER = "controller";
    private static final String LOG_TAG = ShowcaseFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    private ViewPager mViewPager;
    private ScrollingTabContainerView mTabBar;
    private ShowcaseController mController;
    private ActionBar mActionBar;
    private ShowcaseTab[] mTabs;
    private TabPageIndicator mTabPageIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (D)Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        Uri showcaseUri = args.getParcelable(Constants.Params.SHOWCASE_URI);
        int showcaseId = args.getInt(Constants.Params.SHOWCASE_ID, 0);
        if (mController == null) {
            if (savedInstanceState == null) {
                if (D) Log.d(LOG_TAG, "new controller");
                mController = new ShowcaseController(showcaseUri, showcaseId);
            } else {
                if (D) Log.d(LOG_TAG, "controller from parcel");
                mController = savedInstanceState.getParcelable(CONTROLLER);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (D)Log.d(LOG_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (D)Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putParcelable(CONTROLLER, mController);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.d(LOG_TAG, "Starting showcase fragment");
        mController.attach(getActivity(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mController.detach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(LOG_TAG, "onCreateView");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.showcase_fragment, container, false);
        assert view != null;
        mViewPager = (ViewPager)view.findViewById(R.id.view_pager);
        initTabBar(view);
        return view;
    }

    public void initTabBar(View view) {
        mTabPageIndicator = (TabPageIndicator)view.findViewById(R.id.titles);
    }


    @Override
    public PagerAdapter getPagerAdapter() {
        return mViewPager.getAdapter();
    }

    @Override
    public void initAdapter() {
        if (D)Log.d(LOG_TAG, "initAdapter");
        ShowcaseTabsViewPagerAdapter adapter = new ShowcaseTabsViewPagerAdapter(getActivity(),
                getChildFragmentManager(), null);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(this);
        mTabPageIndicator.setViewPager(mViewPager);
    }

    @Override
    public void notifyPagerIndicator() {
        mTabPageIndicator.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class TabInfo{
        public int id;
        public int pos;
    }

    private ActionBar.Tab createTab(ShowcaseTab item, int pos) {
        if (D)Log.d(LOG_TAG, "Create tab");
        ActionBar.Tab tab = mActionBar.newTab();
        tab.setText(item.getName());
        TabInfo info = new TabInfo();
        info.id = item.getId();
        info.pos = pos;
        tab.setTag(info);
        tab.setTabListener(this);
        return tab;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (D) Log.d(LOG_TAG, "onTabSelected");
        TabInfo info = (TabInfo)tab.getTag();
        if (D) Log.d(LOG_TAG, "onTabSelected: " + String.valueOf(info.pos));
        mViewPager.setCurrentItem(info.pos);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (D) Log.d(LOG_TAG, "onTabUnselected: ");

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (D) Log.d(LOG_TAG, "onTabReselected");
        TabInfo info = (TabInfo)tab.getTag();
        if (D) Log.d(LOG_TAG, "onTabReselected: " + String.valueOf(info.pos));
        mViewPager.setCurrentItem(info.pos);
    }
}
