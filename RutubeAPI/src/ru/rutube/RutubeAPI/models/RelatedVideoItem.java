package ru.rutube.RutubeAPI.models;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.content.FeedContract;

/**
 * Created by tumbler on 29.07.13.
 */
public class RelatedVideoItem extends FeedItem {
    private static final String JSON_HITS = "hits";
    private String mVideoId;
    private int mHits;
    public RelatedVideoItem(FeedItem item, int hits) {
        super(item.getTitle(), item.getDescription(), item.getCreated(), item.getThumbnailUri(),
              item.getVideoId(), item.getAuthor(), item.getDuration());
        mHits = hits;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String videoId) {
        this.mVideoId = videoId;
    }

    public static RelatedVideoItem fromJSON(JSONObject data) throws JSONException {
        FeedItem item = FeedItem.fromJSON(data);
        int hits = parseHits(data);
        return new RelatedVideoItem(item, hits);
    }

    private static int parseHits(JSONObject data) {
        return data.optInt(JSON_HITS, 0);
    }

    @Override
    public void fillRow(ContentValues row, int position) {
        super.fillRow(row);
        row.put(FeedContract.RelatedVideo.RELATED_VIDEO_ID, mVideoId);
        row.put(FeedContract.RelatedVideo.POSITION, position);
        row.put(FeedContract.RelatedVideo.HITS, mHits);
    }


}
