package ru.rutube.RutubePlayer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.TrackInfo;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;
import ru.rutube.RutubePlayer.R;

/**
 * Created by oleg on 7/23/13.
 *
 * Класс для работы с сервисами плеера
 * TODO быстрый фикс для чистки кода Activity плеера
 */
public class ServiceVideoImpl {

    private RequestQueue requestQueue;
    private PlayerActivity playerActivity;

    private Video mVideo;
    private volatile int mPlayRequestStage;

    private Uri streamUri;
    private boolean mViewCounted = false;

    public ServiceVideoImpl(PlayerActivity playerActivity) {
        this.playerActivity = playerActivity;
        prepareVolley();
        preparePlayback();
    }

    private void prepareVolley() {
        requestQueue = Volley.newRequestQueue(this.playerActivity,
                new HttpClientStack(HttpTransport.getHttpClient()));
    }

    public void onResult(Uri streamUri) {

    }

    private void preparePlayback() {
        Intent intent = this.playerActivity.getIntent();
        Uri uri = intent.getData();
        Log.d(PlayerActivity.PLAYER_TAG, "Got Uri: " + String.valueOf(uri));
        if (uri != null) {
            final List<String> segments = uri.getPathSegments();
            Log.d(PlayerActivity.PLAYER_TAG, "Segments " + String.valueOf(segments));
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
                Log.d(PlayerActivity.PLAYER_TAG, "Incorrect Uri");
            }
        }
    }

    private void startPlayRequests(Video video) {
        mPlayRequestStage = 0;
        JsonObjectRequest request = video.getTrackInfoRequest(playerActivity, mTrackInfoRequestListener);
        requestQueue.add(request);
        request = video.getPlayOptionsRequest(playerActivity, mTrackInfoRequestListener);
        requestQueue.add(request);
    }

    protected RequestListener mTrackInfoRequestListener = new RequestListener() {

        @Override
        public void onResult(int tag, Bundle result) {
            Log.d(PlayerActivity.PLAYER_TAG, "Received result for " + String.valueOf(tag));
            if (tag == Requests.TRACK_INFO) {
                TrackInfo trackInfo = result.getParcelable(Constants.Result.TRACKINFO);
                assert trackInfo != null;
                streamUri = trackInfo.getBalancerUrl();
                mPlayRequestStage++;
            }
            if (tag == Requests.PLAY_OPTIONS) {
                Boolean allowed = result.getBoolean(Constants.Result.ACL_ALLOWED);
                if (!allowed) {
                    Log.w(PlayerActivity.PLAYER_TAG, "Playback not allowed");
                    showError();
                    return;
                }
                mPlayRequestStage++;
            }
            if (mPlayRequestStage == 2) {
                Log.d(PlayerActivity.PLAYER_TAG, "OK, playing");
                startPlayback();
            } else
                Log.d(PlayerActivity.PLAYER_TAG, "Not ready yet");
        }

        @Override
        public void onVolleyError(VolleyError error) {
            Log.e(PlayerActivity.PLAYER_TAG, error.toString());
            showError();
        }

        @Override
        public void onRequestError(int tag, RequestError error) {
            Log.e(PlayerActivity.PLAYER_TAG, error.toString());
            showError();
        }
    };

    private void showError() {
        Activity activity = this.playerActivity;
        if (activity == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.
                setTitle(android.R.string.dialog_alert_title).
                setMessage(this.playerActivity.getString(R.string.faled_to_load_data)).
                create().
                show();

    }

    private void startPlayback() {
        try {
            Log.d(PlayerActivity.PLAYER_TAG, "Trying to start playback");
            onResult(streamUri);
            Log.d(PlayerActivity.PLAYER_TAG, "started, counting yast");
            if (!mViewCounted) {
                Log.d(PlayerActivity.PLAYER_TAG, "mVideo: " + String.valueOf(mVideo));
                JsonObjectRequest request = mVideo.getYastRequest(playerActivity);
                requestQueue.add(request);
                mViewCounted = true;
                Log.d(PlayerActivity.PLAYER_TAG, "yast viewed");
            }
        } catch (NullPointerException e) {
            Log.d(PlayerActivity.PLAYER_TAG, "Not ready yet");
        }
    }
}
