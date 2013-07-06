package ru.rutube.RutubeFeed.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import ru.rutube.RutubeFeed.R;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 04.05.13
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public class FeedActivity extends Activity {
    private static final String LOG_TAG = FeedActivity.class.getName();
    protected Uri feedUri;
    protected static final int R_FEED_MENU = R.menu.feed_menu;

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            feedUri = getIntent().getData();
        else {
            feedUri = savedInstanceState.getParcelable("feedUri");
            getIntent().setData(feedUri);
        }
        if (feedUri == null){
            feedUri = Uri.parse(getString(ru.rutube.RutubeAPI.R.string.base_uri)).buildUpon()
                .appendEncodedPath(getString(ru.rutube.RutubeAPI.R.string.editors_uri)).build();
            getIntent().setData(feedUri);
        }
        Log.d(LOG_TAG, "onCreate: " + feedUri.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_activity);
        Log.d(LOG_TAG, "content view set.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R_FEED_MENU, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("feedUri", feedUri);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return super.onPreparePanel(featureId, view, menu);
    }
}