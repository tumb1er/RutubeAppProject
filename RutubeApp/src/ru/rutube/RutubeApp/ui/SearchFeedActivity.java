package ru.rutube.RutubeApp.ui;

import android.os.Bundle;

import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ui.feed.PlaFeedFragment;

/**
 * Created by tumbler on 18.08.13.
 */
public class SearchFeedActivity extends ru.rutube.RutubeFeed.ui.SearchFeedActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlaFeedFragment f = (PlaFeedFragment)getSupportFragmentManager().findFragmentById(R.id.feed_fragment);
        f.setStatsFeedTag("search_feed");
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
