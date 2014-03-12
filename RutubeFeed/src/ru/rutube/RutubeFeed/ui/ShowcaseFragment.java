package ru.rutube.RutubeFeed.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.internal.widget.ScrollingTabContainerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeFeed.R;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseFragment extends Fragment {
    private ViewPager mViewPager;
    private ScrollingTabContainerView mTabBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.showcase_fragment, container, false);
        assert view != null;
        mViewPager = (ViewPager)view.findViewById(R.id.view_pager);
        LinearLayout rootView = (LinearLayout)view.findViewById(R.id.showcase_root);
        mTabBar = new ScrollingTabContainerView(getActivity());
        mTabBar.setVisibility(View.VISIBLE);
        int height = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_stacked_max_height);
        mTabBar.setContentHeight(height);
        rootView.addView(mTabBar, 0);
        TextView tv = (TextView)view.findViewById(R.id.uriTextView);
        Uri uri = getArguments().getParcelable(Constants.Params.SHOWCASE_URI);
        tv.setText(String.valueOf(uri));
        return view;
    }
}
