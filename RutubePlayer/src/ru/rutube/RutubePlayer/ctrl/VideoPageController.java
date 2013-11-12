package ru.rutube.RutubePlayer.ctrl;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.models.TrackInfo;

/**
 * Created by tumbler on 12.11.13.
 */
public class VideoPageController implements Parcelable {
    private static final String LOG_TAG = VideoPageController.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    private boolean mAttached;
    private VideoPageView mView;
    private Context mContext;

    public interface VideoPageView {
        /**
         * Определяет ориентацию экрана на основе значений высоты и ширины экрана
         * @return Configuration.ORIENTATION
         */
        public int getScreenOrientation();

        /**
         * Задает ориентацию экрана.
         * @param orientation ActivityInfo.ORIENTATION*
         */
        public void setScreenOrientation(int orientation);
    }

    public VideoPageController(){

    }

    public void attach(Context context, VideoPageView view) {
        mContext = context;
        mView = view;
        mAttached = true;
    }

    public void detach() {
        mAttached = false;
        mContext = null;
        mView = null;
    }

    // Реализация Parcelable

    public static VideoPageController fromParcel(Parcel in) {
        return new VideoPageController();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<PlayerController> CREATOR
            = new Parcelable.Creator<PlayerController>() {
        public PlayerController createFromParcel(Parcel in) {
            return PlayerController.fromParcel(in);
        }

        public PlayerController[] newArray(int size) {
            return new PlayerController[size];
        }
    };

}
