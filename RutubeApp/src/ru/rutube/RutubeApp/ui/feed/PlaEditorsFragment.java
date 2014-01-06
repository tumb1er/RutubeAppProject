package ru.rutube.RutubeApp.ui.feed;

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
public class PlaEditorsFragment extends ru.rutube.RutubeFeed.ui.EditorsFeedFragment {
    private static final String LOG_TAG = PlaEditorsFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    public static final String STATE_POSITION = "position";
    private StaggeredGridView sgView;
    private int mInitialPos = 0;

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            MainApplication.cardClick(getActivity());
            mController.onListItemClick(position);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (D) Log.d(LOG_TAG, "onCreateView");
        View result = inflater.inflate(R.layout.pla_feed_fragment, container, false);
        assert result != null;
        sgView = (StaggeredGridView)result.findViewById(R.id.feed_item_list);
        assert sgView != null;
        sgView.setOnItemClickListener(onItemClickListener);
        if (savedInstanceState != null) {
            mInitialPos = savedInstanceState.getInt(STATE_POSITION);
        }
        return result;
    }

    @Override
    public ListAdapter getListAdapter() {
        return sgView.getAdapter();
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (D) Log.d(LOG_TAG, "Adapter: " + String.valueOf(sgView.getAdapter()));
        sgView.setAdapter(adapter);
        if (D) Log.d(LOG_TAG,"setListAdapter pos " + String.valueOf(mInitialPos));
        setSelectedItem(mInitialPos);
    }

    @Override
    public boolean onItemClick(FeedCursorAdapter.ClickTag position, String viewTag) {
        MainApplication.cardClick(getActivity(), viewTag);
        return super.onItemClick(position, viewTag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_POSITION, getCurrentPosition());
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
