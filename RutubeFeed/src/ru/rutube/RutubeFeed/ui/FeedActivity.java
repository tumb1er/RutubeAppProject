package ru.rutube.RutubeFeed.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import org.jetbrains.annotations.NotNull;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeFeed.R;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 04.05.13
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public class FeedActivity extends SherlockFragmentActivity {
    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;
    protected Uri feedUri;

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
        if (D) Log.d(LOG_TAG, "onCreate: " + feedUri.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_activity);
        if (D) Log.d(LOG_TAG, "content view set.");
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("feedUri", feedUri);
    }

}