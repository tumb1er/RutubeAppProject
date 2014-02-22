package ru.rutube.RutubeApp.ui.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubePlayer.ui.PlayerFragment;

/**
 * Created by tumbler on 30.01.14.
 */
public class RutubePlayerFragment extends PlayerFragment {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = RutubePlayerFragment.class.getName();

    public RutubePlayerFragment() {
        super();
        mOnErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                if (D) Log.d(LOG_TAG, String.format("MediaPlayer error: %d %d", i, i2));
                String videoId = "";
                try {
                    videoId = mController.getVideoUri().getLastPathSegment();
                } catch (Exception ignored) {}
                MainApplication.playerError(getActivity(), String.format("MediaPlayer error (%d, %d)",
                        i, i2), videoId);
                mController.onPlaybackError();
                return true;
            }
        };

    }

    @Override
    public void showError(String error) {
        Activity activity = getActivity();
        if (activity == null)
            return;
        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
    }

}
