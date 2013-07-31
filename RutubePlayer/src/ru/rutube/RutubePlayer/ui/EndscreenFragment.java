package ru.rutube.RutubePlayer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.rutube.RutubePlayer.R;

/**
 * Created by tumbler on 30.07.13.
 */
public class EndscreenFragment extends Fragment {

    private final String LOG_TAG = getClass().getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "OnCreateView");
        return inflater.inflate(R.layout.endscreen_fragment, container, false);
    }
}
