package ru.rutube.RutubeApp.ui.player;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubePlayer.ui.PlayerFragment;

/**
 * Created by tumbler on 27.07.13.
 */


/**
 * Переопределяет PlayerFragment для интеграции VitamioBundle
 * и vitamio.widget.VideoView в частности.
 */
public class VitamioPlayerFragment extends PlayerFragment
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = VitamioPlayerFragment.class.getName();
    private ProgressBar mLoadProgressBar;
    private TextView mEmptyTextView;
    private VideoView mVideoView;
    private VitamioMediaController vitamioMediaController;
    private String mVideoTitle;

    @Override
    public int getCurrentOffset() {
        return (int)mVideoView.getCurrentPosition();
    }

    @Override
    public void stopPlayback() {
        Log.d(LOG_TAG, "stopPlayback");
        mVideoView.setVideoURI(null);
        mVideoView.stopPlayback();
    }


    @Override
    public void seekTo(int millis) {
        Log.d(LOG_TAG, "Seek to: " + String.valueOf(millis));
        mVideoView.seekTo(millis);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void pauseVideo() {
        Log.d(LOG_TAG, "pauseVideo");
        mVideoView.pause();
    }

    @Override
    protected void initVideoView() {
        Activity activity = getActivity();
        if (!LibsChecker.checkVitamioLibs(activity))
            return;
        View view = getView();
        assert view != null;
        mLoadProgressBar = (ProgressBar) view.findViewById(R.id.load);
        mEmptyTextView = (TextView) view.findViewById(R.id.empty);
        vitamioMediaController = new VitamioMediaController(activity);
        mVideoView = (VideoView) view.findViewById(R.id.surface_view);
        mVideoView.setMediaController(vitamioMediaController);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.pause();
        mVideoView.requestFocus();
    }

    public void setLoading() {
        mLoadProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    public void setLoadingCompleted() {
        mLoadProgressBar.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    @Override
    public void setVideoUri(Uri uri) {
        mVideoView.setVideoURI(uri);
    }

    @Override
    public void setVideoTitle(String title) {
        mVideoTitle = title;
        vitamioMediaController.setFileName(mVideoTitle);
    }

    @Override
    public void startVideoPlayback() {
        mVideoView.setVideoURI(mStreamUri);
        Log.d(LOG_TAG, "startVideoPlayback: " + String.valueOf(mVideoTitle));
        vitamioMediaController.setFileName(mVideoTitle);
        mVideoView.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
        mController.onCompletion();
    }

    @Override
    protected void toggleMediaController(boolean visible) {
        if (visible)
            vitamioMediaController.show();
        else
            vitamioMediaController.hide();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(LOG_TAG, "onError");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        mVideoView.pause();
        vitamioMediaController.setFileName(mVideoTitle);
        mController.onViewReady();
    }
}
