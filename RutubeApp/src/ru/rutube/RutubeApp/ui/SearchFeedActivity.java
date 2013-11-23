package ru.rutube.RutubeApp.ui;

import com.google.analytics.tracking.android.EasyTracker;

import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;

/**
 * Created by tumbler on 18.08.13.
 */
public class SearchFeedActivity extends ru.rutube.RutubeFeed.ui.SearchFeedActivity {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.search_feed_activity);
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
