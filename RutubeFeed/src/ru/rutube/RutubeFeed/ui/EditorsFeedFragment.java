package ru.rutube.RutubeFeed.ui;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.data.EditorsCursorAdapter;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
import ru.rutube.RutubeFeed.data.SubscriptionsCursorAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public class EditorsFeedFragment extends FeedFragment{
    private static final String LOG_TAG = EditorsFeedFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    @Override
    public FeedCursorAdapter initAdapter() {
        return new EditorsCursorAdapter(getActivity(),
                R.layout.feed_item,
                null,
                new String[]{FeedContract.FeedColumns.TITLE, FeedContract.FeedColumns.THUMBNAIL_URI},
                new int[]{R.id.titleTextView, R.id.thumbnailImageView},
                0);
    }
}
