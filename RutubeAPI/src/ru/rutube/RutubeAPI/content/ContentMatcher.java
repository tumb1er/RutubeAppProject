package ru.rutube.RutubeAPI.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.models.FeedItem;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 09.05.13
 * Time: 23:22
 * To change this template use File | Settings | File Templates.
 */
public class ContentMatcher {
    public static final int COMMON = 0;
    public static final int EDITORS = 1;
    public static final int MYVIDEO = 2;
    public static final int SUBSCRIPTIONS = 3;
    public static final int RELATED = 4;
    public static final int AUTHOR = 5;
    public static final int TAGVIDEO = 6;
    public static final int TVSHOWVIDEO = 7;
    public static final int PERSONVIDEO = 8;

    private static final String PARAM_QUERY = "query";
    private static final String LOG_TAG = ContentMatcher.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private HashMap<String, Integer> feedTypeMap;
    private SparseArray<Uri> contentUriMap;
    private static ContentMatcher instance;

    public static synchronized ContentMatcher from(Context context) {
        if (instance == null) {
            instance = new ContentMatcher(context);
        }
        return instance;
    }

    private ContentMatcher(Context context) {
        feedTypeMap = new HashMap<String, Integer>();
        feedTypeMap.put("/" + context.getString(R.string.editors_uri), EDITORS);
        feedTypeMap.put("/" + context.getString(R.string.my_video_uri), MYVIDEO);
        feedTypeMap.put("/" + context.getString(R.string.subscription_uri), SUBSCRIPTIONS);
        feedTypeMap.put("/" + context.getString(R.string.related_video_uri), RELATED);
        feedTypeMap.put("/" + context.getString(R.string.authors_uri), AUTHOR);
        feedTypeMap.put("/" + context.getString(R.string.video_by_tag_uri), TAGVIDEO);
        feedTypeMap.put("/" + context.getString(R.string.tvshow_video_uri), TVSHOWVIDEO);
        feedTypeMap.put("/" + context.getString(R.string.person_video_uri), PERSONVIDEO);

        contentUriMap = new SparseArray<Uri>();
        contentUriMap.put(EDITORS, FeedContract.Editors.CONTENT_URI);
        contentUriMap.put(MYVIDEO, FeedContract.MyVideo.CONTENT_URI);
        contentUriMap.put(SUBSCRIPTIONS, FeedContract.Subscriptions.CONTENT_URI);
        contentUriMap.put(RELATED, FeedContract.RelatedVideo.CONTENT_URI);
        contentUriMap.put(AUTHOR, FeedContract.AuthorVideo.CONTENT_URI);
        contentUriMap.put(TAGVIDEO, FeedContract.TagsVideo.CONTENT_URI);
        contentUriMap.put(TVSHOWVIDEO, FeedContract.TVShowVideo.CONTENT_URI);
        contentUriMap.put(PERSONVIDEO, FeedContract.PersonVideo.CONTENT_URI);
    }

    public Uri getContentUri(Uri rutube_uri) {
        Integer feedType = getFeedType(rutube_uri);

        Uri result = contentUriMap.get(feedType);
        if (result == null)
            result = matchWithParams(rutube_uri);
        if (result == null)
            result = matchMetainfo(rutube_uri);
        if (result == null)
            throw new IllegalArgumentException("Unmatched url: " + String.valueOf(rutube_uri));
        if (D) Log.d(LOG_TAG, "Matched: " + String.valueOf(result));
        return result;
    }

    public int getFeedType(Uri rutube_uri) {
        if (D) Log.d(LOG_TAG, "Matching " + rutube_uri.toString());
        String path = rutube_uri.getPath();
        assert path != null;
        path = normalize(path);
        if (D) Log.d(LOG_TAG, "Path: " + path);

        return feedTypeMap.containsKey(path)? feedTypeMap.get(path): COMMON;
    }

    public String normalize(String path) {
        if (!path.endsWith("/"))
            path += "/";
        if (!path.startsWith("/"))
            path = "/" + path;
        if (!path.startsWith("/api/"))
            path = "/api" + path;
        return path;
    }

    public int getFeedTypeWithParams(Uri rutube_uri){
        List<String> segments = rutube_uri.getPathSegments();
        assert segments != null;
        String last = segments.get(segments.size() - 1);
        Integer feedType;
        if (last.matches("^[\\d]+$")) {
            String path = TextUtils.join("/", segments).replace(last, "%d");
            path = normalize(path);
            if (D) Log.d(LOG_TAG, "Matching with params: " + path);
            feedType = feedTypeMap.get(path);
            if (feedType != null)
                return feedType;

        }
        if (last.matches("^[\\da-f]{32}$")) {
            String path = TextUtils.join("/", segments).replace(last, "%s");
            path = normalize(path);
            if (D) Log.d(LOG_TAG, "Matching with params: " + path);
            feedType = feedTypeMap.get(path);
            if (feedType != null)
                return feedType;
        }
        if (D) Log.d(LOG_TAG, "Not matched URL: " + String.valueOf(rutube_uri));
        return COMMON;
    }

    public int getMetainfoFeedType(Uri rutube_uri) {
        if(D) Log.d(LOG_TAG, "MI: start " + String.valueOf(rutube_uri));
        List<String> segments = rutube_uri.getPathSegments();
        assert segments != null;
        String id = segments.get(segments.size() - 2);
        if(D) Log.d(LOG_TAG, "MI: id segment: " + id);
        if (!id.matches("^[\\d]+"))
            return COMMON;

        String path = TextUtils.join("/", segments).replace(id, "%d");
        if(D) Log.d(LOG_TAG, "MI: path: " + path);
        path = normalize(path);
        if(D) Log.d(LOG_TAG, "MI: normalized: " + path);
        if (D) Log.d(LOG_TAG, "Matching metainfo: " + path);
        Integer feedType = feedTypeMap.get(path);
        if (feedType == null){
            if (D) Log.d(LOG_TAG, "Not matched URL: " + path);
            if (D) Log.d(LOG_TAG, "Map: " + String.valueOf(feedTypeMap));
            return COMMON;
        }
        return feedType;

    }

    /**
     * Выбирает из урла последний сегмент, и если он числовой, осуществляет поиск соответствующего
     * contentUri
     * @param rutube_uri
     * @return
     */
    private Uri matchWithParams(Uri rutube_uri) {
        int feedType = getFeedTypeWithParams(rutube_uri);
        if (feedType == COMMON)
            return null;
        Uri result = contentUriMap.get(feedType);
        if (result == null)
            return null;
        List<String> segments = rutube_uri.getPathSegments();
        assert segments != null;
        String last = segments.get(segments.size() - 1);

        result = result.buildUpon().appendPath(last).build();
        if (D) Log.d(LOG_TAG, "Matched: " + String.valueOf(result));
        return result;
    }

    private Uri matchMetainfo(Uri rutube_uri) {
        int feedType = getMetainfoFeedType(rutube_uri);
        if (feedType == COMMON)
            return null;
        Uri result = contentUriMap.get(feedType);
        if (D)Log.d(LOG_TAG, "Got MI Uri: " + String.valueOf(result));
        if (result == null)
            return null;
        List<String> segments = rutube_uri.getPathSegments();
        assert segments != null;
        String id = segments.get(segments.size() - 2);

        result = result.buildUpon().appendPath(id).build();
        if (D) Log.d(LOG_TAG, "Matched: " + String.valueOf(result));
        return result;
    }

    public Uri getRelatedVideoContentUri(Uri feedUri) {
        Context context = RutubeApp.getInstance();
        String path = feedUri.getPath();
        assert path != null;
        if (!path.startsWith("/"))
            path = "/" + path;
        String relatedPath = "/" + context.getString(R.string.related_video_uri);
        if (!path.startsWith(relatedPath)){
            return null;
        }
        List<String> pathSegments = feedUri.getPathSegments();
        assert pathSegments != null;
        if (pathSegments.size() != 4) {
            return null;
        }
        String related_video_id = pathSegments.get(3);
        return FeedContract.RelatedVideo.CONTENT_URI.buildUpon().appendPath(
                String.valueOf(related_video_id)).build();
    }

    public Uri getSearchContentUri(Uri feedUri) {
        if (D) Log.d(LOG_TAG, "Matching search: " + feedUri.toString());
        Context context = RutubeApp.getInstance();
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
        assert c != null;
        if (c.moveToFirst()) {
            query_id = c.getInt(c.getColumnIndex(FeedContract.SearchQuery._ID));
            c.close();
            contentResolver.update(FeedContract.SearchQuery.CONTENT_URI, cv,
                    FeedContract.SearchQuery._ID + " = ?", selectionArgs);
        } else {
            c.close();
            Uri inserted = contentResolver.insert(FeedContract.SearchQuery.CONTENT_URI, cv);
            assert inserted != null;
            if (D) {
                Log.d(LOG_TAG, "Inserted Search Query: " + inserted.toString());
            }
            query_id = Integer.parseInt(inserted.getLastPathSegment());
        }
        Uri result = FeedContract.SearchResults.CONTENT_URI.buildUpon().appendPath(
                String.valueOf(query_id)).build();
        assert result != null;
        if (D) {
            Log.d(LOG_TAG, "Matched search uri: " + result.toString());
        }
        return result;
    }


    public static ContentMatcher getInstance() {
        return from(RutubeApp.getInstance());
    }
}
