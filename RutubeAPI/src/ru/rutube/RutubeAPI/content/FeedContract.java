package ru.rutube.RutubeAPI.content;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */
public final class FeedContract {
    public static final String FEED = FeedContentProvider.AUTHORITY;

    public static final Uri FEED_URI = Uri.parse("content://" + FEED);

    public interface FeedColumns {
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String CREATED = "created";
        public static final String THUMBNAIL_URI = "thumbnail_url";
        public static final String AUTHOR_ID = "author_id";
        public static final String AUTHOR_NAME = "author_name";
        public static final String AVATAR_URI = "avatar_url";
    }

    public static final class Editors implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "editors";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + FEED + "." + CONTENT_PATH;
    }

    public static final class MyVideo implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "my_video";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + FEED + "." + CONTENT_PATH;

        public static final String SIGNATURE = "signature";
    }

    public static final class Subscriptions implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "subscriptions";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + FEED + "." + CONTENT_PATH;
    }
}
