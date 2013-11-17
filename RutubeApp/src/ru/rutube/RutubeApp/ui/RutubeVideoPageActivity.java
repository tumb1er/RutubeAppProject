package ru.rutube.RutubeApp.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;

import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeApp.BuildConfig;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ui.feed.RutubeRelatedFeedFragment;
import ru.rutube.RutubePlayer.ui.VideoPageActivity;

/**
 * Created by tumbler on 13.11.13.
 * Активити страницы видео.
 * Переопределяет поведение страницы видео из библиотеки RutubePlayer:
 *   - добавляет фрагмент с похожими
 *   - переносит информацию о видео в ListView.addHeaderView с помощью RutubeRelatedFeedFragment
 */
public class RutubeVideoPageActivity extends VideoPageActivity {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = RutubeVideoPageActivity.class.getName();
    private RutubeRelatedFeedFragment mRelatedFragment;

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(name, context, attrs);
        FragmentManager fm = getSupportFragmentManager();
        mRelatedFragment = (RutubeRelatedFeedFragment) fm.findFragmentById(
                R.id.related_video_container);
        return view;
    }

    @Override
    public void setVideoInfo(Video video) {
        mRelatedFragment.setVideoInfo(video);
    }

    @Override
    public void toggleFullscreen(boolean isFullscreen) {
        // при переходе в фулскрин скрывать похожие надо до изменения ориентации,
        if (isFullscreen)
            toggleRelatedFragment(!isFullscreen);
        super.toggleFullscreen(isFullscreen);
        // а при переходе в режим страницы видео - после изменения ориентации.
        if (!isFullscreen)
            toggleRelatedFragment(!isFullscreen);

    }

    private void toggleRelatedFragment(boolean visible) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!visible)
            ft.hide(mRelatedFragment);
        else
            ft.show(mRelatedFragment);
        ft.commit();
    }
}
