package ru.rutube.RutubeApp.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
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

    public static class ViewHolder extends VideoPageActivity.ViewHolder {
        public TextView from;
        public TextView created;
    }

    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = RutubeVideoPageActivity.class.getName();

    private boolean mIsLandscape;
    private RutubeRelatedFeedFragment mRelatedFragment;
    private OrientationEventListener mOrientationListener;

    /**
     * Переопределяет обработчики кликов на элементы карточки видео, добавляя отправку статистики
     * Google Analytics
     */
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

    /**
     * Отложенная задача, включающая обработку событий от датчика ориентации.
     */
    protected Runnable mEnableOrientationEventListenerTask = new Runnable() {
        public void run() {
            if (D) Log.d(LOG_TAG, "Release Orientation");
            mOrientationListener.enable();
        }
    };

    /**
     * Обработчик изменения настроек автоповорота
     */
    protected ContentObserver mRotationObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            // При изменении настроек автоповорота включаем и выключаем обработчик событий
            // от датчика ориентации.
            boolean autorotate = Settings.System.getInt(
                    getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
            if (!autorotate) {
                if (D) Log.d(LOG_TAG, "Autorotate is off, skip orientation handling");
                mOrientationListener.disable();
            } else {
                if (D) Log.d(LOG_TAG, "Autorotate is on, enable orientation handling");
                mOrientationListener.enable();
            }
        }
    };

    /**
     * Переопределение методов Activity
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        checkOrientation();
        getIntent().putExtra(RutubeRelatedFeedFragment.INIT_HEADER, true);
        mLayoutResId = R.layout.video_page_activity;
        super.onCreate(savedInstanceState);
        init();
        transformLayout(mIsLandscape);
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
        mRelatedFragment = (RutubeRelatedFeedFragment)fm.findFragmentById(
                R.id.related_video_container);
        return view;
    }

    /**
     * Обработчик события изменения конфигурации устройства
     * @param newConfig новая конфигурация
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Трансформирует страницу в зависимости от указанной ориентации устройства,
        // включает и выключает фулскрин.
        if (D) Log.d(LOG_TAG, String.format("Orientation: %d", newConfig.orientation));
        if (D) Log.d(LOG_TAG, String.format("ReqOrient: %d", getRequestedOrientation()));
        checkOrientation();
        transformLayout(mIsLandscape);
        toggleFullscreen(mIsLandscape, false);
    }

    /**
     * Переопределение методов VideoPageActivity
     */

    @Override
    public void setVideoInfo(Video video) {
        mRelatedFragment.setVideoInfo(video);
        super.setVideoInfo(video);
    }

    /**
     * Переводит страницу в полноэкранный режим и при необходимости меняет ориентацию экрана
     * @param isFullscreen флаг полноэкранного режима
     * @param rotate флаг необходимости смены ориентации экрана
     */
    @Override
    public void toggleFullscreen(boolean isFullscreen, boolean rotate) {
        // при переходе в фулскрин скрывать похожие надо до изменения ориентации,
        if (isFullscreen)
            toggleRelatedFragment(false);

        super.toggleFullscreen(isFullscreen, rotate);
        checkOrientation();
        if (rotate)
            transformLayout(mIsLandscape);
        toggleVideoHeader();

        // при переходе в режим страницы видео похожие добавляются после изменения ориентации.
        if (!isFullscreen)
            toggleRelatedFragment(true);
    }

    /**
     * Изменяет видимость блока информации о видео
     */
    protected void toggleVideoHeader() {
        // основная карточка видео видна только в пейзажной ориентации и только не в фуллскрине
        mViewHolder.videoInfoContainer.setVisibility(
                (mIsLandscape && !mIsFullscreen) ? View.VISIBLE : View.GONE);
        // меняем видимость карточки видео в похожих в зависимости от ориентации
        mRelatedFragment.toggleHeader(!mIsLandscape);
    }


    /**
     * Задает ориентацию устройства
     * @param orientation значение ориентации, соответствующее ActivityInfo.SCREEN_ORIENTATION_*
     */
    @Override
    public void setScreenOrientation(int orientation) {
        if (D) Log.d(LOG_TAG, String.format("setScreenOrientation: %d", orientation));
        // временно выключаем обработку событий датчика ориентации
        mOrientationListener.disable();
        // изменяем ориентацию
        setRequestedOrientation(orientation);
        // добавляем отложенный вызов включения обработчика собитый датчика ориентации
        Handler handler = new Handler();
        handler.postDelayed(mEnableOrientationEventListenerTask, 500);
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
    protected void bindDuration(Video video) {}

    /**
     * Возвращает обработчик событий от датчика ориентации
     * @return OrientationEventListener
     */
    protected OrientationEventListener getOrientationEventListener() {

        return new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {

            /**
             * Обрабатывает события от датчика ориентации
             * Заменяет явно заданную ориентацию экрана на "Не указано", когда пользователь
             * поворачивает устройтво так, чтобы реальная ориентация совпала с указанной
             * через setRequestedOrientation
             @param degree угол поворота устройства
             */
            @Override
            public void onOrientationChanged(int degree) {

                if (D) Log.d(LOG_TAG, String.format("Orientation changed! %d", degree));
                // Переводим угол поворота в константы ориентации устройства
                degree = ((degree + 45) / 90) % 4;
                int orientation = (degree == 0 || degree == 2)?
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                if (D) Log.d(LOG_TAG, String.format("Orient: %d %d", orientation,
                        getScreenOrientation()));
                // Если реальная ориентация совпала с указанной, сбрасываем ориентацию экрана.
                if (orientation == getScreenOrientation()) {
                    if (D) Log.d(LOG_TAG, "Orient: Rotating screen");
                    setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                }
            }
        };
    }

    protected void transformLayout(boolean isLandscape) {
        if(D) Log.d(LOG_TAG, "transformLayout " + String.valueOf(!isLandscape));
        // список похожих справа или снизу
        LinearLayout ll = (LinearLayout)findViewById(R.id.page);
        ll.setOrientation(isLandscape? LinearLayout.HORIZONTAL: LinearLayout.VERTICAL);

        // основная карточка видео видна только в пейзажной ориентации и только не в фуллскрине
        mViewHolder.videoInfoContainer.setVisibility(
                (isLandscape && !mIsFullscreen) ? View.VISIBLE : View.GONE);

        // карточка видео в похожих видна только в портретной ориентации
        mRelatedFragment.toggleHeader(!isLandscape);

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

    protected void checkOrientation() {
        mIsLandscape = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    /**
     * Меняет видимость ленты похожих видео
     * @param visible флаг видимости ленты похожих видео
     */
    private void toggleRelatedFragment(boolean visible) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!visible)
            ft.hide(mRelatedFragment);
        else
            ft.show(mRelatedFragment);
        try {
            ft.commit();
        } catch (IllegalStateException ignored) {}
    }


    private void init() {

        mOrientationListener = getOrientationEventListener();
        mOrientationListener.enable();
        getContentResolver().registerContentObserver(Settings.System.getUriFor
                (Settings.System.ACCELEROMETER_ROTATION), true, mRotationObserver);


        Typeface normalFont = Typefaces.get(this, "fonts/opensansregular.ttf");
        Typeface lightFont = Typefaces.get(this, "fonts/opensanslight.ttf");

        FragmentManager fm = getSupportFragmentManager();
        mRelatedFragment = (RutubeRelatedFeedFragment) fm.findFragmentById(
                R.id.related_video_container);

        ViewHolder holder = (ViewHolder)mViewHolder;
        holder.title.setTypeface(normalFont);
        holder.from.setTypeface(lightFont);
        holder.author.setTypeface(lightFont);
        holder.created.setTypeface(lightFont);
        holder.hits.setTypeface(lightFont);
        holder.description.setTypeface(lightFont);

        holder.author.setOnClickListener(mOnVideoElementClickListener);
    }

}
