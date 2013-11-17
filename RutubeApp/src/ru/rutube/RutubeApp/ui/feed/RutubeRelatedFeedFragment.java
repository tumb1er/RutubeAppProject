package ru.rutube.RutubeApp.ui.feed;

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

import ru.rutube.RutubeAPI.models.Author;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeFeed.ui.RelatedFeedFragment;

/**
 * Created by tumbler on 18.08.13.
 * Фрагмент ленты для похожих видео.
 * Добавляет поддержку прокручеваемой информации о видео с помощью ListView.addHeaderView
 */
public class RutubeRelatedFeedFragment extends RelatedFeedFragment {
    private static final String LOG_TAG = RutubeRelatedFeedFragment.class.getName();
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v!= null;
        initListView(v);
        return v;
    }

    private void initListView(View fragmentView) {
        // инициализирует и добавляет в ListView заголовок с информацией о видео.
        View infoView = getActivity().getLayoutInflater().inflate(R.layout.video_info, null);
        mListView = (ListView) fragmentView.findViewById(android.R.id.list);
        mListView.addHeaderView(infoView);
    }

    @Override
    public ListAdapter getListAdapter() {
        // При вызове addHeaderView ListView трансформирует свой адаптер в HeaderViewListAdapter,
        // являющийся оберткой адаптера, проставляемого через ListView.setAdapter
        return ((HeaderViewListAdapter)mListView.getAdapter()).getWrappedAdapter();
    }

    public void setVideoInfo(Video video) {
        View v = getView();
        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.video_title)).setText(
                video.getTitle());
        Author author = video.getAuthor();
        if (author != null) {
            TextView authorName = (TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.author_name);
            String text = String.format("<a href=\"%s\">%s</a>",
                    author.getFeedUrl(), author.getName());
            authorName.setText(Html.fromHtml(text));

        }
        int duration = video.getDuration();
        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.duration)).setText(
                DateUtils.formatElapsedTime(duration / 1000));
        int hits = video.getHits();
        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.hits)).setText(String.valueOf(hits));
    }
}
