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
    private Uri balancerUrl;

    public TrackInfo(Uri balancerUrl) {
        this.balancerUrl = balancerUrl;
    }

    public static TrackInfo fromParcel(Parcel parcel) {
        Uri balancerUrl = parcel.readParcelable(Uri.class.getClassLoader());
        return new TrackInfo(balancerUrl);
    }

    public static TrackInfo fromJSON(JSONObject data) throws JSONException {
        JSONObject balancer = data.getJSONObject(VIDEO_BALANCER);
        return new TrackInfo(Uri.parse(balancer.getString(STREAM_TYPE_M3U8)));
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
        parcel.writeParcelable(balancerUrl, i);
    }

    public Uri getBalancerUrl() {
        return balancerUrl;
    }
}
