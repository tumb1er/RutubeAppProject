package ru.rutube.RutubeApp.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
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
    public void onCreate(Bundle savedInstanceState) {
        mIsLandscape = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        boolean needHeader = getString(R.string.device_type).equals("phone");
        getIntent().putExtra(RutubeRelatedFeedFragment.INIT_HEADER, needHeader);
        mLayoutResId = R.layout.video_page_activity;
        super.onCreate(savedInstanceState);
        init();
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
        transformLayout(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    protected void transformLayout(boolean isPortrait) {
        if(D) Log.d(LOG_TAG, "transformLayout " + String.valueOf(isPortrait));
        // список похожих справа или снизу
        LinearLayout ll = (LinearLayout)findViewById(R.id.page);
        ll.setOrientation(isPortrait? LinearLayout.VERTICAL: LinearLayout.HORIZONTAL);

        // основная карточка видео видна только в пейзажной ориентации и только не в фуллскрине
        mViewHolder.videoInfoContainer.setVisibility((isPortrait || mIsFullscreen) ? View.GONE : View.VISIBLE);
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

    @Override
    public void toggleFullscreen(boolean isFullscreen) {
        // при переходе в фулскрин скрывать похожие надо до изменения ориентации,
        if (isFullscreen)
            toggleRelatedFragment(false);
        super.toggleFullscreen(isFullscreen);
        mViewHolder.videoInfoContainer.setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
        // а при переходе в режим страницы видео - после изменения ориентации.
        if (!isFullscreen)
            toggleRelatedFragment(true);

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
