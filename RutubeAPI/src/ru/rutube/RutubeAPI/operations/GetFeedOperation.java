package ru.rutube.RutubeAPI.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Auth;
import ru.rutube.RutubeAPI.models.Author;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.FeedItem;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 04.05.13
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
public class GetFeedOperation implements RequestService.Operation {
    private static final String LOG_TAG = GetFeedOperation.class.getName();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
        Uri contentUri = (Uri)request.getParcelable(Constants.Params.CONTENT_URI);
        Uri feedUri = (Uri)request.getParcelable(Constants.Params.FEED_URI);
        int page = request.getInt(Constants.Params.PAGE);
        Log.d(LOG_TAG, "Fetching " + feedUri.toString() + "?page=" + String.valueOf(page));
        Log.d(LOG_TAG, " and store to " + contentUri.toString());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("page", String.valueOf(page));
        NetworkConnection connection = new NetworkConnection(context, feedUri.toString());
        connection.setParameters(params);
        Auth.from(context).setToken(connection);
        NetworkConnection.ConnectionResult result = connection.execute();
        ContentValues[] feedItems;
        Bundle bundle = new Bundle();
        String firstId = null;
        try {
            JSONObject body = new JSONObject(result.body);
            JSONArray data = body.getJSONArray("results");
            int perPage = body.getInt("per_page");
            bundle.putInt(Constants.Result.PER_PAGE, perPage);
            feedItems = new ContentValues[data.length()];

            for (int i = 0; i < data.length(); ++i) {
                ContentValues row = new ContentValues();
                JSONObject data_item = data.getJSONObject(i);
                FeedItem item = FeedItem.fromJSON(data_item);
                if (i == 0)
                    firstId = item.getVideoId();
                row.put(FeedContract.FeedColumns._ID, item.getVideoId());
                row.put(FeedContract.FeedColumns.TITLE, item.getTitle());
                row.put(FeedContract.FeedColumns.DESCRIPTION, item.getDescription());
                Log.d(LOG_TAG, "Date: " + sdf.format(item.getCreated()));
                row.put(FeedContract.FeedColumns.CREATED, sdf.format(item.getCreated()));
                row.put(FeedContract.FeedColumns.THUMBNAIL_URI, item.getThumbnailUri().toString());
                Author author = item.getAuthor();
                if (author != null) {
                    row.put(FeedContract.FeedColumns.AUTHOR_ID, author.getId());
                    row.put(FeedContract.FeedColumns.AUTHOR_NAME, author.getName());
                    row.put(FeedContract.FeedColumns.AVATAR_URI, author.getAvatarUrl().toString());
                }
                feedItems[i] = row;
            }
        } catch (JSONException e) {
            throw new DataException(e.getMessage());
        }
        Log.d(LOG_TAG, "Inserting items: " + String.valueOf(feedItems));
        try {
            context.getContentResolver().bulkInsert(contentUri, feedItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (page == 1)
            checkInvalidation(context, contentUri, firstId);
        else
            context.getContentResolver().notifyChange(contentUri, null);
        Log.d(LOG_TAG, "Operation finished");
        return bundle;
    }

    private void checkInvalidation(Context context, Uri contentUri, String firstId) {
        Log.d(LOG_TAG, "Checking invalidation");
        Cursor c = context.getContentResolver().query(contentUri, null, null,null,
                FeedContract.FeedColumns.CREATED + " DESC LIMIT 1");
        if (c.getCount() > 0){
            c.moveToFirst();
            int idIndex = c.getColumnIndex(FeedContract.FeedColumns._ID);
            String dbId = c.getString(idIndex);
            if (!dbId.equals(firstId)) {
                Log.d(LOG_TAG, "NotifyChange");
                context.getContentResolver().notifyChange(contentUri, null);
            }
        }
        c.close();
    }
}
