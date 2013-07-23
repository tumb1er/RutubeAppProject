package ru.rutube.RutubePlayer.ui;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import ru.rutube.RutubePlayer.R;

public class PlayerActivity extends FragmentActivity implements OnCompletionListener, OnErrorListener, OnPreparedListener {

    public static String PLAYER_TAG = "Rutube Player";

    private VideoView mVideoView;
    private ProgressBar load;
    private TextView empty;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.player_activity);
        
        if (!LibsChecker.checkVitamioLibs(this))
            return;

        initializePlayer();

        ServiceVideoImpl serviceVideo = new ServiceVideoImpl(this) {
            @Override
            public void onResult(Uri streamUri) {
                mVideoView.setVideoURI(streamUri);
            }
        };
    }

    private void initializePlayer() {
        load = (ProgressBar) this.findViewById(R.id.load);
        empty = (TextView) this.findViewById(R.id.empty);
        mVideoView = (VideoView) this.findViewById(R.id.surface_view);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.requestFocus();
        loading();
    }

    private void loading() {
        load.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
    }

    private void loadingComplete(MediaPlayer mp) {
        load.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);

        mVideoView.start();
        mVideoView.resume();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(PLAYER_TAG, "Prepared");
        loadingComplete(mp);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(PLAYER_TAG, "Complete");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(PLAYER_TAG, "Error");
        return false;
    }
}
