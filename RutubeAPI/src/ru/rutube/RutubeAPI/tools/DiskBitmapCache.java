package ru.rutube.RutubeAPI.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;
import java.nio.ByteBuffer;

public class DiskBitmapCache extends DiskBasedCache implements ImageLoader.ImageCache {

    public DiskBitmapCache(File rootDirectory, int maxCacheSizeInBytes) {
        super(rootDirectory, maxCacheSizeInBytes);
    }

    public DiskBitmapCache(File cacheDir) {
        super(cacheDir);
    }

    public Bitmap getBitmap(String url) {

        File file = getFileForKey(url);
        if (file.exists())
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        return null;
    }

    public void putBitmap(String url, Bitmap bitmap) {
        final Entry entry = new Entry();

        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        entry.data = buffer.array();

        put(url, entry);
    }
}