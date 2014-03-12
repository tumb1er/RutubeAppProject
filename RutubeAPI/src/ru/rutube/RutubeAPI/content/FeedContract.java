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
    public static final String VND = "vnd.android.cursor.dir/vnd.";

    public interface FeedColumns {
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String CREATED = "created";
        public static final String THUMBNAIL_URI = "thumbnail_url";
        public static final String AUTHOR_ID = "author_id";
        public static final String AUTHOR_NAME = "author_name";
        public static final String AVATAR_URI = "avatar_url";
        public static final String DURATION = "duration";
        public static final String CACHED = "cached_ts";
    }

    public interface TagsColumns {
        public static final String TAGS_JSON = "tags_json";
    }

    public static final class Editors implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "editors";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;
    }

    public static final class MyVideo implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "my_video";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;

        public static final String SIGNATURE = "signature";
    }

    public static final class AuthorVideo implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "author_video";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;
    }

    public static final class Subscriptions implements BaseColumns, FeedColumns, TagsColumns {
        public static final String CONTENT_PATH = "subscriptions";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;
    }

    public static final class SearchResults implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "search_results";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;

        public static final String QUERY_ID = "query_id";
        public static final String POSITION = "position";
    }

    public static final class SearchQuery implements BaseColumns {
        public static final String CONTENT_PATH = "search_query";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;

        public static final String QUERY = "query";
        public static final String UPDATED = "updated";
    }

    public static final class Navigation implements BaseColumns {
        public static final String CONTENT_PATH = "navigation";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;

        public static final String NAME = "name";
        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String POSITION = "position";
    }

    public static final class ShowcaseTabs implements BaseColumns {
        public static final String CONTENT_PATH = "showcase_tabs";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;

        public static final String NAME = "name";
        public static final String SORT = "sort";
        public static final String ORDER_NUMBER = "order_number";
        public static final String SHOWCASE_ID = "showcase_id";
    }

    public static final class RelatedVideo implements BaseColumns, FeedColumns {
        public static final String CONTENT_PATH = "related_video";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;

        public static final String RELATED_VIDEO_ID = "related_video_id";
        public static final String POSITION = "position";

        public static final String HITS = "hits";
    }

    public static final class TagsVideo implements  BaseColumns, FeedColumns, TagsColumns {
        public static final String CONTENT_PATH = "tags_video";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(FEED_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = VND + FEED + "." + CONTENT_PATH;

        public static final String TAG_ID = "tag_id";
    }
}
