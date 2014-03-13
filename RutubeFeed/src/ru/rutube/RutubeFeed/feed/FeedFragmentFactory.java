package ru.rutube.RutubeFeed.feed;

import android.net.Uri;

import ru.rutube.RutubeAPI.content.ContentMatcher;
import ru.rutube.RutubeFeed.ui.FeedFragment;

/**
 * Created by tumbler on 06.01.14.
 */
public class FeedFragmentFactory {

    public FeedFragment getFeedFragment(int fragmentType) {
        FeedFragment.FeedImpl feedImpl = getFeedImplementation(fragmentType);
        return instantiateFragment(feedImpl);
    }

    public FeedFragment getFeedFragment(String url) {
        Uri uri = Uri.parse(url);
        int feedType = ContentMatcher.getInstance().getFeedType(uri);
        if (feedType == ContentMatcher.COMMON)
            feedType = ContentMatcher.getInstance().getFeedTypeWithParams(uri);
        return getFeedFragment(feedType);
    }

    protected FeedFragment instantiateFragment(FeedFragment.FeedImpl feedImpl) {
        return new FeedFragment(feedImpl);
    }

    protected FeedFragment.FeedImpl getFeedImplementation(int fragmentType) {
        switch(fragmentType)
        {
            case ContentMatcher.EDITORS:
                return new EditorsFeedImpl();
            case ContentMatcher.SUBSCRIPTIONS:
                return new SubscriptionsFeedImpl();
            case ContentMatcher.COMMON:
            default:
                return new BasicFeedImpl();
        }
    }


}
