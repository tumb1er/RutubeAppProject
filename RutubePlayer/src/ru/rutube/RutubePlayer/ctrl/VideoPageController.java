package ru.rutube.RutubePlayer.ctrl;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 12.11.13.
 */
public class VideoPageController implements Parcelable, RequestListener {
    private static final String LOG_TAG = VideoPageController.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    protected RequestQueue mRequestQueue;

    private boolean mAttached;
    private VideoPageView mView;
    private Context mContext;

    private Uri mVideoUri;
    private Video mVideo;

    @Override
    public void onResult(int tag, Bundle result) {
        if (tag == Requests.VIDEO) {
            mVideo = result.getParcelable(Constants.Result.VIDEO);
            setVideoInfo();
        }
    }

    private void setVideoInfo() {
        if (mView != null)
            mView.setVideoInfo(mVideo);
    }

    @Override
    public void onVolleyError(VolleyError error) {

    }

    @Override
    public void onRequestError(int tag, RequestError error) {

    }


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

        public void closeVideoPage();

        public void toggleFullscreen(boolean isFullscreen);

        public void setVideoInfo(Video mVideo);

        boolean isFullscreen();
    }

    public VideoPageController(Uri videoUri) {
        this(videoUri, null);
    }

    public VideoPageController(Uri videoUri, Video video){
        mVideoUri = videoUri;
        mVideo = video;
    }

    public void attach(Context context, VideoPageView view) {
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mAttached = true;
        if (mVideo != null) {
            mView.setVideoInfo(mVideo);
        }
        startRequests();
    }

    private void startRequests() {
        String videoId = mVideoUri.getLastPathSegment();
        mVideo = new Video(videoId);
        mRequestQueue.add(mVideo.getVideoRequest(mContext, this));
    }

    public void detach() {
        mAttached = false;
        mContext = null;
        mView = null;
    }

    public void onBackPressed() {
        if (mView == null) {
            // двойное нажатие на "назад" приходит после того, как контроллер уже детачнулся
            return;
        }
        if (mView.isFullscreen()) {
            mView.toggleFullscreen(false);
        } else {
            mView.closeVideoPage();
        }
    }


    public void onDoubleTap() {
        if (D) Log.d(LOG_TAG, "onDoubleTap");
        mView.toggleFullscreen(!mView.isFullscreen());
    }


    // Реализация Parcelable

    public static VideoPageController fromParcel(Parcel in) {
        Uri videoUri = in.readParcelable(Uri.class.getClassLoader());
        Video video = in.readParcelable(Video.class.getClassLoader());
        return new VideoPageController(videoUri, video);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(mVideoUri, flags);
        parcel.writeParcelable(mVideo, flags);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<VideoPageController> CREATOR
            = new Parcelable.Creator<VideoPageController>() {
        public VideoPageController createFromParcel(Parcel in) {
            return VideoPageController.fromParcel(in);
        }

        public VideoPageController[] newArray(int size) {
            return new VideoPageController[size];
        }
    };

}
