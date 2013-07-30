package ru.rutube.RutubeAPI;

import android.content.Context;
import android.net.Uri;

import ru.rutube.RutubeAPI.tools.BitmapLruCache;

/**
 * Created by tumbler on 22.06.13.
 */
public class RutubeAPI {

    public static BitmapLruCache sBitmapCache = new BitmapLruCache();

    public static BitmapLruCache getBitmapCache() {
        return sBitmapCache;
    }

    public static String getUrl(Context context, int stringId) {
        assert context != null;
        Uri baseUri = Uri.parse(context.getString(R.string.base_uri));
        Uri resultUri = baseUri.buildUpon()
                .appendEncodedPath(context.getString(stringId))
                .build();
        assert resultUri != null;
        return resultUri.toString();
    }

    public static String getUrl(Context context, String path) {
        assert context != null;
        Uri baseUri = Uri.parse(context.getString(R.string.base_uri));
        Uri resultUri = baseUri.buildUpon()
                .appendEncodedPath(path)
                .build();
        assert resultUri != null;
        return resultUri.toString();
    }


}
