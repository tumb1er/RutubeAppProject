package ru.rutube.RutubeApp.ui;

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
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubePlayer.ui.PlayerFragment;

/**
 * Created by tumbler on 27.07.13.
 */
public class VitamioPlayerFragment extends PlayerFragment
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {


    private static final String LOG_TAG = VitamioPlayerFragment.class.getName();
    private ProgressBar mLoadProgressBar;
    private TextView mEmptyTextView;
    private VideoView mVideoView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
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
        mVideoView = (VideoView) view.findViewById(R.id.surface_view);
        mVideoView.setMediaController(new MediaController(activity));
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.requestFocus();
        setLoading();
    }

    private void setLoading() {
        mLoadProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    private void setLoadingCompleted(MediaPlayer mp) {
        mLoadProgressBar.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    @Override
    public void setVideoUri(Uri uri) {
        mVideoView.setVideoURI(uri);
    }

    @Override
    public void startVideoPlayback() {
        mVideoView.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
        mController.onCompletion();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(LOG_TAG, "onError");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        setLoadingCompleted(mp);
        mVideoViewInited = true;
        startPlayback();
    }
}