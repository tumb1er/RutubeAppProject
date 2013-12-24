package ru.rutube.RutubeFeed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.helpers.BitmapProcessor;
import ru.rutube.RutubeFeed.helpers.CropBitmapProcessor;
import ru.rutube.RutubeFeed.helpers.TopRoundCornerBitmapProcessor;

/**
 * Created by tumbler on 07.07.13.
 */
public class ThumbnailView extends NetworkImageView {
    private static double ASPECT = 0.668;
    private String mImageUrl = null;
    private static final BitmapProcessor sBitmapProcessor = new CropBitmapProcessor(ASPECT);
    public ThumbnailView(Context context) {
        super(context);
    }

    public ThumbnailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    @Override
//    public void setImageBitmap(Bitmap bm) {
//        super.setImageBitmap(sBitmapProcessor.process(bm, this));
//    }


    @Override
    public void setImageUrl(String url, ImageLoader imageLoader) {
        if (mImageUrl != null && mImageUrl.equals(url))
            return;
        super.setImageUrl(url, imageLoader);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(parentWidth, (int)(parentWidth * ASPECT));
    }
}
