package ru.rutube.RutubeAPI.content;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;

import ru.rutube.RutubeAPI.R;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 09.05.13
 * Time: 23:22
 * To change this template use File | Settings | File Templates.
 */
public class ContentMatcher {
    private static final String LOG_TAG = ContentMatcher.class.getName();
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
        Log.d(LOG_TAG, "Matching " + rutube_uri.toString());
        String path = rutube_uri.getPath();
        assert path != null;
        if (!path.endsWith("/"))
            path += "/";
        if (!path.startsWith("/"))
            path = "/" + path;
        Log.d(LOG_TAG, "Path: " + path);
        Uri result = uriMap.get(path);
        Log.d(LOG_TAG, "Matched: " + String.valueOf(result));
        return result;
    }

}
