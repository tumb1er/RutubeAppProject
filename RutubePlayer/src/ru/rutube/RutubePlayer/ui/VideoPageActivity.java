package ru.rutube.RutubePlayer.ui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.models.Author;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubePlayer.R;
import ru.rutube.RutubePlayer.ctrl.VideoPageController;


/**
 * Активити фулскрин-плеера.
 * Возможен старт по intent-action: ru.rutube.player.play c Uri видео вида:
 * http://rutube.ru/video/<video_id>/
 */
public class VideoPageActivity extends SherlockFragmentActivity
        implements PlayerFragment.PlayerEventsListener,
        EndscreenFragment.ReplayListener,
        VideoPageController.VideoPageView {
    private static final String CONTROLLER = "controller";
    private static final String FULLSCREEN = "fullscreen";
    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;
    private VideoPageController mController;
    protected boolean mIsTablet;
    protected boolean mIsFullscreen;
    protected int mLayoutResId = R.layout.player_activity;
    protected ViewHolder mViewHolder;

    protected static class ViewHolder {
        public PlayerFragment playerFragment;
        public EndscreenFragment endscreenFragment;
        public View videoInfoContainer;
        public TextView description;
        public TextView hits;
        public TextView duration;
        public TextView author;
        public TextView title;
    }

    @Override
    public void replay() {
        mViewHolder.playerFragment.replay();
    }

    @Override
    public void onPlay() {
        toggleEndscreen(false);
    }

    @Override
    public void onFail() {
        finish();
    }

    private void toggleEndscreen(boolean visible) {
        if (D) Log.d(LOG_TAG, "toggleEndscreen: " + String.valueOf(visible));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (visible)
            ft.show(mViewHolder.endscreenFragment);
        else
            ft.hide(mViewHolder.endscreenFragment);
        ft.commit();
    }

    @Override
    public void toggleFullscreen(boolean isFullscreen, boolean rotate) {
        mIsFullscreen = isFullscreen;
        initWindow(isFullscreen);
        if (!isFullscreen) {
            if (!mIsTablet && rotate) {
                // Для телефонов выход из полного экрана - это переход в портретную ориентацию
                setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            // Для планшетов - смена layout
            if (mViewHolder.videoInfoContainer != null)
                mViewHolder.videoInfoContainer.setVisibility(View.VISIBLE);
        } else {
            if (mViewHolder.videoInfoContainer != null)
                mViewHolder.videoInfoContainer.setVisibility(View.GONE);
            if (rotate)
                setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void closeVideoPage() {
        finish();
    }

    @Override
    public void onComplete() {
        if (D) Log.d(LOG_TAG, "onComplete");
        toggleEndscreen(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mController.attach(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mController.detach();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTROLLER, mController);
        outState.putBoolean(FULLSCREEN, mIsFullscreen);
    }

    @Override
    public boolean isFullscreen() {
        return mIsFullscreen;
    }

    @Override
    public void onBackPressed() {
        mController.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (D) Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mIsFullscreen = (savedInstanceState == null) ?
                getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                savedInstanceState.getBoolean(FULLSCREEN);
        initController(savedInstanceState);
        initWindow();
        setContentView(mLayoutResId);
        init();
        toggleFullscreen(mIsFullscreen, false);
    }

    private void initController(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mController = savedInstanceState.getParcelable(CONTROLLER);
        else
            mController = new VideoPageController(getIntent().getData());
    }

    @Override
    public void setVideoInfo(Video video) {
        bindTitle(video);
        bindAuthor(video);
        bindDuration(video);
        bindHits(video);
        bindDescription(video);
    }

    protected void bindDescription(Video video) {
        String description = video.getDescription();
        mViewHolder.description.setText(Html.fromHtml(description));
    }

    protected void bindHits(Video video) {
        String hits = video.getHitsText(this);
        mViewHolder.hits.setText(hits);
    }

    protected void bindDuration(Video video) {
        int duration = video.getDuration();
        mViewHolder.duration.setText(DateUtils.formatElapsedTime(duration / 1000));
    }

    protected void bindAuthor(Video video) {
        Author author = video.getAuthor();
        if (author != null) {
            TextView authorName = mViewHolder.author;
            authorName.setText(author.getName());
            authorName.setTag(author.getFeedUrl());

        }
    }

    protected void bindTitle(Video video) {
        mViewHolder.title.setText(video.getTitle());
    }

    @Override
    public void onDoubleTap() {
        mController.onDoubleTap();
    }

    private void init() {
        ViewHolder holder = getHolder();
        initHolder(holder);

        mViewHolder.playerFragment.setPlayerStateListener(this);
        mViewHolder.endscreenFragment.setReplayListener(this);

        toggleEndscreen(false);

        mIsTablet = getResources().getString(R.string.device_type, "phone").equals("tablet");

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    protected ViewHolder getHolder() {
        return new ViewHolder();
    }

    protected void initHolder(ViewHolder holder) {
        holder.playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment);
        holder.endscreenFragment = (EndscreenFragment) getSupportFragmentManager().findFragmentById(R.id.endscreen_fragment);
        holder.videoInfoContainer = findViewById(R.id.video_info_container);
        holder.description = ((TextView) findViewById(R.id.description));
        holder.hits = ((TextView) findViewById(R.id.hits));
        holder.duration = ((TextView) findViewById(R.id.duration));
        holder.author = (TextView) findViewById(R.id.author_name);
        holder.title = ((TextView) findViewById(R.id.video_title));
        mViewHolder = holder;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return mViewHolder.playerFragment.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
            default:
                return super.onKeyDown(keyCode, event);
        }

    }

    protected void initWindow() {
        initWindow(mIsFullscreen);
    }

    /**
     * фулскрин без заголовка окна
     */
    protected void initWindow(boolean isFullscreen) {
        if (D) Log.d(LOG_TAG, "initWindow fullscreen:" + String.valueOf(isFullscreen));
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (isFullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    @Override
    public int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else {
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    @Override
    public void setScreenOrientation(int orientation) {
        setRequestedOrientation(orientation);
    }
}
