package ru.rutube.RutubeApp.ctrl;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.models.NaviItem;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 11.03.14.
 */
public class SplashScreenController implements Parcelable, RequestListener {

    private Context mContext;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private SplashScreenView mView;

    private User mUser;
    private int mRequestsDone;
    private static final int TOTAL_REQUESTS = 2;
    private boolean mAttached;

    public interface SplashScreenView {
        public void setBannerUrl(String url);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    @Override
    public void onResult(int tag, Bundle result) {

    }

    @Override
    public void onVolleyError(VolleyError error) {

    }

    @Override
    public void onRequestError(int tag, RequestError error) {

    }

    public void attach(Context context, SplashScreenView view) {
        assert mContext == null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mImageLoader = new ImageLoader(mRequestQueue, RutubeApp.getBitmapCache());
        mUser = User.load();
        mAttached = true;
        startRequests();
    }

    private void startRequests() {
        initBanner();
        JsonObjectRequest naviLinksRequest = NaviItem.getNaviLinksRequest(null);
        mRequestQueue.add(naviLinksRequest);
    }

    private void initBanner() {
        String url = "http://pic.rutube.ru/genericimage/3c/eb/3ceb636aa40bf7d59da0adcf2f6f9c33.png";

        mView.setBannerUrl(url);
    }

    public void detach() {
        mContext = null;
        mRequestQueue.cancelAll(Requests.VISITOR);
        mRequestQueue.cancelAll(Requests.MENU_LINKS);
        mRequestQueue.stop();
        mAttached = false;
    }


    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
