package ru.rutube.RutubeApp.ui.player;

import android.util.Log;

import ru.rutube.RutubeApp.R;
import ru.rutube.RutubePlayer.ui.PlayerActivity;

/**
 * Created by tumbler on 27.07.13.
 */


/**
 * Наследует PlayerActivity, переопределяя через layout фрагмент на VitamioPlayerFragment
 */
public class VitamioPlayerActivity extends PlayerActivity {


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.player_activity);
    }
}
