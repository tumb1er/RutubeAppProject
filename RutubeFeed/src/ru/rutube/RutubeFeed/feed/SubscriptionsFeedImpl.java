package ru.rutube.RutubeFeed.feed;

import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
import ru.rutube.RutubeFeed.data.SubscriptionsCursorAdapter;

/**
 * Created by tumbler on 06.01.14.
 */
public class SubscriptionsFeedImpl extends BasicFeedImpl {
    @Override
    public FeedCursorAdapter initAdapter() {
        return new SubscriptionsCursorAdapter(mContext,
                R.layout.feed_item,
                null,
                new String[]{FeedContract.FeedColumns.TITLE, FeedContract.FeedColumns.THUMBNAIL_URI},
                new int[]{R.id.titleTextView, R.id.thumbnailImageView},
                0);
    }
}
