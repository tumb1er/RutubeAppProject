package ru.rutube.RutubeAPI.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:07
 * To change this template use File | Settings | File Templates.
 */
public class TrackInfo implements Parcelable {
    public static final String VIDEO_BALANCER = "video_balancer";
    public static final String STREAM_TYPE_M3U8 = "m3u8";
    private static final String JSON_TRACK_ID = "track_id";
    private Uri mBalancerUrl;
    private int mTrackId;
    private String mTitle;

    public TrackInfo(Uri balancerUrl, int trackId, String title) {
        this.mBalancerUrl = balancerUrl;
        this.mTrackId = trackId;
        mTitle = title;
    }

    public static TrackInfo fromParcel(Parcel parcel) {
        Uri balancerUrl = parcel.readParcelable(Uri.class.getClassLoader());
        int trackId = parcel.readInt();
        String title = parcel.readString();
        return new TrackInfo(balancerUrl, trackId, title);
    }

    public static TrackInfo fromJSON(JSONObject data) throws JSONException {
        JSONObject balancer = data.getJSONObject(VIDEO_BALANCER);
        int trackId = data.getInt(JSON_TRACK_ID);
        String title = data.getString("title");
        return new TrackInfo(Uri.parse(balancer.getString(STREAM_TYPE_M3U8)), trackId, title);
    }

    // Parcelable implementation

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<TrackInfo> CREATOR
            = new Parcelable.Creator<TrackInfo>() {
        public TrackInfo createFromParcel(Parcel in) {
            return TrackInfo.fromParcel(in);
        }

        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mBalancerUrl, i);
        parcel.writeInt(mTrackId);
    }

    public String getTitle() {
        return mTitle;
    }

    public Uri getBalancerUrl() {
        return mBalancerUrl;
    }

    public int getTrackId(){
        return mTrackId;
    }
}
