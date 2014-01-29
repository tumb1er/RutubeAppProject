package ru.rutube.RutubeApp.views;

import android.content.Context;
import android.util.AttributeSet;

import com.etsy.android.grid.StaggeredGridView;

/**
 * Created by tumbler on 29.01.14.
 */
public class RutubeStaggeredGridView extends com.etsy.android.grid.StaggeredGridView {
    public RutubeStaggeredGridView(Context context) {
        super(context);
    }

    public RutubeStaggeredGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RutubeStaggeredGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        try {
            // Ловим NPE в onColumnSync
            super.onSizeChanged(w, h, oldw, oldh);
        } catch (NullPointerException ignored) {
            requestLayout();
        }
    }
}
