package ru.rutube.RutubeFeed.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

import ru.rutube.RutubeAPI.BuildConfig;

/**
 * Created by tumbler on 26.10.13.
 */
public class Typefaces {
    private static final String TAG = "Typefaces";
    private static boolean D = BuildConfig.DEBUG;

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Log.d(TAG, "Assets:");
                    for (String n: c.getAssets().list("")) {
                        Log.d(TAG, "Asset: " + String.valueOf(n));
                    }
                    Typeface t = Typeface.createFromAsset(c.getAssets(),
                            assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    if(D) Log.e(TAG, "Could not get typeface '" + assetPath
                            + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}