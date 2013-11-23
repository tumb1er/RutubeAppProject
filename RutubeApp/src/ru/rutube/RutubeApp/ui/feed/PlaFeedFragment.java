package ru.rutube.RutubeApp.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.internal.PLA_AdapterView;

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
    private MultiColumnListView sgView;

    private PLA_AdapterView.OnItemClickListener onItemClickListener = new PLA_AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
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
        sgView = (MultiColumnListView)result.findViewById(R.id.feed_item_list);
        assert sgView != null;
        sgView.setOnItemClickListener(onItemClickListener);
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
    public void onItemClick(FeedCursorAdapter.ClickTag position, String viewTag) {
        MainApplication.cardClick(getActivity(), viewTag);
        super.onItemClick(position, viewTag);
    }
}
