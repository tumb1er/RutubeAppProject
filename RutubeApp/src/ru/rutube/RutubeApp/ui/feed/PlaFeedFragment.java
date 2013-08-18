package ru.rutube.RutubeApp.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.internal.PLA_AdapterView;

import ru.rutube.RutubeApp.R;

/**
 * Created by tumbler on 18.08.13.
 */
public class PlaFeedFragment extends ru.rutube.RutubeFeed.ui.FeedFragment {
    private static final String LOG_TAG = PlaFeedFragment.class.getName();
    private MultiColumnListView sgView;

    private PLA_AdapterView.OnItemClickListener onItemClickListener = new PLA_AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
            mController.onListItemClick(position);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d(LOG_TAG, "onCreateView");
        sgView = (MultiColumnListView)inflater.inflate(R.layout.pla_feed_fragment, container, false);
        assert sgView != null;
        sgView.setOnItemClickListener(onItemClickListener);
        return sgView;
    }

    @Override
    public ListAdapter getListAdapter() {
        return sgView.getAdapter();
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        sgView.setAdapter(adapter);
    }

}
