package ru.rutube.RutubeApp.ui.feed;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Author;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.data.RelatedCursorAdapter;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
import ru.rutube.RutubeFeed.data.SubscriptionsCursorAdapter;
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
    private ListView mListView;
    private Typeface mNormalFont;
    private Typeface mLightFont;
    private View mInfoView;
    private boolean mHasInfoView = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v!= null;
        mListView = (ListView) v.findViewById(android.R.id.list);
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra(INIT_HEADER, false))
            initListView();
        return v;
    }

    private void initListView() {
        mHasInfoView = true;
        mNormalFont = Typefaces.get(getActivity(), "fonts/opensansregular.ttf");
        mLightFont = Typefaces.get(getActivity(), "fonts/opensanslight.ttf");
        // инициализирует и добавляет в ListView заголовок с информацией о видео.
        mInfoView = getActivity().getLayoutInflater().inflate(R.layout.video_info, null);
        assert mInfoView != null;
        ((TextView)mInfoView.findViewById(R.id.video_title)).setTypeface(mNormalFont);
        ((TextView)mInfoView.findViewById(R.id.fromTextView)).setTypeface(mLightFont);
        ((TextView)mInfoView.findViewById(R.id.author_name)).setTypeface(mNormalFont);
        ((TextView)mInfoView.findViewById(R.id.bullet)).setTypeface(mLightFont);
        ((TextView)mInfoView.findViewById(R.id.createdTextView)).setTypeface(mLightFont);
        ((TextView)mInfoView.findViewById(R.id.hits)).setTypeface(mLightFont);
        ((TextView)mInfoView.findViewById(R.id.descriptionTextView)).setTypeface(mLightFont);
        mListView.addHeaderView(mInfoView);
        mListView.setHeaderDividersEnabled(false);
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
        View v = getView();
        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.video_title)).setText(
                video.getTitle());
        Author author = video.getAuthor();
        if (author != null) {
            TextView authorName = (TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.author_name);
            authorName.setText(author.getName());
            authorName.setTag(author.getFeedUrl());

        }
//        int duration = video.getDuration();
//        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.duration)).setText(
//                DateUtils.formatElapsedTime(duration));
        String hits = video.getHitsText(getActivity());
        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.hits)).setText(hits);
        ((TextView)v.findViewById(R.id.descriptionTextView)).setText(video.getDescription());
    }
}
