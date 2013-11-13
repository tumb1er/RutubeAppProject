package ru.rutube.RutubeApp.ui;

import android.view.View;

import ru.rutube.RutubeApp.R;
import ru.rutube.RutubePlayer.ui.VideoPageActivity;

/**
 * Created by tumbler on 13.11.13.
 */
public class RutubeVideoPageActivity extends VideoPageActivity {
    @Override
    public void toggleFullscreen(boolean isFullscreen) {
        super.toggleFullscreen(isFullscreen);
        findViewById(R.id.related_video_container).setVisibility(
                isFullscreen? View.GONE: View.VISIBLE);
    }
}
