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
public class Trackinfo implements Parcelable {
    public static final String VIDEO_BALANCER = "video_balancer";
    public static final String STREAM_TYPE_M3U8 = "m3u8";
    private Uri balancerUrl;

    public Trackinfo(Uri balancerUrl) {
        this.balancerUrl = balancerUrl;
    }

    public static Trackinfo fromParcel(Parcel parcel) {
        Uri balancerUrl = parcel.readParcelable(Uri.class.getClassLoader());
        return new Trackinfo(balancerUrl);
    }

    public static Trackinfo fromJSON(JSONObject data) throws JSONException {
        JSONObject balancer = data.getJSONObject(VIDEO_BALANCER);
        return new Trackinfo(Uri.parse(balancer.getString(STREAM_TYPE_M3U8)));
    }

    // Parcelable implementation

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<Trackinfo> CREATOR
            = new Parcelable.Creator<Trackinfo>() {
        public Trackinfo createFromParcel(Parcel in) {
            return Trackinfo.fromParcel(in);
        }

        public Trackinfo[] newArray(int size) {
            return new Trackinfo[size];
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
