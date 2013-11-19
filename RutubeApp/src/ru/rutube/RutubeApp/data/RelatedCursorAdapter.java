package ru.rutube.RutubeApp.data;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 11.05.13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class RelatedCursorAdapter extends FeedCursorAdapter {
    protected static int item_layout_id = R.layout.related_feed_item;

    protected static class ViewHolder extends FeedCursorAdapter.ViewHolder {
       public TextView hits;
    }

    public RelatedCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    protected ViewHolder getHolder(View view) {
        ViewHolder holder = new ViewHolder();
        initHolder(view, holder);
        holder.hits = (TextView)view.findViewById(R.id.hits);
        return holder;
    }

    @Override
    protected void initView(FeedCursorAdapter.ViewHolder holder) {
        holder.title.setTypeface(mNormalFont);
        holder.author.setTypeface(mLightFont);
    }

    @Override
    protected void bindCreated(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {}

    @Override
    protected void bindAvatar(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {}

    @Override
    protected void bindDescription(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {}

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        try {
            bindHits(cursor, (ViewHolder)view.getTag());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    protected void bindHits(Cursor cursor, ViewHolder holder) {
        // No hits
    }
}
