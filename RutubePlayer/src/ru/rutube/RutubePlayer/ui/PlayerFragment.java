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

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class PlayerFragment extends Fragment {
    private final String LOG_TAG = getClass().getName();
    private Uri streamUri;

    private RequestQueue mRequestQueue;
    private volatile int mPlayRequestStage;
    private boolean mViewCounted = false;
    private Video mVideo;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        preparePlayback();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: задваивается старт проигрывания
        startPlayback();
    }

    private void init() {
        // TODO: remove after transfer to DRF-2.3
        mRequestQueue = Volley.newRequestQueue(getActivity(),
                new HttpClientStack(HttpTransport.getHttpClient()));

    }

    private void showError() {
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

    protected RequestListener mTrackInfoRequestListener = new RequestListener() {

        @Override
        public void onResult(int tag, Bundle result) {
            Log.d(LOG_TAG, "Received result for " + String.valueOf(tag));
            if (tag == Requests.TRACK_INFO) {
                TrackInfo trackInfo = result.getParcelable(Constants.Result.TRACKINFO);
                assert trackInfo != null;
                streamUri = trackInfo.getBalancerUrl();
                mPlayRequestStage++;
            }
            if (tag == Requests.PLAY_OPTIONS) {
                Boolean allowed = result.getBoolean(Constants.Result.ACL_ALLOWED);
                if (!allowed) {
                    Log.w(LOG_TAG, "Playback not allowed");
                    showError();
                    return;
                }
                mPlayRequestStage++;
            }
            if (mPlayRequestStage == 2) {
                Log.d(LOG_TAG, "OK, playing");
                startPlayback();
            } else
                Log.d(LOG_TAG, "Not ready yet");
        }

        @Override
        public void onVolleyError(VolleyError error) {
            Log.e(LOG_TAG, error.toString());
            showError();
        }

        @Override
        public void onRequestError(int tag, RequestError error) {
            Log.e(LOG_TAG, error.toString());
            showError();
        }
    };

    private void preparePlayback() {
        Intent intent = getActivity().getIntent();
        Uri uri = intent.getData();
        Log.d(LOG_TAG, "Got Uri: " + String.valueOf(uri));
        if (uri != null) {
            final List<String> segments = uri.getPathSegments();
            Log.d(LOG_TAG, "Segments " + String.valueOf(segments));
            if (segments.size() == 2) {
                String videoId = segments.get(1);
                mVideo = new Video(videoId);
                startPlayRequests(mVideo);
            } else if (segments.size() == 3) {
                String videoId = segments.get(2);
                String signature = uri.getQueryParameter("p");
                mVideo = new Video(videoId, signature);
                startPlayRequests(mVideo);
            } else {
                Log.d(LOG_TAG, "Incorrect Uri");
            }
        }
    }

    private void startPlayRequests(Video video) {
        mPlayRequestStage = 0;
        JsonObjectRequest request = video.getTrackInfoRequest(getActivity(),
                mTrackInfoRequestListener);
        mRequestQueue.add(request);
        request = video.getPlayOptionsRequest(getActivity(),
                mTrackInfoRequestListener);
        mRequestQueue.add(request);
    }

    private void startPlayback() {
        try {
            Log.d(LOG_TAG, "Trying to start playback");
            VideoView vv = (VideoView) getView().findViewById(R.id.video_view);
            vv.setMediaController(new MediaController(getActivity()));
            vv.setVideoURI(streamUri);
            vv.setPadding(10, 0, 0, 0);
            vv.start();
            Log.d(LOG_TAG, "started, counting yast");
            if (!mViewCounted) {
                Log.d(LOG_TAG, "mVideo: " + String.valueOf(mVideo));
                JsonObjectRequest request = mVideo.getYastRequest(getActivity());
                mRequestQueue.add(request);
                mViewCounted = true;
                Log.d(LOG_TAG, "yast viewed");
            }
        } catch (NullPointerException e) {
            Log.d(LOG_TAG, "Not ready yet");
        }
    }
}
