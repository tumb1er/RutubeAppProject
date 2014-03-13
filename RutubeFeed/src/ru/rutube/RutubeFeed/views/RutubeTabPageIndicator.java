package ru.rutube.RutubeFeed.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.viewpagerindicator.TabPageIndicator;

import ru.rutube.RutubeFeed.helpers.Typefaces;

/**
 * Created by tumbler on 13.03.14.
 */
public class RutubeTabPageIndicator extends TabPageIndicator {
    protected Typeface mNormalFont;

    public RutubeTabPageIndicator(Context context) {
        super(context);
    }

    public RutubeTabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNormalFont = Typefaces.get(context, "fonts/opensansregular.ttf");
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (int i=0;i<getChildCount();i++) {
            View v = getChildAt(i);
            if (v instanceof TextView) {
                ((TextView)v).setTypeface(mNormalFont);
            }
        }
    }
}
