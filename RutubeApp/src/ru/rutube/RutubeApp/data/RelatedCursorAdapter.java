package ru.rutube.RutubeApp.data;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    protected void processPosition(int position) {
        super.processPosition(position);
    }

    @Override
    protected ViewHolder getHolder(View view) {
        ViewHolder holder = new ViewHolder();
        initHolder(view, holder);
        holder.hits = (TextView) view.findViewById(R.id.hitsTextView);
        return holder;
    }

    @Override
    protected void initView(FeedCursorAdapter.ViewHolder holder) {
        holder.title.setTypeface(mNormalFont);
        holder.author.setTypeface(mLightFont);
        initOnClickListeners(holder);
    }

    @Override
    protected void bindCreated(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
    }

    @Override
    protected void bindAvatar(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
    }

    @Override
    protected void bindDescription(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        decorateItem(view, position == 0);
        return view;
    }

    private void decorateItem(View view, boolean isFirst) {
        View cardView = view.findViewById(R.id.card);
        int pl = cardView.getPaddingLeft();
        int pt = cardView.getPaddingTop();
        int pr = cardView.getPaddingRight();
        int pb = cardView.getPaddingBottom();
        if (isFirst)
            cardView.setBackgroundResource(R.drawable.related_first_card_bg);
        else
            cardView.setBackgroundResource(R.color.card_background);

        int topPadding = (int) view.getContext().getResources().getDimension(
                R.dimen.related_card_padding_top);
        // при установке фона padding сбрасывается
        cardView.setPadding(pl, pt, pr, pb);
        view.setPadding(
                view.getPaddingLeft(),
                isFirst ? topPadding: 0,
                view.getPaddingRight(),
                view.getPaddingBottom());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        try {
            bindHits(cursor, (ViewHolder) view.getTag());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    protected void bindHits(Cursor cursor, ViewHolder holder) {
        // No hits
    }

    @Override
    protected void setTags(int position, View view) {
        ClickTag tag = new ClickTag();
        tag.position = position;
        ViewHolder holder = (ViewHolder)view.getTag();
        holder.title.setTag(tag);
        holder.thumbnail.setTag(tag);
        holder.hits.setTag(tag);

    }

    @Override
    protected void initOnClickListeners(FeedCursorAdapter.ViewHolder holder) {
        ViewHolder h = (ViewHolder)holder;
        h.title.setOnClickListener(mOnClickListener);
        h.thumbnail.setOnClickListener(mOnClickListener);
        h.hits.setOnClickListener(mOnClickListener);
    }
}
