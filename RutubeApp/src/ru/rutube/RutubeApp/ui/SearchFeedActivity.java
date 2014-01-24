package ru.rutube.RutubeApp.ui;

import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;

import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeFeed.ui.FeedFragment;

/**
 * Created by tumbler on 18.08.13.
 */
public class SearchFeedActivity extends ru.rutube.RutubeFeed.ui.SearchFeedActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeedFragment f = (FeedFragment)getSupportFragmentManager().findFragmentById(R.id.feed_fragment);
        f.setEmptyText(getResources().getText(R.string.nothing_found).toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainApplication.searchActivityStart(this, getSearchQuery());
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainApplication.activityStop(this);
    }

}
