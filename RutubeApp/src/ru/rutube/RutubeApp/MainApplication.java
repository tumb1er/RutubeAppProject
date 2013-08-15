package ru.rutube.RutubeApp;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.RequestQueue;

/**
 * Created by oleg on 7/30/13.
 */
public class MainApplication extends Application {

    private static MainApplication instance;

    private RequestQueue requestQueue;

    public MainApplication() {
        instance = this;
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
}
