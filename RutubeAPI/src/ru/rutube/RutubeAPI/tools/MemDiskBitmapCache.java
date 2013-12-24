package ru.rutube.RutubeAPI.tools;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.toolbox.ImageLoader;

import java.io.File;

import ru.rutube.RutubeAPI.RutubeApp;

/**
 * Created by tumbler on 06.11.13.
 */
public class MemDiskBitmapCache implements ImageLoader.ImageCache {
    private BitmapLruCache mMemoryCache;
    private DiskLruImageCache mDiskCache;

    public MemDiskBitmapCache(String cacheDir) {
        mDiskCache = new DiskLruImageCache(RutubeApp.getContext(), cacheDir, 20*1024*1024,
                Bitmap.CompressFormat.PNG, 100);
        mMemoryCache = new BitmapLruCache();
    }

    @Override
    public Bitmap getBitmap(String url) {
        String key = getKey(url);
        Bitmap result = mMemoryCache.getBitmap(key);
        if (result != null)
            return result;
        result = mDiskCache.getBitmap(key);
        return result;
    }

    protected String getKey(String url) {
        int keyStart = url.lastIndexOf('/');
        int keyEnd = url.lastIndexOf('.');
        return url.substring(keyStart + 1, keyEnd);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        String key = getKey(url);
        mMemoryCache.putBitmap(key, bitmap);
        mDiskCache.putBitmap(key, bitmap);
    }

}
