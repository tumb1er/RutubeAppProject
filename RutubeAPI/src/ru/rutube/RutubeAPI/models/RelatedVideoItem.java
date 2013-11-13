package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.content.FeedContract;

/**
 * Created by tumbler on 29.07.13.
 */
public class RelatedVideoItem extends FeedItem {
    private String mVideoId;
    public RelatedVideoItem(FeedItem item) {
        super(item.getTitle(), item.getDescription(), item.getCreated(), item.getThumbnailUri(),
                item.getVideoId(), item.getAuthor());
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String videoId) {
        this.mVideoId = videoId;
    }

    public static RelatedVideoItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        return new RelatedVideoItem(item);
    }

    @Override
    public void fillRow(ContentValues row, int position) {
        super.fillRow(row);
        row.put(FeedContract.RelatedVideo.RELATED_VIDEO_ID, mVideoId);
        row.put(FeedContract.RelatedVideo.POSITION, position);
    }
}
