package ru.rutube.RutubeAPI.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.models.FeedItem;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 13:05
 * To change this template use File | Settings | File Templates.
 */
public class FeedContentProvider extends ContentProvider {
    private static final String DBNAME = "rutube";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int EDITORS = 1;
    private static final int EDITORS_FEEDITEM = 2;
    private static final int MY_VIDEO = 3;
    private static final int MY_VIDEO_FEEDITEM = 4;
    private static final int SUBSCRIPTION = 5;
    private static final int SUBSCRIPTION_FEEDITEM = 6;
    private static final int SEARCH_RESULTS = 7;
    private static final int SEARCH_RESULTS_FEEDITEM = 8;
    private static final int SEARCH_QUERY = 9;
    private static final int SEARCH_QUERY_ITEM = 10;
    private static final int RELATED_VIDEO = 11;
    private static final int RELATED_VIDEO_ITEM = 12;
    private static final int AUTHOR_VIDEO = 13;
    private static final int AUTHOR_VIDEO_FEEDITEM = 14;
    private static final int TAGS_VIDEO = 15;
    private static final int TAGS_VIDEO_FEEDITEM = 16;
    private static final int NAVIGATION = 17;
    private static final int NAVIGATION_ITEM = 18;
    private static final int SHOWCASE_TABS = 19;
    private static final int SHOWCASE_TABITEM = 20;
    private static final int TABSOURCE = 21;
    private static final int TABSOURCE_ITEM = 22;
    private static final int TABSOURCE_ALL = 23;
    private static final int TVSHOW_VIDEO = 24;
    private static final int TVSHOW_VIDEOITEM = 25;
    private static final int PERSON_VIDEO = 26;
    private static final int PERSON_VIDEOITEM = 27;

    private static final String LOG_TAG = FeedContentProvider.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    public static final String AUTHORITY = FeedContentProvider.class.getName();

    static {
        sUriMatcher.addURI(AUTHORITY, FeedContract.Editors.CONTENT_PATH, EDITORS);
        sUriMatcher.addURI(AUTHORITY, FeedContract.Editors.CONTENT_PATH + "/#", EDITORS_FEEDITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.MyVideo.CONTENT_PATH, MY_VIDEO);
        sUriMatcher.addURI(AUTHORITY, FeedContract.MyVideo.CONTENT_PATH + "/#", MY_VIDEO_FEEDITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.Subscriptions.CONTENT_PATH, SUBSCRIPTION);
        sUriMatcher.addURI(AUTHORITY, FeedContract.Subscriptions.CONTENT_PATH + "/#", SUBSCRIPTION_FEEDITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.SearchResults.CONTENT_PATH + "/#", SEARCH_RESULTS);
        sUriMatcher.addURI(AUTHORITY, FeedContract.SearchResults.CONTENT_PATH + "/#/#", SEARCH_RESULTS_FEEDITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.SearchQuery.CONTENT_PATH, SEARCH_QUERY);
        sUriMatcher.addURI(AUTHORITY, FeedContract.SearchQuery.CONTENT_PATH + "/#", SEARCH_QUERY_ITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.RelatedVideo.CONTENT_PATH + "/*", RELATED_VIDEO);
        sUriMatcher.addURI(AUTHORITY, FeedContract.RelatedVideo.CONTENT_PATH + "/*/*", RELATED_VIDEO_ITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.AuthorVideo.CONTENT_PATH + "/#", AUTHOR_VIDEO);
        sUriMatcher.addURI(AUTHORITY, FeedContract.AuthorVideo.CONTENT_PATH + "/#/#", AUTHOR_VIDEO_FEEDITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.TagsVideo.CONTENT_PATH + "/#", TAGS_VIDEO);
        sUriMatcher.addURI(AUTHORITY, FeedContract.TagsVideo.CONTENT_PATH + "/#/#", TAGS_VIDEO_FEEDITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.Navigation.CONTENT_PATH, NAVIGATION);
        sUriMatcher.addURI(AUTHORITY, FeedContract.Navigation.CONTENT_PATH + "/#", NAVIGATION_ITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.ShowcaseTabs.CONTENT_PATH + "/#", SHOWCASE_TABS);
        sUriMatcher.addURI(AUTHORITY, FeedContract.ShowcaseTabs.CONTENT_PATH + "/#/#", SHOWCASE_TABITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.TabSources.CONTENT_PATH, TABSOURCE_ALL);
        sUriMatcher.addURI(AUTHORITY, FeedContract.TabSources.CONTENT_PATH + "/#", TABSOURCE);
        sUriMatcher.addURI(AUTHORITY, FeedContract.TabSources.CONTENT_PATH + "/#/#", TABSOURCE_ITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.TVShowVideo.CONTENT_PATH + "/#", TVSHOW_VIDEO);
        sUriMatcher.addURI(AUTHORITY, FeedContract.TVShowVideo.CONTENT_PATH + "/#/#", TVSHOW_VIDEOITEM);
        sUriMatcher.addURI(AUTHORITY, FeedContract.PersonVideo.CONTENT_PATH + "/#", PERSON_VIDEO);
        sUriMatcher.addURI(AUTHORITY, FeedContract.PersonVideo.CONTENT_PATH + "/#/#", PERSON_VIDEOITEM);
    }

    private MainDatabaseHelper dbHelper;

    private SQLiteDatabase db;


    @Override
    public boolean onCreate() {
        dbHelper = new MainDatabaseHelper(getContext());

        return true;
    }

    public String getTable(int uriType) {
        String table;
        switch (uriType) {
            case EDITORS:
                table = FeedContract.Editors.CONTENT_PATH;
                break;
            case MY_VIDEO:
                table = FeedContract.MyVideo.CONTENT_PATH;
                break;
            case SUBSCRIPTION:
                table = FeedContract.Subscriptions.CONTENT_PATH;
                break;
            case SEARCH_RESULTS:
                table = FeedContract.SearchResults.CONTENT_PATH;
                break;
            case SEARCH_QUERY:
                table = FeedContract.SearchQuery.CONTENT_PATH;
                break;
            case RELATED_VIDEO:
                table = FeedContract.RelatedVideo.CONTENT_PATH;
                break;
            case AUTHOR_VIDEO:
                table = FeedContract.AuthorVideo.CONTENT_PATH;
                break;
            case TAGS_VIDEO:
                table = FeedContract.TagsVideo.CONTENT_PATH;
                break;
            case NAVIGATION:
                table = FeedContract.Navigation.CONTENT_PATH;
                break;
            case SHOWCASE_TABS:
                table = FeedContract.ShowcaseTabs.CONTENT_PATH;
                break;
            case TABSOURCE_ALL:
            case TABSOURCE:
                table = FeedContract.TabSources.CONTENT_PATH;
                break;
            case TVSHOW_VIDEO:
                table = FeedContract.TVShowVideo.CONTENT_PATH;
                break;
            case PERSON_VIDEO:
                table = FeedContract.PersonVideo.CONTENT_PATH;
                break;
            default:
                throw new IllegalArgumentException("Unknown UriType: " + uriType);
        }
        return table;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sUriMatcher.match(uri);
        // Check if the caller has requested a column which does not exists
        checkColumns(projection, uriType);
        String table = getTable(uriType);
        queryBuilder.setTables(table);

        String where = computeWhere(uri, uriType);
        if (where != null) {
            queryBuilder.appendWhere(where);
        }


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (sortOrder == null)

            sortOrder = computeSortOrder(uriType);

        if (D) Log.d(LOG_TAG, "ORDER BY: " + sortOrder);
        assert db != null;

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        assert cursor != null;
        Context context = getContext();
        assert context != null;

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(context.getContentResolver(), uri);
        if (D && uriType == NAVIGATION) Log.d(LOG_TAG, "query results: " + String.valueOf(cursor.getCount()));
        return cursor;
    }

    public String computeSortOrder(int uriType) {
        switch(uriType) {
            case SEARCH_RESULTS:
                return FeedContract.SearchResults.POSITION;
            case NAVIGATION:
                return FeedContract.Navigation.POSITION;
            case SHOWCASE_TABS:
                return FeedContract.ShowcaseTabs.ORDER_NUMBER;
            case TVSHOW_VIDEO:
                return String.format("%s ASC, %s ASC",
                        FeedContract.TVShowVideo.SEASON,
                        FeedContract.TVShowVideo.EPISODE);
            case TABSOURCE:
            case TABSOURCE_ALL:
                return FeedContract.TabSources.ORDER_NUMBER;
            default:
                return FeedContract.FeedColumns.CREATED + " DESC";
        }
    }

    public String computeWhere(Uri uri, int uriType) {
        String where = null;
        String fk_field = null;

        switch (uriType) {
            case EDITORS:
            case SUBSCRIPTION:
            case MY_VIDEO:
            case NAVIGATION:
            case SEARCH_QUERY:
            case TABSOURCE_ALL:
                return null;
            // Получение одной строки по значению первичного ключа
            case EDITORS_FEEDITEM:
            case MY_VIDEO_FEEDITEM:
            case AUTHOR_VIDEO_FEEDITEM:
            case TAGS_VIDEO_FEEDITEM:
            case SUBSCRIPTION_FEEDITEM:
            case SEARCH_RESULTS_FEEDITEM:
            case SEARCH_QUERY_ITEM:
            case RELATED_VIDEO_ITEM:
            case SHOWCASE_TABITEM:
            case NAVIGATION_ITEM:
            case TABSOURCE_ITEM:
            case TVSHOW_VIDEOITEM:
            case PERSON_VIDEOITEM:
                where = String.format("%s = '%s'", BaseColumns._ID, uri.getLastPathSegment());
                break;
            // Получение списка строк отфильтрованных по значению внешнего ключа
            case AUTHOR_VIDEO:
                fk_field = FeedContract.AuthorVideo.AUTHOR_ID;
                break;
            case TAGS_VIDEO:
                fk_field = FeedContract.TagsVideo.TAG_ID;
                break;
            case SEARCH_RESULTS:
                fk_field = FeedContract.SearchResults.QUERY_ID;
                break;
            case RELATED_VIDEO:
                fk_field = FeedContract.RelatedVideo.RELATED_VIDEO_ID;
                break;
            case SHOWCASE_TABS:
                fk_field = FeedContract.ShowcaseTabs.SHOWCASE_ID;
                break;
            case TABSOURCE:
                fk_field = FeedContract.TabSources.TAB_ID;
                break;
            case TVSHOW_VIDEO:
                fk_field = FeedContract.TVShowVideo.TVSHOW_ID;
                break;
            case PERSON_VIDEO:
                fk_field = FeedContract.PersonVideo.PERSON_ID;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (fk_field != null) {
            List<String> segments = uri.getPathSegments();
            assert segments != null;
            if (D) Log.d(LOG_TAG, String.valueOf(segments));
            // путь выглядит так: /content_path/1
            // соответственно, нужен 2 сегмент
            where = String.format("%s = '%s'", fk_field , segments.get(1));
        }
        return where;
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EDITORS:
                return FeedContract.Editors.CONTENT_TYPE;
            case MY_VIDEO:
                return FeedContract.MyVideo.CONTENT_TYPE;
            case SUBSCRIPTION:
                return FeedContract.Subscriptions.CONTENT_TYPE;
            case SEARCH_RESULTS:
                return FeedContract.SearchResults.CONTENT_TYPE;
            case SEARCH_QUERY:
                return FeedContract.SearchQuery.CONTENT_TYPE;
            case RELATED_VIDEO:
                return FeedContract.RelatedVideo.CONTENT_TYPE;
            case AUTHOR_VIDEO:
                return FeedContract.AuthorVideo.CONTENT_TYPE;
            case TAGS_VIDEO:
                return FeedContract.TagsVideo.CONTENT_TYPE;
            case NAVIGATION:
                return FeedContract.Navigation.CONTENT_TYPE;
            case SHOWCASE_TABS:
                return FeedContract.ShowcaseTabs.CONTENT_TYPE;
            case TABSOURCE_ALL:
            case TABSOURCE:
                return FeedContract.TabSources.CONTENT_TYPE;
            case TVSHOW_VIDEO:
                return FeedContract.TVShowVideo.CONTENT_TYPE;
            case PERSON_VIDEO:
                return FeedContract.PersonVideo.CONTENT_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        if (D) Log.d(LOG_TAG, "Insert uri: " + uri.toString());
        int uriType = sUriMatcher.match(uri);
        if (D) Log.d(LOG_TAG, "Type: " + String.valueOf(uriType));
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        String table = getTable(uriType);
        assert sqlDB != null;
        long rowId = sqlDB.replace(table, null, contentValues);
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null);
        return uri.buildUpon().appendPath(String.valueOf(rowId)).build();
    }

    @Override
    public int bulkInsert(Uri uri, @NotNull ContentValues[] values) {
        if (D) Log.d(LOG_TAG, "start bulk insert");
        int numInserted = 0;
        if (values.length == 0) {
            if (D) Log.d(LOG_TAG, "empty bulk insert");
            return 0;
        }
        int uriType = sUriMatcher.match(uri);
        checkColumns(values[0], uriType);
        String table = getTable(uriType);

        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        assert sqlDB != null;
        sqlDB.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = sqlDB.replace(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            sqlDB.setTransactionSuccessful();
            numInserted = values.length;
        } finally {
            sqlDB.endTransaction();
        }
        if (D) Log.d(LOG_TAG, String.format("end bulk insert: %d of %d inserted", numInserted, values.length));
        return numInserted;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        Context context = getContext();
        assert context != null;
        int uriType = sUriMatcher.match(uri);
        if (D) Log.d(LOG_TAG, "Delete: " + String.valueOf(uri));
        String table = getTable(uriType);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        assert sqlDB != null;
        final String base_query = "SELECT COUNT(*) as cnt FROM " + table;
        if (D) {
            Cursor c = sqlDB.rawQuery(base_query, null);
            c.moveToFirst();
            int count = c.getInt(c.getColumnIndex("cnt"));
            Log.d(LOG_TAG, "Before delete: " + String.valueOf(count));
            c.close();
            if (where != null) {
                c = sqlDB.rawQuery("SELECT COUNT(*) as cnt FROM " + table + " WHERE " + where, whereArgs);
                c.moveToFirst();
                count = c.getInt(c.getColumnIndex("cnt"));
                Log.d(LOG_TAG, "Delete Where: " + where);
                Log.d(LOG_TAG, "For delete: " + String.valueOf(count));
                c.close();
            }
        }
        sqlDB.delete(table, where, whereArgs);
        if (D) {
            Cursor c = sqlDB.rawQuery(base_query, null);
            c.moveToFirst();
            int count = c.getInt(c.getColumnIndex("cnt"));
            c.close();
            Log.d(LOG_TAG, "After delete: " + String.valueOf(count));
        }
        context.getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        Context context = getContext();
        assert context != null;
        int uriType = sUriMatcher.match(uri);
        if (D) Log.d(LOG_TAG, "Delete: " + String.valueOf(uri));
        String table = getTable(uriType);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        assert sqlDB != null;
        return sqlDB.update(table, contentValues, s, strings);
    }

    private void checkColumns(ContentValues values, int uriType) {
        // ContentValues.keySet in 2.3 just stops the whole thread without exception
        if (values != null) {
            HashSet<String> requestedColumns = new HashSet<String>();
            for (Map.Entry<String, Object> entry : values.valueSet()) {
                requestedColumns.add(entry.getKey());
            }
            checkColumns(requestedColumns, uriType);
        }
    }

    private void checkColumns(HashSet<String> requestedColumns, int uriType) {
        String[] columnList = getProjection(uriType);

        HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(columnList));
        if (!availableColumns.containsAll(requestedColumns)) {
            if (D) Log.d(LOG_TAG, "checkColumns");
            if (D) Log.d(LOG_TAG, String.valueOf(availableColumns));
            if (D) Log.d(LOG_TAG, String.valueOf(requestedColumns));
            throw new IllegalArgumentException("Unknown columns in projection");
        }
    }

    public static String[] getProjection(Uri contentUri) {
        int uriType = sUriMatcher.match(contentUri);
        return getProjection(uriType);
    }

    public static String[] getProjection(int uriType) {
        // Всякие разные ленты со стандартным набором колонок и дополнительными колонками,
        // уникальными для разных типов лент.
        final String[] available = {
                FeedContract.FeedColumns._ID,
                FeedContract.FeedColumns.TITLE,
                FeedContract.FeedColumns.DESCRIPTION,
                FeedContract.FeedColumns.CREATED,
                FeedContract.FeedColumns.THUMBNAIL_URI,
                FeedContract.FeedColumns.AUTHOR_ID,
                FeedContract.FeedColumns.AUTHOR_NAME,
                FeedContract.FeedColumns.AVATAR_URI,
                FeedContract.FeedColumns.DURATION,
                FeedContract.FeedColumns.CACHED
        };
        ArrayList<String> columnList = new ArrayList<String>(Arrays.asList(available));
        if (D) Log.d(LOG_TAG, "Get projection for " + String.valueOf(uriType));
        switch (uriType) {
            case SEARCH_QUERY:
            case SEARCH_QUERY_ITEM:
                return new String[]{
                        BaseColumns._ID,
                        FeedContract.SearchQuery.QUERY,
                        FeedContract.SearchQuery.UPDATED
                };
            case NAVIGATION:
            case NAVIGATION_ITEM:
                return new String[]{
                        BaseColumns._ID,
                        FeedContract.Navigation.NAME,
                        FeedContract.Navigation.TITLE,
                        FeedContract.Navigation.LINK,
                        FeedContract.Navigation.POSITION
                };
            case SHOWCASE_TABS:
            case SHOWCASE_TABITEM:
                return new String[]{
                        BaseColumns._ID,
                        FeedContract.ShowcaseTabs.NAME,
                        FeedContract.ShowcaseTabs.SORT,
                        FeedContract.ShowcaseTabs.ORDER_NUMBER,
                        FeedContract.ShowcaseTabs.SHOWCASE_ID,
                };
            case TABSOURCE:
            case TABSOURCE_ALL:
            case TABSOURCE_ITEM:
                return new String[]{
                        BaseColumns._ID,
                        FeedContract.TabSources.TAB_ID,
                        FeedContract.TabSources.CONTENT_TYPE_ID,
                        FeedContract.TabSources.ORDER_NUMBER,
                        FeedContract.TabSources.LINK,
                };
            case MY_VIDEO:
            case MY_VIDEO_FEEDITEM:
                columnList.add(FeedContract.MyVideo.SIGNATURE);
                break;
            case SEARCH_RESULTS:
            case SEARCH_RESULTS_FEEDITEM:
                columnList.add(FeedContract.SearchResults.QUERY_ID);
                columnList.add(FeedContract.SearchResults.POSITION);
                break;
            case TVSHOW_VIDEO:
            case TVSHOW_VIDEOITEM:
                columnList.add(FeedContract.TVShowVideo.TVSHOW_ID);
                columnList.add(FeedContract.TVShowVideo.METAINFO);
                columnList.add(FeedContract.TVShowVideo.SEASON);
                columnList.add(FeedContract.TVShowVideo.EPISODE);
                columnList.add(FeedContract.TVShowVideo.TYPE);
                break;
            case RELATED_VIDEO:
            case RELATED_VIDEO_ITEM:
                columnList.add(FeedContract.RelatedVideo.RELATED_VIDEO_ID);
                columnList.add(FeedContract.RelatedVideo.POSITION);
                columnList.add(FeedContract.RelatedVideo.HITS);
                break;
            case SUBSCRIPTION:
            case SUBSCRIPTION_FEEDITEM:
                columnList.add(FeedContract.Subscriptions.TAGS_JSON);
                break;
            case TAGS_VIDEO:
            case TAGS_VIDEO_FEEDITEM:
                columnList.add(FeedContract.Subscriptions.TAGS_JSON);
                columnList.add(FeedContract.TagsVideo.TAG_ID);
                break;
            case PERSON_VIDEO:
            case PERSON_VIDEOITEM:
                columnList.add(FeedContract.PersonVideo.PERSON_ID);
                break;
            case -1:
                throw new IllegalArgumentException("Invalid uri type");
            default:
                break;

        }
        String[] result = new String[columnList.size()];
        return columnList.toArray(result);
    }

    private void checkColumns(String[] values, int uriType) {
        HashSet<String> requestedColumns = new HashSet<String>();
        if (values != null) {
            Collections.addAll(requestedColumns, values);
            checkColumns(requestedColumns, uriType);
        }
    }

    private static final String SEARCH_QUERY_COLUMNS_SQL =
            " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " query VARCHAR(100)," +
            " updated DATETIME";

    private static final String FEED_COLUMNS_SQL =
            " _id VARCHAR(32) PRIMARY KEY," +
            " title VARCHAR(255)," +
            " description VARCHAR(1024)," +
            " thumbnail_url VARCHAR(255)," +
            " created DATETIME," +
            " author_id INTEGER NULL," +
            " author_name VARCHAR(120)," +
            " avatar_url VARCHAR(255)," +
            " duration INTEGER NULL," +
            " cached_ts DATETIME DEFAULT (datetime('now','localtime'))";

    private static final String MY_VIDEO_COLUMNS_SQL =
            FEED_COLUMNS_SQL + "," +
                    " signature VARCHAR(30) NULL";

    private static final String SUBSCRIPTIONS_COLUMNS_SQL =
            FEED_COLUMNS_SQL + "," +
                    " tags_json TEXT NULL";

    private static final String TAGS_VIDEO_COLUMNS_SQL =
            SUBSCRIPTIONS_COLUMNS_SQL + "," +
                    " tag_id INTEGER NOT NULL";

    private static final String SEARCH_RESULTS_COLUMNS_SQL =
            FEED_COLUMNS_SQL + "," +
                    " query_id INTEGER NOT NULL," +
                    " position INTEGER NOT NULL";

    private static final String NAVIGATION_COLUMNS_SQL =
            " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " name VARCHAR(50)," +
                    " title VARCHAR(200)," +
                    " link VARCHAR(200)," +
                    " position INTEGER DEFAULT 0";

    private static final String SHOWCASE_TABS_COLUMNS_SQL =
            " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " name VARCHAR(50)," +
                    " sort VARCHAR(200)," +
                    " showcase_id INTEGER NULL," +
                    " order_number INTEGER DEFAULT 0";

    private static final String TABSOURCE_COLUMNS_SQL =
            " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " link VARCHAR(200)," +
                    " content_type_id INTEGER NULL," +
                    " tab_id INTEGER NULL," +
                    " order_number INTEGER DEFAULT 0";

    private static final String TVSHOW_VIDEO_COLUMNS_SQL =
            FEED_COLUMNS_SQL + "," +
                    " tvshow_id INTEGER NULL," +
                    " metainfo TEXT null, " +
                    " season INTEGER DEFAULT 0," +
                    " episode INTEGER DEFAULT 0," +
                    " type INTEGER DEFAULT 0";

    private static final String PERSON_VIDEO_COLUMNS_SQL =
            FEED_COLUMNS_SQL + "," +
                    " person_id INTEGER NULL";

    private static final String NAVIGATION_FIXTURE_QUERY = "INSERT INTO " +
            FeedContract.Navigation.CONTENT_PATH + " (name, title, link, position) ";

    private static final String SHOWCASE_FIXTURE_QUERY = "INSERT INTO " +
            FeedContract.ShowcaseTabs.CONTENT_PATH + " (showcase_id, name, sort, order_number) ";

    private static final String RELATED_VIDEO_COLUMNS_SQL =
            FEED_COLUMNS_SQL + "," +
                    " related_video_id INTEGER NOT NULL," +
                    " position INTEGER NOT NULL," +
                    " hits INTEGER NOT NULL";

    private static final String SQL_CREATE_VIDEO_EDITORS = "CREATE TABLE " +
            FeedContract.Editors.CONTENT_PATH + " (" +
            FEED_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_VIDEO_MY_VIDEO = "CREATE TABLE " +
            FeedContract.MyVideo.CONTENT_PATH + " (" +
            MY_VIDEO_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_VIDEO_SUBSCRIPTION = "CREATE TABLE " +
            FeedContract.Subscriptions.CONTENT_PATH + " (" +
            SUBSCRIPTIONS_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_SEARCH_RESULTS = "CREATE TABLE " +
            FeedContract.SearchResults.CONTENT_PATH + " (" +
            SEARCH_RESULTS_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_SEARCH_QUERY = "CREATE TABLE " +
            FeedContract.SearchQuery.CONTENT_PATH + " (" +
            SEARCH_QUERY_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_NAVIGATION = "CREATE TABLE " +
            FeedContract.Navigation.CONTENT_PATH + " (" +
            NAVIGATION_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_SHOWCASE_TABS = "CREATE TABLE " +
            FeedContract.ShowcaseTabs.CONTENT_PATH + " (" +
            SHOWCASE_TABS_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_TABSOURCE = "CREATE TABLE " +
            FeedContract.TabSources.CONTENT_PATH + " (" +
            TABSOURCE_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_AUTHOR_QUERY = "CREATE TABLE " +
            FeedContract.AuthorVideo.CONTENT_PATH + " (" +
            FEED_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_TAGS_VIDEO_QUERY = "CREATE TABLE " +
            FeedContract.TagsVideo.CONTENT_PATH + " (" +
            TAGS_VIDEO_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_RELATED_VIDEO = "CREATE TABLE " +
            FeedContract.RelatedVideo.CONTENT_PATH + " (" +
            RELATED_VIDEO_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_TVSHOW_VIDEO_QUERY = "CREATE TABLE " +
            FeedContract.TVShowVideo.CONTENT_PATH + " (" +
            TVSHOW_VIDEO_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_PERSON_VIDEO_QUERY = "CREATE TABLE " +
            FeedContract.PersonVideo.CONTENT_PATH + " (" +
            PERSON_VIDEO_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_INDEX_NAVIGATION = "CREATE UNIQUE INDEX idx_navigation_link " +
            "ON " + FeedContract.Navigation.CONTENT_PATH + " (" + FeedContract.Navigation.LINK  + ")";

    private static final String SQL_DROP_TABLE = "DROP TABLE %s";

    private static final int DB_VERSION = 13;

    private static String getNaviFixture() {
        Context context = RutubeApp.getContext();
        String name = context.getString(R.string.navi_main_name);
        String title = context.getString(R.string.navi_main_title);
        String link = context.getString(R.string.navi_main_link);
        return String.format("VALUES('%s', '%s', '%s', 0)", name, title, link);
    }

    private static String[] getShowcaseTabsFixture(int showcaseId) {
        Context context = RutubeApp.getContext();
        // получаем витрину "андроид"
        String[] tab_names = context.getResources().getStringArray(R.array.showcase_tabs_names);
        String[] result = new String[tab_names.length];
        int i = 0;
        for (String tabName: tab_names) {
            result[i] = String.format("VALUES (%d, '%s', 'created_date', %d)", showcaseId, tabName, i);
            i += 1;
        }
        return result;
    }

    public static String[] computeWhere(int feedType, Uri feedUri) {
        List<String> parts = new ArrayList<String>();
        String where;
        List<String> args = new ArrayList<String>();
        switch(feedType){
            case ContentMatcher.TVSHOWVIDEO:
                String show_all = feedUri.getQueryParameter("show_all");
                boolean zero_episode = "1".equals(show_all);

                String season = feedUri.getQueryParameter("season");
                if (season != null) {
                    parts.add(zero_episode ? "(season = ? OR season = 0)" : "season = ?");
                    args.add(season);
                }
                String episode = feedUri.getQueryParameter("episode");
                if (episode != null) {
                    parts.add(zero_episode ? "(episode = ? OR episode = 0)" : "episode = ?");
                    args.add(episode);
                }
                String type = feedUri.getQueryParameter("type");
                if (type != null) {
                    parts.add("type = ?");
                    args.add(type);
                }
                where = TextUtils.join(" AND ", parts);
                args.add(0, where);
                if (D) Log.d(LOG_TAG, "ARGS: " + String.valueOf(args));
                String[] result = new String[args.size()];
                return args.toArray(result);
            default:
                return null;
        }
    }

    public static String computeSortOrder(int feedType, Uri feedUri) {
        String sort = feedUri.getQueryParameter("sort");
        if (sort == null)
            return null;
        String[] parts = sort.split("_");
        if (parts.length != 2)
            return null;
        String order = (parts[1].equals("d"))?" DESC":" ASC";
        String field = parts[0];
        switch(feedType) {
            case ContentMatcher.TVSHOWVIDEO:
                if ("series".equals(field))
                    return String.format("season %s, episode %s", order, order);
                if ("created".equals(field))
                    return String.format("created %s", order);
                return null;
            default:
                return null;
        }
    }

    protected static final class MainDatabaseHelper extends SQLiteOpenHelper {
        /**
         * Instantiates an open helper for the provider's SQLite data repository
         * Do not do database creation and upgrade here.
         */
        MainDatabaseHelper(Context context) {
            super(context, DBNAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            if (D) Log.d(LOG_TAG, "Creating database");
            db.execSQL(SQL_CREATE_VIDEO_EDITORS);
            db.execSQL(SQL_CREATE_VIDEO_MY_VIDEO);
            db.execSQL(SQL_CREATE_VIDEO_SUBSCRIPTION);
            db.execSQL(SQL_CREATE_SEARCH_RESULTS);
            db.execSQL(SQL_CREATE_SEARCH_QUERY);
            db.execSQL(SQL_CREATE_RELATED_VIDEO);
            db.execSQL(SQL_CREATE_AUTHOR_QUERY);
            db.execSQL(SQL_CREATE_TAGS_VIDEO_QUERY);
            db.execSQL(SQL_CREATE_NAVIGATION);
            db.execSQL(SQL_CREATE_SHOWCASE_TABS);
            db.execSQL(SQL_CREATE_TABSOURCE);
            db.execSQL(NAVIGATION_FIXTURE_QUERY + getNaviFixture());
            int showcaseId = getAndroidShowcaseId(db);
            for (String fix: getShowcaseTabsFixture(showcaseId))
                db.execSQL(SHOWCASE_FIXTURE_QUERY + fix);
            db.execSQL(SQL_CREATE_INDEX_NAVIGATION);
            if (D)Log.d(LOG_TAG, SQL_CREATE_TVSHOW_VIDEO_QUERY);
            db.execSQL(SQL_CREATE_TVSHOW_VIDEO_QUERY);
            db.execSQL(SQL_CREATE_PERSON_VIDEO_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int from, int to) {
            //To change body of implemented methods use File | Settings | File Templates.
            if (D) Log.d(LOG_TAG, String.format("Upgrading db from %d to %d", from, to));
            for (int v=from; v<to; v++) {
                switch(v) {
                    case 1:
                        db.execSQL("ALTER TABLE " + FeedContract.Subscriptions.CONTENT_PATH +
                                " ADD COLUMN tags_json TEXT NULL DEFAULT '[]'");
                        break;
                    case 2:
                        db.execSQL(SQL_CREATE_RELATED_VIDEO);
                        break;
                    case 3:
                        // писать кучу дополнительного кода ради ALTER TABLE ADD COLUMN не хочется
                        // поэтому тупо удаляем модифицируемые таблицы и создаем их заново.
                        final String[] tables = {
                                FeedContract.Editors.CONTENT_PATH,
                                FeedContract.MyVideo.CONTENT_PATH,
                                FeedContract.Subscriptions.CONTENT_PATH,
                                FeedContract.SearchResults.CONTENT_PATH,
                                FeedContract.RelatedVideo.CONTENT_PATH
                        };
                        for (String table : tables) {
                            db.execSQL(String.format(SQL_DROP_TABLE, table));
                        }
                        db.execSQL(SQL_CREATE_VIDEO_EDITORS);
                        db.execSQL(SQL_CREATE_VIDEO_MY_VIDEO);
                        db.execSQL(SQL_CREATE_VIDEO_SUBSCRIPTION);
                        db.execSQL(SQL_CREATE_SEARCH_RESULTS);
                        db.execSQL(SQL_CREATE_RELATED_VIDEO);
                        break;
                    case 4:
                        db.execSQL(SQL_CREATE_AUTHOR_QUERY);
                        break;
                    case 5:
                        db.execSQL(SQL_CREATE_TAGS_VIDEO_QUERY);
                        break;
                    case 6:
                        final String[] tables6 = {
                                FeedContract.Editors.CONTENT_PATH,
                                FeedContract.MyVideo.CONTENT_PATH,
                                FeedContract.Subscriptions.CONTENT_PATH,
                                FeedContract.SearchResults.CONTENT_PATH,
                                FeedContract.RelatedVideo.CONTENT_PATH,
                                FeedContract.AuthorVideo.CONTENT_PATH,
                                FeedContract.TagsVideo.CONTENT_PATH,
                        };
                        String dt = FeedItem.sSqlDateTimeFormat.format(new Date());
                        for (String t : tables6) {
                            String query = String.format("ALTER TABLE %s ADD COLUMN %s DATETIME" +
                                    " DEFAULT '%s'", t,
                                    FeedContract.FeedColumns.CACHED,
                                    dt);
                            db.execSQL(query);
                        }
                        break;
                    case 7:
                        db.execSQL(SQL_CREATE_NAVIGATION);
                        db.execSQL(NAVIGATION_FIXTURE_QUERY + getNaviFixture());
                        break;
                    case 8:
                        db.execSQL(SQL_CREATE_SHOWCASE_TABS);
                        int showcaseId = getAndroidShowcaseId(db);
                        for (String fix: getShowcaseTabsFixture(showcaseId))
                            db.execSQL(SHOWCASE_FIXTURE_QUERY + fix);
                        break;
                    case 9:
                        db.execSQL(SQL_CREATE_INDEX_NAVIGATION);
                        break;
                    case 10:
                        db.execSQL(SQL_CREATE_TABSOURCE);
                        break;
                    case 11:
                        db.execSQL(SQL_CREATE_TVSHOW_VIDEO_QUERY);
                        break;
                    case 12:
                        db.execSQL(SQL_CREATE_PERSON_VIDEO_QUERY);
                    default:
                        break;
                }
            }
        }

        public int getAndroidShowcaseId(SQLiteDatabase db) {
            String androidFeedUri = RutubeApp.getContext().getString(R.string.navi_main_link);
            Cursor c = db.query(
                    FeedContract.Navigation.CONTENT_PATH,
                    new String[]{BaseColumns._ID},
                    "link = ?",
                    new String[]{androidFeedUri},
                    null, null, null
            );
            assert c!= null;
            c.moveToFirst();
            if (c.getCount() == 0) {
                if (D) Log.d(LOG_TAG, "android showcase: " + androidFeedUri + " not in db");
                c.close();
                c = db.query(
                        FeedContract.Navigation.CONTENT_PATH,
                        new String[]{BaseColumns._ID},
                        null,
                        null,
                        null, null, null);
                c.moveToFirst();
            }
            int showcaseId = c.getInt(0);
            c.close();
            return showcaseId;
        }
    }

}
