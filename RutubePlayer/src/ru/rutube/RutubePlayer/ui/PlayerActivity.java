package ru.rutube.RutubePlayer.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import ru.rutube.RutubePlayer.R;

public class PlayerActivity extends Activity {
    private final String LOG_TAG = getClass().getName();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Запрашиваем горизонтальное расположение экрана, если он перевернется,
        // то активити пересоздатся и вся цепочка автостарта воспроизведения обломится,
        // поэтому не задаем ContentView ДО готовности ориентации экрана.
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.player_activity);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
}
