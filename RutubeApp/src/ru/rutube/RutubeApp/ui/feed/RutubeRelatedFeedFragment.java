package ru.rutube.RutubeApp.ui.feed;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Author;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeApp.BuildConfig;
import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.data.RelatedCursorAdapter;
import ru.rutube.RutubeApp.ui.RutubeVideoPageActivity;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
import ru.rutube.RutubeFeed.helpers.Typefaces;
import ru.rutube.RutubeFeed.ui.RelatedFeedFragment;

/**
 * Created by tumbler on 18.08.13.
 * Фрагмент ленты для похожих видео.
 * Добавляет поддержку прокручеваемой информации о видео с помощью ListView.addHeaderView
 */
public class RutubeRelatedFeedFragment extends RelatedFeedFragment {
    private static final String LOG_TAG = RutubeRelatedFeedFragment.class.getName();
    public static final String INIT_HEADER = "init_header";
    private static final boolean D = BuildConfig.DEBUG;
    private ListView mListView;
    private Typeface mNormalFont;
    private Typeface mLightFont;
    private View mInfoView;
    private boolean mHasInfoView = false;
    private boolean mDescriptionVisible = false;
    protected View.OnClickListener mOnMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toggleDescription();
        }
    };
    protected View.OnClickListener mOnVideoElementClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (D) Log.d(LOG_TAG, "element click: " + String.valueOf(view));
            try {
                Uri feedUri = (Uri)view.getTag();
                MainApplication.getInstance().openFeed(feedUri, getActivity());
            } catch (ClassCastException ignored) {}

        }
    };

    protected static class ViewHolder extends RutubeVideoPageActivity.ViewHolder {
        ImageButton moreInfo;
        TextView bullet;
        View commentLine;
    }

    protected ViewHolder mViewHolder;

    private void toggleDescription(boolean visible) {
        if (visible)
            mViewHolder.moreInfo.setImageResource(R.drawable.more_info_btn_down);
        else
            mViewHolder.moreInfo.setImageResource(R.drawable.more_info_btn_left);
        mDescriptionVisible = visible;
        int visibility = visible? View.VISIBLE: View.GONE;
        mViewHolder.commentLine.setVisibility(visibility);
        mViewHolder.description.setVisibility(visibility);
    }

    private void toggleDescription() {
        toggleDescription(!mDescriptionVisible);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v!= null;
        mListView = (ListView) v.findViewById(android.R.id.list);
        initListView();
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra(INIT_HEADER, false))
            addHeaderView();
        return v;
    }

    private void initListView() {
        mNormalFont = Typefaces.get(getActivity(), "fonts/opensansregular.ttf");
        mLightFont = Typefaces.get(getActivity(), "fonts/opensanslight.ttf");
        // инициализирует и добавляет в ListView заголовок с информацией о видео.
        mInfoView = getActivity().getLayoutInflater().inflate(R.layout.video_info, null);
        assert mInfoView != null;
        mViewHolder = new ViewHolder();
        mViewHolder.title = ((TextView)mInfoView.findViewById(R.id.video_title));
        mViewHolder.from = ((TextView)mInfoView.findViewById(R.id.from));
        mViewHolder.author = ((TextView)mInfoView.findViewById(R.id.author_name));
        mViewHolder.bullet = ((TextView) mInfoView.findViewById(R.id.bullet));
        mViewHolder.created = ((TextView)mInfoView.findViewById(R.id.createdTextView));
        mViewHolder.hits = ((TextView)mInfoView.findViewById(R.id.hits));
        mViewHolder.description = ((TextView)mInfoView.findViewById(R.id.description));
        mViewHolder.moreInfo = ((ImageButton)mInfoView.findViewById(R.id.moreImageButton));
        mViewHolder.commentLine = mInfoView.findViewById(R.id.commentLine);

        mViewHolder.title.setTypeface(mNormalFont);
        mViewHolder.from.setTypeface(mLightFont);
        mViewHolder.author.setTypeface(mNormalFont);
        mViewHolder.bullet.setTypeface(mLightFont);
        mViewHolder.created.setTypeface(mLightFont);
        mViewHolder.hits.setTypeface(mLightFont);
        mViewHolder.description.setTypeface(mLightFont);

        mViewHolder.moreInfo.setOnClickListener(mOnMoreClickListener);
        mViewHolder.moreInfo.setVisibility(View.VISIBLE);
        mViewHolder.author.setOnClickListener(mOnVideoElementClickListener);

        toggleDescription(false);
        mListView.setHeaderDividersEnabled(false);
    }

    private void addHeaderView() {
        if (mHasInfoView) return;
        mHasInfoView = true;
        mListView.addHeaderView(mInfoView);
    }

    private void removeHeaderView() {
        if (!mHasInfoView) return;
        mHasInfoView = false;
        mListView.removeHeaderView(mInfoView);
    }

    @Override
    public ListAdapter getListAdapter() {
        // При вызове addHeaderView ListView трансформирует свой адаптер в HeaderViewListAdapter,
        // являющийся оберткой адаптера, проставляемого через ListView.setAdapter
        if (mHasInfoView)
            return ((HeaderViewListAdapter)mListView.getAdapter()).getWrappedAdapter();
        return mListView.getAdapter();
    }

    @Override
    public FeedCursorAdapter initAdapter() {
        return new RelatedCursorAdapter(getActivity(),
                R.layout.related_feed_item,
                null,
                new String[]{FeedContract.FeedColumns.TITLE, FeedContract.FeedColumns.THUMBNAIL_URI},
                new int[]{ru.rutube.RutubeFeed.R.id.titleTextView, ru.rutube.RutubeFeed.R.id.thumbnailImageView},
                0);
    }

    public void setVideoInfo(Video video) {
        mViewHolder.title.setText(video.getTitle());
        Author author = video.getAuthor();
        if (author != null) {
            mViewHolder.author.setText(author.getName());
            mViewHolder.author.setTag(author.getFeedUrl());

        }
//        int duration = video.getDuration();
//        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.duration)).setText(
//                DateUtils.formatElapsedTime(duration));
        String hits = video.getHitsText(getActivity());
        mViewHolder.hits.setText(hits);
        String description = video.getDescription();
        if (description != null)
            mViewHolder.description.setText(
                    Html.fromHtml(description));
        else
            mViewHolder.description.setText(null);
    }

    public void toggleHeader(boolean visible) {
        if (D) Log.d(LOG_TAG, "toggleHeader: " + String.valueOf(visible));
        mInfoView.setVisibility(visible? View.VISIBLE: View.GONE);
        // разметка такова, что при нахождении карточки видео в похожих, надо менять paddingRight
        // с 0 на значение, равное paddingLeft.
        // FIXME: разные layout + include общей чачти
        mInfoView.setPadding(
                mInfoView.getPaddingLeft(),
                mInfoView.getPaddingTop(),
                mInfoView.getPaddingLeft(),
                mInfoView.getPaddingBottom()
        );
        mListView.forceLayout();
    }


    @Override
    public boolean onItemClick(FeedCursorAdapter.ClickTag position, String viewTag) {
        MainApplication.relatedCardClick(getActivity(), viewTag);
        return super.onItemClick(position, viewTag);
    }
}
