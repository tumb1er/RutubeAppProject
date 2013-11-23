package ru.rutube.RutubeApp.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.internal.PLA_AdapterView;

import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;

/**
 * Created by tumbler on 18.08.13.
 */
public class SearchFeedFragment extends PlaFeedFragment {
    private static final String LOG_TAG = SearchFeedFragment.class.getName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v!= null;
        TextView tv = (TextView)v.findViewById(R.id.empty);
        tv.setText(getResources().getText(R.string.nothing_found));
        return v;
    }
}
