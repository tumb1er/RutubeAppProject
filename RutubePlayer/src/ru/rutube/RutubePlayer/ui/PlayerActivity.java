package ru.rutube.RutubePlayer.ui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubePlayer.R;


/**
 * Активити фулскрин-плеера.
 * Возможен старт по intent-action: ru.rutube.player.play c Uri видео вида:
 * http://rutube.ru/video/<video_id>/
 */
public class PlayerActivity extends FragmentActivity implements PlayerFragment.PlayerStateListener,
EndscreenFragment.ReplayListener {
    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;
    private PlayerFragment mPlayerFragment;
    private EndscreenFragment mEndscreenFragment;

    @Override
    public void replay() {
        mPlayerFragment.replay();
    }

    @Override
    public void onPlay() {
        toggleEndscreen(false);
    }

    @Override
    public void onFail() {
        finish();
    }

    private void toggleEndscreen(boolean visible) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (visible)
            ft.show(mEndscreenFragment);
        else
            ft.hide(mEndscreenFragment);
        ft.commit();
    }

    @Override
    public void onComplete() {
        if (D) Log.d(LOG_TAG, "onComplete");
        toggleEndscreen(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (D) Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        initWindow();
        // Запрашиваем горизонтальное расположение экрана, если он перевернется,
        // то активити пересоздатся и вся цепочка автостарта воспроизведения обломится,
        // поэтому не задаем ContentView ДО готовности ориентации экрана.
        int orientation = getScreenOrientation();
        if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setContentView(R.layout.player_activity);
            init();
        }
    }

    private void init() {
        mPlayerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment);
        assert mPlayerFragment != null;
        mPlayerFragment.setPlayerStateListener(this);
        mEndscreenFragment = (EndscreenFragment) getSupportFragmentManager().findFragmentById(R.id.endscreen_fragment);
        assert mEndscreenFragment != null;
        mEndscreenFragment.setReplayListener(this);
        toggleEndscreen(false);
    }

    /**
     * фулскрин без заголовка окна
     */
    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * Определяет ориентацию экрана на основе значений высоты и ширины экрана
     * @return Configuration.ORIENTATION
     */
    private int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
}
