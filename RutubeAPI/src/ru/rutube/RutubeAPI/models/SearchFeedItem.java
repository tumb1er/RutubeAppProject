package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.rutube.RutubeAPI.content.FeedContract;

/**
 * Created by tumbler on 29.07.13.
 */
public class SearchFeedItem extends FeedItem {
    private int mQueryId;
    public SearchFeedItem(FeedItem item) {
        super(item.getTitle(), item.getDescription(), item.getCreated(), item.getThumbnailUri(),
                item.getVideoId(), item.getAuthor(), item.getDuration());
    }

    public int getQueryId() {
        return mQueryId;
    }

    public void setQueryId(int queryId) {
        this.mQueryId = queryId;
    }

    public static SearchFeedItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        return new SearchFeedItem(item);
    }

    @Override
    public void fillRow(ContentValues row, int position) {
        super.fillRow(row);
        row.put(FeedContract.SearchResults.QUERY_ID, mQueryId);
        row.put(FeedContract.SearchResults.POSITION, position);
    }
}
