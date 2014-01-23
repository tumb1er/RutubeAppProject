package ru.rutube.RutubeApp.views;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by tumbler on 23.01.14.
 */
public class LinkTextView extends TextView {
    public static interface OnLinkClickListener {
        public void onLinkClick(String url, String title);
    }

    private OnLinkClickListener mOnClickListener;

    public void setOnLinkClickListener(OnLinkClickListener listener) {
        mOnClickListener = listener;
    }

    public LinkTextView(Context context) {
        super(context);
    }

    public LinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinkTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private class URLSpanNoUnderline extends ClickableSpan {

        protected String mTitle;
        public URLSpanNoUnderline(String title) {
            mTitle = title;
        }

        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void setText(Spanned html) {
        Log.d(getClass().getName(), "setText intercepted!");
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(html);
        URLSpan[] urls = strBuilder.getSpans(0, html.length(), URLSpan.class);
        if (urls != null)
            for(URLSpan span : urls) {
                makeLinkClickable(strBuilder, span);
            }
        super.setText(strBuilder);
    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        String title = strBuilder.subSequence(start, end).toString();
        Log.d(getClass().getName(), "makeLinkClickable " + String.valueOf(span.getURL())
        + " " + title);
        ClickableSpan clickable = new URLSpanNoUnderline(title) {
            public void onClick(View view) {
                Log.d(LinkTextView.this.getClass().getName(), "AAA");
                if (mOnClickListener != null)
                    mOnClickListener.onLinkClick(span.getURL(), mTitle);
            }
        };
        strBuilder.removeSpan(span);

        strBuilder.setSpan(clickable, start, end, flags);
    }
}
