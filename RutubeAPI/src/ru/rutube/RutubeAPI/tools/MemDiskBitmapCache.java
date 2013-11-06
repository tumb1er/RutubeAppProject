package ru.rutube.RutubeAPI.tools;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;

import java.io.File;

/**
 * Created by tumbler on 06.11.13.
 */
public class MemDiskBitmapCache implements ImageLoader.ImageCache {
    private BitmapLruCache mMemoryCache;
    private DiskBitmapCache mDiskCache;

    public MemDiskBitmapCache(File cacheDir) {
        mDiskCache = new DiskBitmapCache(cacheDir, 5*1024*1024);
        mMemoryCache = new BitmapLruCache();
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap result = mMemoryCache.getBitmap(url);
        if (result != null)
            return result;
        result = mDiskCache.getBitmap(url);
        return result;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mMemoryCache.putBitmap(url, bitmap);
        mDiskCache.putBitmap(url, bitmap);
    }
}
