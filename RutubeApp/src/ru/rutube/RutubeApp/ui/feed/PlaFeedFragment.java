package ru.rutube.RutubeApp.ui.feed;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.etsy.android.grid.StaggeredGridView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;

/**
 * Created by tumbler on 18.08.13.
 */
public class PlaFeedFragment extends ru.rutube.RutubeFeed.ui.FeedFragment {
    private static final String LOG_TAG = PlaFeedFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private View mLoader;
    private View mEmptyList;
    private StaggeredGridView sgView;

    protected String mStatsFeedTag = "feed_fragment";

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            MainApplication.cardClick(getActivity());
            mController.onListItemClick(position);
        }
    };

    public PlaFeedFragment(FeedImpl feedImpl) {
        super(feedImpl);
    }

    public PlaFeedFragment() {}

    public void setStatsFeedTag(String tag) {
        mStatsFeedTag = tag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (D) Log.d(LOG_TAG, "onCreateView");
        View result = inflater.inflate(R.layout.pla_feed_fragment, container, false);
        assert result != null;
        sgView = (StaggeredGridView)result.findViewById(R.id.feed_item_list);
        assert sgView != null;
        sgView.setOnItemClickListener(onItemClickListener);
        mLoader = result.findViewById(R.id.loader);
        mEmptyList = result.findViewById(R.id.empty);
        return result;
    }

    @Override
    public ListAdapter getListAdapter() {
        return sgView.getAdapter();
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        sgView.setAdapter(adapter);
    }

    @Override
    public boolean onItemClick(FeedCursorAdapter.ClickTag position, String viewTag) {
        MainApplication.cardClick(getActivity(), viewTag);
        return super.onItemClick(position, viewTag);
    }

    @Override
    public void openPlayer(Uri uri, Uri thumbnailUri) {
        MainApplication.playerOpened(getActivity(), mStatsFeedTag);
        super.openPlayer(uri, thumbnailUri);
    }

    @Override
    public void setRefreshing() {
        super.setRefreshing();
        mLoader.setVisibility(View.VISIBLE);
        mEmptyList.setVisibility(View.GONE);
    }

    @Override
    public void doneRefreshing() {
        super.doneRefreshing();
        mLoader.setVisibility(View.GONE);
        mEmptyList.setVisibility(View.VISIBLE);
    }


    @Override
    public void setSelectedItem(final int position) {
        try {
            sgView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sgView.setSelection(position);
                    View v = sgView.getChildAt(position);
                    if (v != null)
                        v.requestFocus();
                }
            }, 1);


        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getCurrentPosition() {
        try{
            return sgView.getFirstVisiblePosition();
        } catch (NullPointerException ignored) {
            return 0;
        }
    }
}
