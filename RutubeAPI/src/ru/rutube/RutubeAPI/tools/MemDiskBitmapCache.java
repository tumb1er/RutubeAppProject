package ru.rutube.RutubeAPI.tools;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;

import java.lang.ref.WeakReference;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;

/**
 * Created by tumbler on 06.11.13.
 */
public class MemDiskBitmapCache implements ImageLoader.ImageCache {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = MemDiskBitmapCache.class.getName();
    private static final boolean MEMORY_ENABLED = true;
    private BitmapLruCache mMemoryCache;
    private DiskLruImageCache mDiskCache;

    public MemDiskBitmapCache(String cacheDir) {
        mDiskCache = new DiskBitmapCache(RutubeApp.getContext(), cacheDir, 20*1024*1024,
                Bitmap.CompressFormat.PNG, 100);
        if (MEMORY_ENABLED)
            mMemoryCache = new BitmapLruCache((int)(Runtime.getRuntime().maxMemory() / 1024) / 16);
    }

    @Override
    public Bitmap getBitmap(String url) {
        String key = getKey(url);
        Bitmap result;
        if (D) Log.d(LOG_TAG, "Start getting bitmap " + key);
        if (MEMORY_ENABLED)
        {
            result = mMemoryCache.getBitmap(key);
            if (result != null)
                return result;
        }
        result = mDiskCache.getBitmap(key);
        if (result != null && MEMORY_ENABLED)
            mMemoryCache.putBitmap(key, result);
        if (D) Log.d(LOG_TAG, "End getting bitmap " + key);
        return result;
    }

    protected String getKey(String url) {
        if (D) Log.d(LOG_TAG, "Bitmap Key: " + url);
        int keyStart = url.lastIndexOf('/');
        int keyEnd = url.lastIndexOf('.');
        if (keyEnd < keyStart) {
            // ссылка на аватар без расширения, например, http://pic.rutube.ru/
            return "notanimage";
        }
        int sizePos = url.lastIndexOf("size");
        if (sizePos < 0)
            return url.substring(keyStart + 1, keyEnd) + "n";
        else
            return url.substring(keyStart + 1, keyEnd) + url.substring(sizePos + 5, sizePos + 6);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        String key = getKey(url);
        if (D) Log.d(LOG_TAG, "start saving bitmap " + key);
        if (MEMORY_ENABLED)
            mMemoryCache.putBitmap(key, bitmap);
        SaveToDiskCacheTask task = new SaveToDiskCacheTask(bitmap);
        task.execute(key);
        if (D) Log.d(LOG_TAG, "End saving bitmap " + key);
    }

    protected class SaveToDiskCacheTask extends AsyncTask<String, Void, Void> {
        private final WeakReference<Bitmap> bitmapReference;

        public SaveToDiskCacheTask(Bitmap bitmap) {
            this.bitmapReference = new WeakReference<Bitmap>(bitmap);
        }

        @Override
        protected Void doInBackground(String... keys) {
            String key = keys[0];
            try {
                mDiskCache.putBitmap(key, bitmapReference.get());
            } catch (NullPointerException e) {}
            return null;

        }
    }
}
