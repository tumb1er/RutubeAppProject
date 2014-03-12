package ru.rutube.RutubeFeed.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.internal.widget.ScrollingTabContainerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.ShowcaseController;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseFragment extends Fragment implements ShowcaseController.ShowcaseView {
    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;

    private ViewPager mViewPager;
    private ScrollingTabContainerView mTabBar;
    private ShowcaseController mController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        Uri showcaseUri = args.getParcelable(Constants.Params.SHOWCASE_URI);
        mController = new ShowcaseController(showcaseUri);
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
    }
}
