package ru.rutube.RutubePlayer.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

import io.vov.vitamio.activity.VideoActivity;
import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.TrackInfo;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubePlayer.R;

public class FragmentPlayer extends Fragment {

    private final String LOG_TAG = getClass().getName();
    private Uri streamUri;

    private RequestQueue mRequestQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        preparePlayback();

    }

    private void init() {
        // TODO: remove after transfer to DRF-2.3
        mRequestQueue = Volley.newRequestQueue(getActivity(),
                new HttpClientStack(HttpTransport.getHttpClient()));
    }

    private void showError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.
                setTitle(android.R.string.dialog_alert_title).
                setMessage(getString(R.string.faled_to_load_data)).
                create().
                show();
    }

    protected RequestListener mTrackInfoRequestListener = new RequestListener() {
        @Override
        public void onResult(int tag, Bundle result) {
            TrackInfo trackInfo = result.getParcelable(Constants.Result.TRACKINFO);
            streamUri = trackInfo.getBalancerUrl();
            startPlayback();
        }

        @Override
        public void onVolleyError(VolleyError error) {
            showError();
        }

        @Override
        public void onRequestError(int tag, RequestError error) {
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
                Video video = new Video(videoId);
                JsonObjectRequest request = video.getTrackInfoRequest(getActivity(),
                        mTrackInfoRequestListener);
                mRequestQueue.add(request);
            } else if (segments.size() == 3) {
                String videoId = segments.get(2);
                String signature = uri.getQueryParameter("p");
                Video video = new Video(videoId, signature);
                JsonObjectRequest request = video.getTrackInfoRequest(getActivity(),
                        mTrackInfoRequestListener);
                mRequestQueue.add(request);

            } else {
                Log.d(LOG_TAG, "Incorrect Uri");
            }
        }
    }

    private void startPlayback() {
        try {
            Log.d(LOG_TAG, "Trying to start playback");
            // TODO можем выводить название ролика / автора
            // надо бы передавать дополнительное инфо
            playVideo("Rutube", streamUri);
        } catch (NullPointerException e) {
            Log.d(LOG_TAG, "Not ready yet");
        }
    }

    private void playVideo(String title, Uri uri) {
        Intent playerIntent = new Intent(Intent.ACTION_VIEW, uri, getActivity(), VideoActivity.class);
        playerIntent.putExtra("displayName", title);
        getActivity().startActivity(playerIntent);

        // надо "закрыть" парент активити, чтобы воспользоваться кнопкой назад
        // не забывать использовать Fragments & FragmentActivity из v4.
        getActivity().finish();
    }

}
