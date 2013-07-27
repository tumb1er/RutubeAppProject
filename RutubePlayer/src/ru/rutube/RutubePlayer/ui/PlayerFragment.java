package ru.rutube.RutubePlayer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.TrackInfo;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;
import ru.rutube.RutubePlayer.R;
import ru.rutube.RutubePlayer.ctrl.PlayerController;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class PlayerFragment extends Fragment
        implements PlayerController.PlayerView, MediaPlayer.OnCompletionListener,
            MediaPlayer.OnPreparedListener {
    private static final String CONTROLLER = "controller";
    private final String LOG_TAG = getClass().getName();
    protected PlayerController mController;
    protected VideoView mVideoView;
    protected Uri mStreamUri;
    protected Boolean mVideoViewInited;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
        init(savedInstanceState);
        mController.requestStream();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController.detach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTROLLER, mController);
    }

    private void init(Bundle savedInstanceState) {
        mVideoViewInited = false;
        mStreamUri = null;
        Activity activity = getActivity();
        assert activity != null;
        Intent intent = activity.getIntent();
        Uri uri = intent.getData();
        initVideoView();
        mController = null;
        if (savedInstanceState != null) {
            mController = savedInstanceState.getParcelable(CONTROLLER);
        }
        if (mController == null) {
            mController = new PlayerController(uri);
        }
        mController.attach(activity, this);
    }

    protected void initVideoView() {
        View view = getView();
        assert view != null;
        mVideoView =(VideoView) view.findViewById(R.id.video_view);
        MediaController mMediaController = new MediaController(getActivity());
        mVideoView.setMediaController(mMediaController);
        mVideoView.setPadding(10, 0, 0, 0);
        mVideoView.setOnCompletionListener(this);
    }

    @Override
    public void setStreamUri(Uri uri) {
        Log.d(LOG_TAG, "setStreamUri " + uri.toString());
        setVideoUri(uri);
        mStreamUri = uri;
    }

    public void setVideoUri(Uri uri) {
        mVideoView.setVideoURI(uri);
    }

    public void showError() {
        Activity activity = getActivity();
        if (activity == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.
                setTitle(android.R.string.dialog_alert_title).
                setMessage(getString(R.string.faled_to_load_data)).
                create().
                show();

    }

    public void startPlayback() {
        Log.d(LOG_TAG, "Trying to start playback");
        Log.d(LOG_TAG, "Launch control: inited: "
                + String.valueOf(mVideoViewInited) + " uri: " + String.valueOf(mStreamUri));
        if (mVideoViewInited && mStreamUri !=null)
        {
            Log.d(LOG_TAG, "Start!");
            startVideoPlayback();
        }
    }

    public void startVideoPlayback() {
        mVideoView.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mController.onCompletion();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mVideoViewInited = true;
        startPlayback();

    }
}
