package ru.rutube.RutubeApp.ui.feed;

import ru.rutube.RutubeFeed.feed.FeedFragmentFactory;
import ru.rutube.RutubeFeed.ui.FeedFragment;

/**
 * Created by tumbler on 06.01.14.
 */
public class PlaFeedFragmentFactory extends FeedFragmentFactory{

    @Override
    protected FeedFragment instantiateFragment(FeedFragment.FeedImpl feedImpl) {
        return new PlaFeedFragment(feedImpl);
    }
}
