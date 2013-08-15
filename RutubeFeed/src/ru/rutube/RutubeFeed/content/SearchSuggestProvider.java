package ru.rutube.RutubeFeed.content;

/**
 * Created by tumbler on 30.07.13.
 */
import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestProvider extends SearchRecentSuggestionsProvider {

    public static final String AUTHORITY =
            SearchSuggestProvider.class.getName();

    public static final int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}