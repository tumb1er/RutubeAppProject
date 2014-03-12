package ru.rutube.RutubeFeed.ui;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ScrollingTabContainerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.ShowcaseTab;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.ShowcaseController;
import ru.rutube.RutubeFeed.data.ShowcaseTabsViewPagerAdapter;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseFragment extends Fragment implements ShowcaseController.ShowcaseView {
    private static final String CONTROLLER = "controller";
    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;

    private ViewPager mViewPager;
    private ScrollingTabContainerView mTabBar;
    private ShowcaseController mController;
    private ActionBar mActionBar;
    private ShowcaseTab[] mTabs;

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
        TextView tv = (TextView)view.findViewById(R.id.uriTextView);
        Uri uri = getArguments().getParcelable(Constants.Params.SHOWCASE_URI);
        tv.setText(String.valueOf(uri));
        return view;
    }

    public void initTabBar(View view) {
        LinearLayout rootView = (LinearLayout)view.findViewById(R.id.showcase_root);
        mTabBar = new ScrollingTabContainerView(getActivity());
        mTabBar.setVisibility(View.VISIBLE);
        int height = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_stacked_max_height);
        mTabBar.setContentHeight(height);
        rootView.addView(mTabBar, 0);
        if (mTabs != null)
            initTabs(mTabs);
    }

    @Override
    public void initTabs(ShowcaseTab[] tabs) {
        mTabs = tabs;
        if (D) Log.d(LOG_TAG, "init tabs");
        mTabBar.removeAllTabs();
        for (ShowcaseTab item: tabs) {
            ActionBar.Tab tab = createTab(item);
            mTabBar.addTab(tab, false);
        }
        mTabBar.setTabSelected(0);
    }

    @Override
    public PagerAdapter getPagerAdapter() {
        return mViewPager.getAdapter();
    }


    @Override
    public void initAdapter() {
        if (D)Log.d(LOG_TAG, "initAdapter");
        ShowcaseTabsViewPagerAdapter adapter = new ShowcaseTabsViewPagerAdapter(getActivity(),
                getFragmentManager(), null);
        mViewPager.setAdapter(adapter);
    }

    private ActionBar.Tab createTab(ShowcaseTab item) {
        ActionBar.Tab tab = mActionBar.newTab();
        tab.setText(item.getName());
        tab.setTag(item.getId());
        return tab;
    }
}
