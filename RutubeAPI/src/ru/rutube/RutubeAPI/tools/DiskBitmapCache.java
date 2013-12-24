package ru.rutube.RutubeAPI.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;
import java.nio.ByteBuffer;

public class DiskBitmapCache extends DiskLruImageCache {
    public DiskBitmapCache(Context context, String uniqueName, int diskCacheSize,
                           Bitmap.CompressFormat compressFormat, int quality) {
        super(context, uniqueName, diskCacheSize, compressFormat, quality);
    }

    @Override
    public Bitmap getBitmap(String key) {
        return super.getBitmap(key);
    }
}