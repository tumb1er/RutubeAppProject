package ru.rutube.RutubeFeed.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeAPI;
import ru.rutube.RutubeFeed.content.SearchSuggestProvider;

/**
 * Created by tumbler on 29.07.13.
 */
public class SearchFeedActivity extends FeedActivity {
    public static final String PARAM_QUERY = "query";
    private final String LOG_TAG = getClass().getName();

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
        }
        super.onCreate(savedInstanceState);
    }

    private Uri getSearchFeedUri(String query) {
        return Uri.parse(RutubeAPI.getUrl(this, R.string.search_uri)).buildUpon()
                .appendQueryParameter(PARAM_QUERY, query).build();
    }
}
