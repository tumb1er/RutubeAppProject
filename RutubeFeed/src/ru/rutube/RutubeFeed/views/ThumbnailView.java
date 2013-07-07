package ru.rutube.RutubeFeed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeFeed.helpers.BitmapProcessor;
import ru.rutube.RutubeFeed.helpers.TopRoundCornerBitmapProcessor;

/**
 * Created by tumbler on 07.07.13.
 */
public class ThumbnailView extends NetworkImageView {
    // TODO: сделать возможность задачать round_pixels через XML в процентах.
    private static final BitmapProcessor sBitmapProcessor = new TopRoundCornerBitmapProcessor(10);
    public ThumbnailView(Context context) {
        super(context);
    }

    public ThumbnailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {

        super.setImageBitmap(sBitmapProcessor.process(bm, this));
    }
}
