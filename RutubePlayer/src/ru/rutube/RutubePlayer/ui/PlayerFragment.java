package ru.rutube.RutubePlayer.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.Trackinfo;
import ru.rutube.RutubeAPI.requests.RequestFactory;
import ru.rutube.RutubeAPI.requests.RutubeRequestManager;
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
    private RutubeRequestManager requestManager;
    private Uri streamUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestManager = RutubeRequestManager.from(this.getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        preparePlayback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        startPlayback();
    }

    /**
     * разбирает входные данные, инициирует запросы к rutube.ru
     */
    private void preparePlayback() {
        Intent intent = getActivity().getIntent();
        Uri uri = intent.getData();
        Log.d(LOG_TAG, "Got Uri: " + String.valueOf(uri));
        if (uri != null) {
            final List<String> segments = uri.getPathSegments();
            if (segments.size() == 2) {
                String videoId = segments.get(1);
                Request request = RequestFactory.getTrackInfoRequest(videoId);
                Log.d(LOG_TAG, "Executing TI request for" + videoId);
                        requestManager.execute(request, requestListener);
            } else {
                Log.d(LOG_TAG, "Incorrect Uri");
            }
        }
    }

    private void startPlayback() {
        try {
            Log.d(LOG_TAG, "Trying to start playback");
            VideoView vv = (VideoView) getView().findViewById(R.id.video_view);
            vv.setMediaController(new MediaController(getActivity()));
            vv.setVideoURI(streamUri);
            vv.setPadding(10, 0, 0, 0);
            vv.start();
        } catch (NullPointerException e) {
            Log.d(LOG_TAG, "Not ready yet");
        }

    }

    RequestManager.RequestListener requestListener = new RequestManager.RequestListener() {
        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
            Trackinfo trackinfo = resultData.getParcelable(Constants.Result.TRACKINFO);
            streamUri = trackinfo.getBalancerUrl();
            startPlayback();
        }

        @Override
        public void onRequestConnectionError(Request request, int statusCode) {
            //To change body of implemented methods use File | Settings | File Templates.
            Log.e(LOG_TAG, "error");
        }

        @Override
        public void onRequestDataError(Request request) {
            Log.e(LOG_TAG, "error");
        }

        @Override
        public void onRequestCustomError(Request request, Bundle resultData) {
            Log.e(LOG_TAG, "error");
        }
    };

}
