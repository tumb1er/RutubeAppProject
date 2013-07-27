package ru.rutube.RutubeApp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.rutube.RutubeApp.R;
import ru.rutube.RutubePlayer.ui.PlayerFragment;

/**
 * Created by tumbler on 27.07.13.
 */
public class VitamioPlayerFragment extends PlayerFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }
}
