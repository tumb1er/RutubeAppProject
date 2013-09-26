package ru.rutube.RutubeFeed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.helpers.BitmapProcessor;
import ru.rutube.RutubeFeed.helpers.TopRoundCornerBitmapProcessor;

/**
 * Created by tumbler on 07.07.13.
 */
public class ThumbnailView extends NetworkImageView {
    private static double ROUND_PERCENT = RutubeApp.getContext().getResources().getFraction(R.dimen.round_corners, 1, 1);
    private static double ASPECT = 0.668;
    private static final BitmapProcessor sBitmapProcessor = new TopRoundCornerBitmapProcessor(ROUND_PERCENT, ASPECT);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(parentWidth, (int)(parentWidth * ASPECT));
    }
}
