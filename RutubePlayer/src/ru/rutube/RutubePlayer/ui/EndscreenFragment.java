package ru.rutube.RutubePlayer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.rutube.RutubePlayer.R;

/**
 * Created by tumbler on 30.07.13.
 */
public class EndscreenFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.endscreen_fragment, container, false);
    }
}
