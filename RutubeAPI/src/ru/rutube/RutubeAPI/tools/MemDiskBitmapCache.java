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
    // private BitmapLruCache mMemoryCache;
    private DiskLruImageCache mDiskCache;

    public MemDiskBitmapCache(String cacheDir) {
        mDiskCache = new DiskBitmapCache(RutubeApp.getContext(), cacheDir, 20*1024*1024,
                Bitmap.CompressFormat.PNG, 100);
        // mMemoryCache = new BitmapLruCache((int)(Runtime.getRuntime().maxMemory() / 1024) / 16);
    }

    @Override
    public Bitmap getBitmap(String url) {
        String key = getKey(url);
//        Bitmap result = mMemoryCache.getBitmap(key);
//        if (result != null)
//            return result;
        if (D) Log.d(LOG_TAG, "Start getting bitmap " + key);
        Bitmap result = mDiskCache.getBitmap(key);
        if (D) Log.d(LOG_TAG, "End getting bitmap " + key);
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
        // mMemoryCache.putBitmap(key, bitmap);
        SaveToDiskCacheTask task = new SaveToDiskCacheTask(bitmap);
        task.execute(key);
    }

    protected class SaveToDiskCacheTask extends AsyncTask<String, Void, Void> {
        private final WeakReference<Bitmap> bitmapReference;

        public SaveToDiskCacheTask(Bitmap bitmap) {
            this.bitmapReference = new WeakReference<Bitmap>(bitmap);
        }

        @Override
        protected Void doInBackground(String... keys) {
            String key = keys[0];
            if (D) Log.d(LOG_TAG, "start saving bitmap " + key);
            try {
                mDiskCache.putBitmap(key, bitmapReference.get());
            } catch (NullPointerException e) {}
            if (D) Log.d(LOG_TAG, "End saving bitmap " + key);
            return null;

        }
    }
}
