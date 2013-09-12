package ru.rutube.RutubeFeed.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeFeed.content.SearchSuggestProvider;

/**
 * Created by tumbler on 29.07.13.
 */
public class SearchFeedActivity extends FeedActivity {
    public static final String PARAM_QUERY = "query";
    private final String LOG_TAG = getClass().getName();
    private String mQuery = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(this,
                    SearchSuggestProvider.AUTHORITY,
                    SearchSuggestProvider.MODE);
            searchRecentSuggestions.saveRecentQuery(query, null);
            Uri feedUri = getSearchFeedUri(query);
            intent = new Intent();
            Log.d(LOG_TAG, "Computed URI: " + feedUri.toString());
            intent.setData(feedUri);
            setIntent(intent);
            mQuery = query;
        }
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (mQuery != null)
            setTitle(mQuery);
    }


    private Uri getSearchFeedUri(String query) {
        return Uri.parse(RutubeApp.getUrl(R.string.search_uri)).buildUpon()
                .appendQueryParameter(PARAM_QUERY, query).build();
    }
}
