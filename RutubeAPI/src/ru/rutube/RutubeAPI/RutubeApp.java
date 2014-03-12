package ru.rutube.RutubeAPI;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.tools.MemDiskBitmapCache;

/**
 * Created by tumbler on 22.06.13.
 */
public class RutubeApp extends Application {

    private static ImageLoader.ImageCache sBitmapCache;
    protected static final SimpleDateFormat reprDateFormat = new SimpleDateFormat("d MMMM y");

    private static RutubeApp instance;

    private RequestQueue requestQueue;
    private volatile static boolean mLoadingFeed;

    public RutubeApp() {
        instance = this;
        mLoadingFeed = false;
    }

    public static RutubeApp getInstance() {
        if (instance == null)
            instance = new RutubeApp();
        return instance;
    }

    public static Context getContext() {
        if (instance != null) {
            return instance.getApplicationContext();
        }
        return null;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * вызывается при действиях,
     * - повороты экрана
     * - открытие/закрытие клавиатуры
     * - изменение настроек приложения и тд
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * вызывается при очистке памяти (кэша, ресурсов объектов в памяти и тд)
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * вызывается при преждевременном завершении работы приложения
     * (именно приложения, а не коммандой "ядра")
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static ImageLoader.ImageCache getBitmapCache() {
        if (sBitmapCache != null)
            return sBitmapCache;
        Context context = instance.getApplicationContext();
        assert context != null;
        sBitmapCache = new MemDiskBitmapCache("thumbnails");
        return sBitmapCache;
    }

    public static String getUrl(int stringId) {
        Uri resultUri = getUri(getContext().getString(stringId));
        assert resultUri != null;
        return resultUri.toString();
    }

    public static Uri formatUrl(int stringId, Object param) {
        Uri baseUri = Uri.parse(getContext().getString(R.string.base_uri));
        String path = getContext().getString(stringId).replace("api/", "");
        return baseUri.buildUpon()
                .appendEncodedPath(String.format(path, param))
                .build();
    }

    public static Uri formatApiUrl(int stringId, Object param) {
        Uri baseUri = Uri.parse(getContext().getString(R.string.base_uri));
        String path = getContext().getString(stringId);
        return baseUri.buildUpon()
                .appendEncodedPath(String.format(path, param))
                .build();
    }

    public void openFeed(Uri feedUri, Context context, String title) {
        if (feedUri == null)
            throw new IllegalArgumentException("Can't open feeed");
        Intent intent = new Intent("ru.rutube.feed.open");
        intent.setData(feedUri);
        intent.putExtra(Constants.Params.FEED_TITLE, title);
        context.startActivity(intent);
    }

    public static String getUrl(String path) {
        assert getContext() != null;
        Uri resultUri = getUri(path);
        assert resultUri != null;
        return resultUri.toString();
    }

    public static Uri getUri(String path) {
        Uri baseUri = Uri.parse(getContext().getString(R.string.base_uri));
        return baseUri.buildUpon()
                .appendEncodedPath(path)
                .build();
    }


    public static synchronized boolean isLoadingFeed() {
        return mLoadingFeed;
    }

    public static void startLoading() {
        mLoadingFeed = true;
    }
    public static void stopLoading() {
        mLoadingFeed = false;
    }


    public String getCreatedText(Date created) {
        if (created == null)
            return null;
        Date now = new Date();
        long seconds = (now.getTime() - created.getTime()) / 1000;
        if (seconds < 3600)
            return getResources().getString(R.string.now);
        if (seconds < 24 * 3600)
            return getResources().getString(R.string.today);
        if (seconds < 2 * 24 * 3600)
            return getResources().getString(R.string.yesterday);
        if (seconds < 5 * 24 * 3600)
            return getResources().getString(R.string.days_ago_24, seconds / (24 * 3600));
        if (seconds < 7 * 24 * 3600)
            return getResources().getString(R.string.days_ago_59, seconds / (24 * 3600));
        if (seconds < 14 * 24 * 3600)
            return getResources().getString(R.string.week_ago);
        if (seconds < 31 * 24 * 3600)
            return getResources().getString(R.string.weeks_ago, seconds / (7 * 24 * 3600));
        return reprDateFormat.format(created);
    }

    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
