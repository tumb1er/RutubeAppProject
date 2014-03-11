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
    }

    private MainDatabaseHelper dbHelper;

    private SQLiteDatabase db;


    @Override
    public boolean onCreate() {
        dbHelper = new MainDatabaseHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();



        int uriType = sUriMatcher.match(uri);
        // Check if the caller has requested a column which does not exists
        checkColumns(projection, uriType);
        switch (uriType) {
            case EDITORS:
                queryBuilder.setTables(FeedContract.Editors.CONTENT_PATH);
                break;
            case EDITORS_FEEDITEM:
                queryBuilder.setTables(FeedContract.Editors.CONTENT_PATH);
                queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case MY_VIDEO:
                queryBuilder.setTables(FeedContract.MyVideo.CONTENT_PATH);
                break;
            case MY_VIDEO_FEEDITEM:
                queryBuilder.setTables(FeedContract.MyVideo.CONTENT_PATH);
                queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case AUTHOR_VIDEO:
                queryBuilder.setTables(FeedContract.AuthorVideo.CONTENT_PATH);
                List<String> authorPathSegments = uri.getPathSegments();
                assert authorPathSegments != null;
                if (D) Log.d(LOG_TAG, String.valueOf(authorPathSegments));
                // путь выглядит так: /author_video/1
                // соответственно, нужен 2 сегмент
                String authorId = authorPathSegments.get(1);
                assert authorId != null;
                queryBuilder.appendWhere(FeedContract.AuthorVideo.AUTHOR_ID + "="
                        + authorId);

                break;
            case AUTHOR_VIDEO_FEEDITEM:
                queryBuilder.setTables(FeedContract.AuthorVideo.CONTENT_PATH);
                queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case TAGS_VIDEO:
                queryBuilder.setTables(FeedContract.TagsVideo.CONTENT_PATH);
                List<String> tagsPathSegments = uri.getPathSegments();
                assert tagsPathSegments != null;
                if (D) Log.d(LOG_TAG, String.valueOf(tagsPathSegments));
                // путь выглядит так: /tags_video/1
                // соответственно, нужен 2 сегмент
                String tagId = tagsPathSegments.get(1);
                assert tagId != null;
                queryBuilder.appendWhere(FeedContract.TagsVideo.TAG_ID + "="
                        + tagId);

                break;
            case TAGS_VIDEO_FEEDITEM:
                queryBuilder.setTables(FeedContract.TagsVideo.CONTENT_PATH);
                queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case SUBSCRIPTION:
                queryBuilder.setTables(FeedContract.Subscriptions.CONTENT_PATH);
                break;
            case SUBSCRIPTION_FEEDITEM:
                queryBuilder.setTables(FeedContract.Subscriptions.CONTENT_PATH);
                queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case SEARCH_RESULTS:
                queryBuilder.setTables(FeedContract.SearchResults.CONTENT_PATH);
                List<String> searchPathSegments = uri.getPathSegments();
                assert searchPathSegments != null;
                if (D) Log.d(LOG_TAG, String.valueOf(searchPathSegments));
                // путь выглядит так: /search_results/1
                // соответственно, нужен 2 сегмент
                String searchQueryId = searchPathSegments.get(1);
                assert searchQueryId != null;
                queryBuilder.appendWhere(FeedContract.SearchResults.QUERY_ID + "="
                        + searchQueryId);
                break;
            case SEARCH_RESULTS_FEEDITEM:
                queryBuilder.setTables(FeedContract.SearchResults.CONTENT_PATH);
                queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case SEARCH_QUERY:
                queryBuilder.setTables(FeedContract.SearchQuery.CONTENT_PATH);
                break;
            case SEARCH_QUERY_ITEM:
                queryBuilder.setTables(FeedContract.SearchQuery.CONTENT_PATH);
                queryBuilder.appendWhere(BaseColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case RELATED_VIDEO:
                queryBuilder.setTables(FeedContract.RelatedVideo.CONTENT_PATH);
                List<String> relatedPathSegments = uri.getPathSegments();
                assert relatedPathSegments != null;
                if (D) Log.d(LOG_TAG, String.valueOf(relatedPathSegments));
                // путь выглядит так: /related/<video_id>
                // соответственно, нужен 2 сегмент
                String relatedVideId = relatedPathSegments.get(1);
                assert relatedVideId != null;
                queryBuilder.appendWhere(FeedContract.RelatedVideo.RELATED_VIDEO_ID + "= '"
                        + relatedVideId + "'");
                break;
            case RELATED_VIDEO_ITEM:
                queryBuilder.setTables(FeedContract.RelatedVideo.CONTENT_PATH);
                queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            case NAVIGATION:
                if (D) Log.d(LOG_TAG, "query nav:" + selection);
                queryBuilder.setTables(FeedContract.Navigation.CONTENT_PATH);
                break;
            case NAVIGATION_ITEM:
                queryBuilder.setTables(FeedContract.Navigation.CONTENT_PATH);
                queryBuilder.appendWhere(BaseColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (sortOrder == null) {
            if (uriType == SEARCH_RESULTS)
                sortOrder = FeedContract.SearchResults.POSITION;
            else if (uriType == NAVIGATION)
                sortOrder = FeedContract.Navigation.POSITION;
            else
                sortOrder = FeedContract.FeedColumns.CREATED + " DESC";
        }
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
        long rowId;
        assert sqlDB != null;
        switch (uriType) {
            case EDITORS:
                rowId = sqlDB.replace(FeedContract.Editors.CONTENT_PATH, null, contentValues);
                break;
            case MY_VIDEO:
                rowId = sqlDB.replace(FeedContract.MyVideo.CONTENT_PATH, null, contentValues);
                break;
            case SUBSCRIPTION:
                rowId = sqlDB.replace(FeedContract.Subscriptions.CONTENT_PATH, null, contentValues);
                break;
            case SEARCH_RESULTS:
                rowId = sqlDB.replace(FeedContract.SearchResults.CONTENT_PATH, null, contentValues);
                break;
            case SEARCH_QUERY:
                rowId = sqlDB.replace(FeedContract.SearchQuery.CONTENT_PATH, null, contentValues);
                break;
            case RELATED_VIDEO:
                rowId = sqlDB.replace(FeedContract.RelatedVideo.CONTENT_PATH, null, contentValues);
                break;
            case AUTHOR_VIDEO:
                rowId = sqlDB.replace(FeedContract.AuthorVideo.CONTENT_PATH, null, contentValues);
                break;
            case TAGS_VIDEO:
                rowId = sqlDB.replace(FeedContract.TagsVideo.CONTENT_PATH, null, contentValues);
                break;
            case NAVIGATION:
                rowId = sqlDB.replace(FeedContract.Navigation.CONTENT_PATH, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null);

        switch (uriType) {
            case SEARCH_RESULTS:
                return FeedContract.SearchResults.CONTENT_URI.buildUpon()
                        .appendEncodedPath(contentValues.getAsString(FeedContract.SearchResults.QUERY_ID))
                        .appendEncodedPath(String.valueOf(rowId)).build();
            case SEARCH_QUERY:
                return Uri.withAppendedPath(FeedContract.SearchQuery.CONTENT_URI,
                        String.valueOf(rowId));
            case EDITORS:
                return Uri.withAppendedPath(FeedContract.Editors.CONTENT_URI,
                        String.valueOf(rowId));
            case MY_VIDEO:
                return Uri.withAppendedPath(FeedContract.MyVideo.CONTENT_URI,
                        String.valueOf(rowId));
            case SUBSCRIPTION:
                return Uri.withAppendedPath(FeedContract.Subscriptions.CONTENT_URI,
                        String.valueOf(rowId));
            case RELATED_VIDEO:
                return FeedContract.RelatedVideo.CONTENT_URI.buildUpon()
                        .appendEncodedPath(contentValues.getAsString(FeedContract.RelatedVideo.RELATED_VIDEO_ID))
                        .appendEncodedPath(String.valueOf(rowId)).build();
            case AUTHOR_VIDEO:
                return Uri.withAppendedPath(FeedContract.AuthorVideo.CONTENT_URI,
                        String.valueOf(rowId));
            case TAGS_VIDEO:
                return Uri.withAppendedPath(FeedContract.TagsVideo.CONTENT_URI,
                        String.valueOf(rowId));
            case NAVIGATION:
                return Uri.withAppendedPath(FeedContract.Navigation.CONTENT_URI,
                        String.valueOf(rowId));
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public int bulkInsert(Uri uri, @NotNull ContentValues[] values) {
        if (D) Log.d(LOG_TAG, "start bulk insert");
        int numInserted = 0;
        if (values.length == 0) {
            if (D) Log.d(LOG_TAG, "empty bulk insert");
            return 0;
        }
        String table;
        int uriType = sUriMatcher.match(uri);
        checkColumns(values[0], uriType);

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
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
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
        String table;
        int uriType = sUriMatcher.match(uri);
        if (D) Log.d(LOG_TAG, "Delete: " + String.valueOf(uri));
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
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
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
        throw new NullPointerException();
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
            throw new IllegalArgumentException("Unknown columns in projection");
        }

    }

    public static String[] getProjection(Uri contentUri) {
        int uriType = sUriMatcher.match(contentUri);
        return getProjection(uriType);
    }
    public static String[] getProjection(int uriType) {
        if (uriType == SEARCH_QUERY || uriType == SEARCH_QUERY_ITEM) {
            return new String[]{
                    BaseColumns._ID,
                    FeedContract.SearchQuery.QUERY,
                    FeedContract.SearchQuery.UPDATED
            };
        }
        if (uriType == NAVIGATION || uriType == NAVIGATION_ITEM) {
            return new String[]{
                    BaseColumns._ID,
                    FeedContract.Navigation.NAME,
                    FeedContract.Navigation.TITLE,
                    FeedContract.Navigation.LINK,
                    FeedContract.Navigation.POSITION
            };
        }
        String[] available = {
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
        if (uriType == MY_VIDEO || uriType == MY_VIDEO_FEEDITEM)
            columnList.add(FeedContract.MyVideo.SIGNATURE);
        if (uriType == SEARCH_RESULTS || uriType == SEARCH_RESULTS_FEEDITEM) {
            columnList.add(FeedContract.SearchResults.QUERY_ID);
            columnList.add(FeedContract.SearchResults.POSITION);
        }
        if (uriType == RELATED_VIDEO || uriType == RELATED_VIDEO_ITEM) {
            columnList.add(FeedContract.RelatedVideo.RELATED_VIDEO_ID);
            columnList.add(FeedContract.RelatedVideo.POSITION);
            columnList.add(FeedContract.RelatedVideo.HITS);
        }

        if (uriType == SUBSCRIPTION || uriType == SUBSCRIPTION_FEEDITEM ||
                uriType == TAGS_VIDEO || uriType == TAGS_VIDEO_FEEDITEM) {
            columnList.add(FeedContract.Subscriptions.TAGS_JSON);
        }
        if (uriType == TAGS_VIDEO || uriType == TAGS_VIDEO_FEEDITEM) {
            columnList.add(FeedContract.TagsVideo.TAG_ID);
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

    private static final String NAVIGATION_FIXTURE_QUERY = "INSERT INTO " +
            FeedContract.Navigation.CONTENT_PATH + " (name, title, link, position) ";

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

    private static final String SQL_CREATE_AUTHOR_QUERY = "CREATE TABLE " +
            FeedContract.AuthorVideo.CONTENT_PATH + " (" +
            FEED_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_TAGS_VIDEO_QUERY = "CREATE TABLE " +
            FeedContract.TagsVideo.CONTENT_PATH + " (" +
            TAGS_VIDEO_COLUMNS_SQL + ")";

    private static final String SQL_CREATE_RELATED_VIDEO = "CREATE TABLE " +
            FeedContract.RelatedVideo.CONTENT_PATH + " (" +
            RELATED_VIDEO_COLUMNS_SQL + ")";

    private static final String SQL_DROP_TABLE = "DROP TABLE %s";

    private static final int DB_VERSION = 8;

    private static final String getNaviFixture() {
        Context context = RutubeApp.getContext();
        String name = context.getString(R.string.navi_main_name);
        String title = context.getString(R.string.navi_main_title);
        String link = context.getString(R.string.navi_main_link);
        return String.format("VALUES('%s', '%s', '%s', 0)", name, title, link);
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

                    default:
                        break;
                }
            }
        }
    }

}
