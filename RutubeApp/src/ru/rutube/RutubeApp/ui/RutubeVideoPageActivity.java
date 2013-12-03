package ru.rutube.RutubeApp.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeApp.BuildConfig;
import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ui.feed.RutubeRelatedFeedFragment;
import ru.rutube.RutubeFeed.helpers.Typefaces;
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
    private boolean mIsLandscape;
    private Typeface mNormalFont;
    private Typeface mLightFont;
    private RutubeRelatedFeedFragment mRelatedFragment;
    private OrientationEventListener mOrientationListener;

    protected View.OnClickListener mOnVideoElementClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (D) Log.d(LOG_TAG, "element click: " + String.valueOf(view));
            try {
                Uri feedUri = (Uri)view.getTag();
                MainApplication.getInstance().openFeed(feedUri, RutubeVideoPageActivity.this);
            } catch (ClassCastException ignored) {}

        }
    };

    public static class ViewHolder extends VideoPageActivity.ViewHolder {
        public TextView from;
        public TextView created;
    }

    @Override
    protected VideoPageActivity.ViewHolder getHolder() {
        return new ViewHolder();
    }

    @Override
    protected void initHolder(VideoPageActivity.ViewHolder holder) {
        super.initHolder(holder);
        ViewHolder h = (ViewHolder)holder;
        h.from = ((TextView)findViewById(R.id.from));
        h.created = ((TextView)findViewById(R.id.createdTextView));
        mViewHolder = h;

    }

    @Override
    public void setScreenOrientation(int orientation) {
        if (D) Log.d(LOG_TAG, String.format("setScreenOrientation: %d", orientation));
        mOrientationListener.disable();
        setRequestedOrientation(orientation);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {
                if (D) Log.d(LOG_TAG, "Release Orientation");
                mOrientationListener.enable();
            }

        }, 5000); // 5000ms delay
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mIsLandscape = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        boolean needHeader = getString(R.string.device_type).equals("phone");
        // FIXME
        getIntent().putExtra(RutubeRelatedFeedFragment.INIT_HEADER, true);
        mLayoutResId = R.layout.video_page_activity;
        super.onCreate(savedInstanceState);
        init();
        transformLayout(!mIsLandscape);
        // toggleFullscreen(mIsLandscape);
        resetScreenOrientation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainApplication.playerActivityStart(this, String.valueOf(getIntent().getData()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainApplication.activityStop(this);
    }


    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(name, context, attrs);
        FragmentManager fm = getSupportFragmentManager();
        mRelatedFragment = (RutubeRelatedFeedFragment)fm.findFragmentById(R.id.related_video_container);
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        resetScreenOrientation();
        if (D) Log.d(LOG_TAG, String.format("Orientation: %d", newConfig.orientation));
        if (D) Log.d(LOG_TAG, String.format("ReqOrient: %d", getRequestedOrientation()));
        transformLayout(isPortrait);
        toggleFullscreen(!isPortrait);
    }

    private void resetScreenOrientation() {
    }

    protected void transformLayout(boolean isPortrait) {
        if(D) Log.d(LOG_TAG, "transformLayout " + String.valueOf(isPortrait));
        // список похожих справа или снизу
        LinearLayout ll = (LinearLayout)findViewById(R.id.page);
        ll.setOrientation(isPortrait? LinearLayout.VERTICAL: LinearLayout.HORIZONTAL);

        // основная карточка видео видна только в пейзажной ориентации и только не в фуллскрине
        mViewHolder.videoInfoContainer.setVisibility((!isPortrait && !mIsFullscreen) ? View.VISIBLE : View.GONE);

        // карточка видео в похожих видна только в портретной ориентации
        mRelatedFragment.toggleHeader(isPortrait);
        // layout_weight для плеера + карточки видео в зависимости от ориентации
        View v = findViewById(R.id.video_container);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)v.getLayoutParams();
        lp.weight = getResources().getInteger(R.integer.video_container_weight);
        if(D) Log.d(LOG_TAG, "VC weight: " + String.valueOf(lp.weight));
        v.setLayoutParams(lp);
        // layout_weight для похожих в зависимости от ориентации
        v = mRelatedFragment.getView();
        lp =(LinearLayout.LayoutParams)v.getLayoutParams();
        lp.weight = getResources().getInteger(R.integer.related_video_container_weight);
        if(D) Log.d(LOG_TAG, "RC weight: " + String.valueOf(lp.weight));
        v.setLayoutParams(lp);
    }

    private void init() {

        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int degree) {
                boolean autorotate = Settings.System.getInt(
                        getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
                if (!autorotate) {
                    if (D) Log.d(LOG_TAG, "Autorotate is off, skip orientation handling");
                    return;
                }
                if (D) Log.d(LOG_TAG, String.format("Orientation changed! %d", degree));
                degree = ((degree + 45) / 90) % 4;
                int orientation = (degree == 0 || degree == 2)?
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                if (D) Log.d(LOG_TAG, String.format("Orient: %d %d", orientation, getScreenOrientation()));
                if (orientation == getScreenOrientation()) {
                    if (D) Log.d(LOG_TAG, "Orient: Rotating screen");
                    setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                }
            }
        };
        mOrientationListener.enable();
        mNormalFont = Typefaces.get(this, "fonts/opensansregular.ttf");
        mLightFont = Typefaces.get(this, "fonts/opensanslight.ttf");
        FragmentManager fm = getSupportFragmentManager();
        mRelatedFragment = (RutubeRelatedFeedFragment) fm.findFragmentById(
                R.id.related_video_container);
        ViewHolder holder = (ViewHolder)mViewHolder;
        holder.title.setTypeface(mNormalFont);
        holder.from.setTypeface(mLightFont);
        holder.author.setTypeface(mLightFont);
        holder.created.setTypeface(mLightFont);
        holder.hits.setTypeface(mLightFont);
        holder.description.setTypeface(mLightFont);

        holder.author.setOnClickListener(mOnVideoElementClickListener);
    }

    @Override
    protected void bindDuration(Video video) {}

    @Override
    public void setVideoInfo(Video video) {
        mRelatedFragment.setVideoInfo(video);
        super.setVideoInfo(video);
    }

    public void toggleFullscreen(boolean isFullscreen) {
        toggleFullscreen(isFullscreen, false);
    }

    @Override
    public void toggleFullscreen(boolean isFullscreen, boolean rotate) {
        // при переходе в фулскрин скрывать похожие надо до изменения ориентации,
        if (isFullscreen)
            toggleRelatedFragment(false);
        super.toggleFullscreen(isFullscreen, rotate);
        mIsLandscape = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        if (rotate)
            transformLayout(!mIsLandscape);
        // основная карточка видео видна только в пейзажной ориентации и только не в фуллскрине
        mViewHolder.videoInfoContainer.setVisibility((mIsLandscape && !mIsFullscreen) ? View.VISIBLE : View.GONE);
        // а при переходе в режим страницы видео - после изменения ориентации.
        if (!isFullscreen)
            toggleRelatedFragment(true);
        // вызываем relayout() у заголовка в похожих
        mRelatedFragment.toggleHeader(!mIsLandscape);
        mIsFullscreen = isFullscreen;
        initWindow();

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
