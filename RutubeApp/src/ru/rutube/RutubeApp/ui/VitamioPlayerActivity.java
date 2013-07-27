package ru.rutube.RutubeApp.ui;

import android.util.Log;

import ru.rutube.RutubeApp.R;
import ru.rutube.RutubePlayer.ui.PlayerActivity;

/**
 * Created by tumbler on 27.07.13.
 */
public class VitamioPlayerActivity extends PlayerActivity {
    @Override
    public void setContentView(int layoutResID) {
        Log.d(getClass().getName(), "Vitamio!");
        super.setContentView(R.layout.player_activity);
    }
}
