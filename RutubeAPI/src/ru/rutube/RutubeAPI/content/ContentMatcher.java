package ru.rutube.RutubeAPI.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.models.FeedItem;

import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 09.05.13
 * Time: 23:22
 * To change this template use File | Settings | File Templates.
 */
public class ContentMatcher {
    private static final String PARAM_QUERY = "query";
    private static final String LOG_TAG = ContentMatcher.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private HashMap<String, Uri> uriMap = null;
    private static ContentMatcher instance;

    public static synchronized ContentMatcher from(Context context) {
        if (instance == null) {
            instance = new ContentMatcher(context);
        }
        return instance;
    }

    private ContentMatcher(Context context) {
        uriMap = new HashMap<String, Uri>();
        uriMap.put("/" + context.getString(R.string.editors_uri), FeedContract.Editors.CONTENT_URI);
        uriMap.put("/" + context.getString(R.string.my_video_uri), FeedContract.MyVideo.CONTENT_URI);
        uriMap.put("/" + context.getString(R.string.subscription_uri), FeedContract.Subscriptions.CONTENT_URI);
    }

    public Uri getContentUri(Uri rutube_uri) {
        if (D) Log.d(LOG_TAG, "Matching " + rutube_uri.toString());
        String path = rutube_uri.getPath();
        assert path != null;
        if (!path.endsWith("/"))
            path += "/";
        if (!path.startsWith("/"))
            path = "/" + path;
        if (D) Log.d(LOG_TAG, "Path: " + path);
        Uri result = uriMap.get(path);
        if (D) Log.d(LOG_TAG, "Matched: " + String.valueOf(result));
        return result;
    }

    public Uri getSearchContentUri(Context context, Uri feedUri) {
        if (D) Log.d(LOG_TAG, "Matching search: " + feedUri.toString());
        String path = feedUri.getPath();
        assert path != null;
        if (!path.startsWith("/"))
            path = "/" + path;
        String searchPath = "/" + context.getString(R.string.search_uri);
        if (!path.startsWith(searchPath)) {
            if (D) Log.d(LOG_TAG, "Path not ok");
            return null;
        }

        String query = feedUri.getQueryParameter(PARAM_QUERY);
        if (query == null){
            if (D) Log.d(LOG_TAG, "Missing query");
            return null;
        }

        ContentValues cv = new ContentValues();
        cv.put(FeedContract.SearchQuery.UPDATED, FeedItem.sSqlDateTimeFormat.format(new Date()));
        cv.put(FeedContract.SearchQuery.QUERY, query);
        String[] selectionArgs = {query};
        String[] projection = {FeedContract.SearchQuery._ID};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = contentResolver.query(
                FeedContract.SearchQuery.CONTENT_URI,
                projection,
                FeedContract.SearchQuery.QUERY + " = ?", selectionArgs,
                FeedContract.SearchQuery._ID);

        int query_id;
        if (c.moveToFirst()) {
            query_id = c.getInt(c.getColumnIndex(FeedContract.SearchQuery._ID));
            c.close();
            contentResolver.update(FeedContract.SearchQuery.CONTENT_URI, cv,
                    FeedContract.SearchQuery._ID + " = ?", selectionArgs);
        } else {
            c.close();
            Uri inserted = contentResolver.insert(FeedContract.SearchQuery.CONTENT_URI, cv);
            if (D) Log.d(LOG_TAG, "Inserted Search Query: " + inserted.toString());
            query_id = Integer.parseInt(inserted.getLastPathSegment());
        }
        Uri result = FeedContract.SearchResults.CONTENT_URI.buildUpon().appendPath(
                String.valueOf(query_id)).build();
        if (D) Log.d(LOG_TAG, "Matched search uri: " + result.toString());
        return result;
    }


}
