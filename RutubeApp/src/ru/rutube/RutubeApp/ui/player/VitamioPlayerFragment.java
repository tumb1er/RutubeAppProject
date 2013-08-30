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
import ru.rutube.RutubeAPI.BuildConfig;
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
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = VitamioPlayerFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    protected ProgressBar mLoadProgressBar;
    protected TextView mEmptyTextView;
    protected VideoView mVideoView;
    protected VitamioMediaController vitamioMediaController;
    protected String mVideoTitle;

    //
    // переопределенные методы из Fragment
    //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    //
    // Обработчики событий MediaPlayer
    //

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (D) Log.d(LOG_TAG, "onCompletion");
        mController.onCompletion();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (D) Log.d(LOG_TAG, "onPrepared");
        mVideoView.pause();
        vitamioMediaController.setFileName(mVideoTitle);
        mController.onViewReady();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (D) Log.d(LOG_TAG, "onError");
        return false;
    }

    //
    // Переопределенные методы работы с VideoView
    //

    @Override
    public int getCurrentOffset() {
        return (int) mVideoView.getCurrentPosition();
    }

    @Override
    public void stopPlayback() {
        if (D) Log.d(LOG_TAG, "stopPlayback");
        mVideoView.setVideoURI(null);
        mVideoView.stopPlayback();
    }

    @Override
    public void seekTo(int millis) {
        if (D) Log.d(LOG_TAG, "Seek to: " + String.valueOf(millis));
        mVideoView.seekTo(millis);
    }

    @Override
    public void pauseVideo() {
        if (D) Log.d(LOG_TAG, "pauseVideo");
        mVideoView.pause();
    }

    @Override
    public void setVideoUri(Uri uri) {
        mStreamUri = uri;
        if (mVideoView != null)
            mVideoView.setVideoURI(uri);
    }

    @Override
    public void setVideoTitle(String title) {
        mVideoTitle = title;
        if (vitamioMediaController != null)
            vitamioMediaController.setFileName(mVideoTitle);
    }

    @Override
    public void startVideoPlayback() {
        mVideoView.setVideoURI(mStreamUri);
        mVideoView.start();
    }

    @Override
    public void setLoading() {
        mLoadProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    @Override
    public void setLoadingCompleted() {
        mLoadProgressBar.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    @Override
    protected void toggleMediaController(boolean visible) {
        if (visible)
            vitamioMediaController.show();
        else
            vitamioMediaController.hide();
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
        vitamioMediaController.setFileName(mVideoTitle);
    }
}
