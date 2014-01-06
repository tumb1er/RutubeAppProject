package ru.rutube.RutubeApp.ui;

import android.os.Bundle;

import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeFeed.feed.SubscriptionsFeedImpl;
import ru.rutube.RutubeFeed.ui.FeedActivity;
import ru.rutube.RutubeFeed.ui.FeedFragment;

/**
 * Created by tumbler on 18.08.13.
 */
public class TagsFeedActivity extends FeedActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeedFragment f = (FeedFragment)getSupportFragmentManager().findFragmentById(R.id.feed_fragment);
        f.setFeedImplementation(new SubscriptionsFeedImpl());
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainApplication.feedActivityStart(this, String.valueOf(getIntent().getData()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainApplication.activityStop(this);
    }

}
