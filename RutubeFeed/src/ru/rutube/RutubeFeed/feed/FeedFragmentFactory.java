package ru.rutube.RutubeFeed.feed;

import ru.rutube.RutubeFeed.ui.FeedFragment;

/**
 * Created by tumbler on 06.01.14.
 */
public class FeedFragmentFactory {

    public static final int COMMON = 0;
    public static final int EDITORS = 1;
    public static final int SUBSCRIPTIONS = 2;

    public FeedFragment getFeedFragment(int fragmentType) {
        FeedFragment.FeedImpl feedImpl = getFeedImplementation(fragmentType);
        return instantiateFragment(feedImpl);
    }

    protected FeedFragment instantiateFragment(FeedFragment.FeedImpl feedImpl) {
        return new FeedFragment(feedImpl);
    }

    protected FeedFragment.FeedImpl getFeedImplementation(int fragmentType) {
        switch(fragmentType)
        {
            case EDITORS:
                return new EditorsFeedImpl();
            case SUBSCRIPTIONS:
                return new SubscriptionsFeedImpl();
            case COMMON:
            default:
                return new BasicFeedImpl();
        }
    }


}
