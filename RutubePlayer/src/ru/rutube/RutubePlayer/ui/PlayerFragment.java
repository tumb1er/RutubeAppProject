package ru.rutube.RutubePlayer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
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
public class PlayerFragment extends Fragment implements PlayerController.PlayerView {
    private final String LOG_TAG = getClass().getName();
    private PlayerController mController;
    private VideoView mVideoView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
        init();
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

    private void init() {
        Activity activity = getActivity();
        assert activity != null;
        Intent intent = activity.getIntent();
        Uri uri = intent.getData();
        initVideoView();
        mController = new PlayerController(uri);
        mController.attach(activity, this);
    }

    private void initVideoView() {
        View view = getView();
        assert view != null;
        mVideoView =(VideoView) view.findViewById(R.id.video_view);
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.setPadding(10, 0, 0, 0);
    }

    @Override
    public void setStreamUri(Uri uri) {
        Log.d(LOG_TAG, "setStreamUri " + uri.toString());
        assert mVideoView != null;
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
        mVideoView.start();
    }
}
