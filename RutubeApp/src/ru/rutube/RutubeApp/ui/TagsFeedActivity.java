package ru.rutube.RutubeApp.ui;

import ru.rutube.RutubeApp.MainApplication;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeFeed.ui.FeedActivity;

/**
 * Created by tumbler on 18.08.13.
 */
public class TagsFeedActivity extends FeedActivity {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.tags_feed_activity);
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
