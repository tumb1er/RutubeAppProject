package ru.rutube.RutubePlayer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by tumbler on 20.11.13.
 * Контейнер элементов, позволяющий подписываться на событие изменения размера контейнера.
 */
public class VideoFrameLayout extends FrameLayout {

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        this.mOnSizeChangedListener = onSizeChangedListener;
    }

    public interface OnSizeChangedListener {
        public void onSizeChanged(int width, int height);
    }
    private OnSizeChangedListener mOnSizeChangedListener;


    public VideoFrameLayout(Context context) {
        super(context);
    }

    public VideoFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && mOnSizeChangedListener != null)
            mOnSizeChangedListener.onSizeChanged(right-left, bottom-top);
    }
}
