package ru.rutube.RutubeApp.data;

import android.content.Context;
import android.database.Cursor;

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

    public RelatedCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    protected void initView(ViewHolder holder) {
        holder.title.setTypeface(mNormalFont);
        holder.author.setTypeface(mLightFont);
    }

    @Override
    protected void bindCreated(Cursor cursor, ViewHolder holder) {}

    @Override
    protected void bindAvatar(Cursor cursor, ViewHolder holder) {}

    @Override
    protected void bindDescription(Cursor cursor, ViewHolder holder) {}
}
