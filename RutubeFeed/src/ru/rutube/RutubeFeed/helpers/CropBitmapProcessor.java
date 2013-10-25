package ru.rutube.RutubeFeed.helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import ru.rutube.RutubeAPI.BuildConfig;

/**
 * Created by tumbler on 06.07.13.
 */
public class CropBitmapProcessor implements BitmapProcessor {
    private static final String LOG_TAG = CropBitmapProcessor.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private final double mCropAspect;

    public CropBitmapProcessor(double cropAspect) {
        this.mCropAspect = cropAspect;
    }

    @Override
    public Bitmap process(Bitmap bitmap, ImageView imageView) {
        if (bitmap == null)
            return null;
        return cropAspect(bitmap, mCropAspect);
    }

    private static Bitmap cropAspect(Bitmap bitmap, double cropAspect) {
        if (bitmap == null)
            return null;
        if (bitmap.getWidth() == 0)
            return bitmap;
        double srcAspect = (double)bitmap.getHeight() / (double)bitmap.getWidth();
        int width, height;
        Bitmap cropped;
        if (srcAspect < cropAspect) {
            height = bitmap.getHeight();
            width = (int)(height / cropAspect);
            cropped = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2, 0, width, height);
        } else {
            width = bitmap.getWidth();
            height = (int)(width * cropAspect);
            cropped = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - height) / 2, width, height);
        }
        return cropped;
    }
}
