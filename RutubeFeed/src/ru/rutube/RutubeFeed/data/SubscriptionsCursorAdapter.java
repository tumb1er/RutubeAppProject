package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.TagsFeedItem;
import ru.rutube.RutubeAPI.models.VideoTag;
import ru.rutube.RutubeFeed.R;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 11.05.13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionsCursorAdapter extends FeedCursorAdapter {
    private static final String LOG_TAG = SubscriptionsCursorAdapter.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    private int mTagColor;


    protected static class ViewHolder extends FeedCursorAdapter.ViewHolder {
        public LinearLayout tags;
        public View balloon;
        public int firstTagId;
    }

    public SubscriptionsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mTagColor = context.getResources().getColor(R.color.tag_title);
    }

    @Override
    protected FeedCursorAdapter.ViewHolder initHolder(View view) {
        ViewHolder holder = new ViewHolder();
        super.initHolder(view, holder);
        holder.tags = (LinearLayout)view.findViewById(R.id.tagsListContainer);
        holder.balloon = view.findViewById(R.id.balloon);
        return holder;
    }

    @Override
    protected void bindDescription(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {

    }

    @Override
    public void bindView(@NotNull View view, Context context, @NotNull Cursor cursor) {
        super.bindView(view, context, cursor);
        try {
            ViewHolder holder = (ViewHolder)getViewHolder(view);
            // Сохраняем ID первого тега, который окажется на месте имени автора.
            TagsFeedItem item = TagsFeedItem.fromCursor(cursor);
            List<VideoTag> tags = item.getTags();
            if (tags.size() > 0)
                holder.firstTagId = tags.get(0).getId();
            else
                holder.firstTagId = 0;
            bindTags(holder, cursor);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void bindAuthor(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
        holder.author.setTextColor(mTagColor);
        holder.authorId = 0;
    }

    @Override
    protected void bindAvatar(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        ViewHolder holder = (ViewHolder) initHolder(view);
        holder.author.setTypeface(mNormalFont);
        holder.footer.setVisibility(View.VISIBLE);
        holder.avatar.setVisibility(View.GONE);
        holder.tags.setVisibility(View.VISIBLE);
        return view;
    }

    private ViewHolder bindTags(ViewHolder holder, Cursor cursor) throws JSONException {
        int tagsListIndex = cursor.getColumnIndexOrThrow(FeedContract.Subscriptions.TAGS_JSON);
        String tagsJson = cursor.getString(tagsListIndex);
        if (tagsJson == null) {
            tagsJson = "[]";
        }
        JSONArray tags = new JSONArray(tagsJson);
        VideoTag[] tagValues = new VideoTag[tags.length()];
        for(int i=0;i<tags.length();i++) {
            VideoTag tag = VideoTag.fromJSON(tags.getJSONObject(i));
            tagValues[i] = tag;
        }
        TagsListAdapter tagsListAdapter = new TagsListAdapter(mContext, R.layout.tag_item, tagValues);
        holder.tags.removeAllViews();
        holder.author.setText(null);
        holder.description.setText(null);
        boolean description_set = false;
        for (int i=0;i<tagValues.length;i++) {
            View tagView = tagsListAdapter.getView(i, null, holder.tags);
            assert tagView != null;
            TagsListAdapter.ViewHolder tag_holder = (TagsListAdapter.ViewHolder)tagView.getTag();

            // Первый непустой комментарий переносится из блока тега в описание видео
            if (!description_set && tag_holder.comment.getVisibility() == View.VISIBLE)
            {
                tag_holder.comment.setVisibility(View.GONE);
                holder.description.setText(tag_holder.comment.getText());
                description_set = true;
            }

            // Первый тег переносится на место имени автора
            if (i == 0) {
                holder.author.setText(tag_holder.title.getText());
            } else {
                holder.tags.addView(tagView);
            }
        }
        holder.description.setVisibility(description_set ? View.VISIBLE : View.GONE);
        holder.balloon.setVisibility(tagValues.length > 0 ? View.VISIBLE : View.GONE);
        return holder;
    }

    @Override
    protected void setTags(int position, View view) {
        super.setTags(position, view);
        ViewHolder holder = (ViewHolder)getViewHolder(view);
        ClickTag feedTag;
        for (int i=0; i<holder.tags.getChildCount(); i++) {
            View tagView = holder.tags.getChildAt(i);
            assert tagView != null;
            TagsListAdapter.ViewHolder tagHolder = (TagsListAdapter.ViewHolder)tagView.getTag();
            String tagTitle = String.valueOf(tagHolder.title.getText());
            feedTag = getClickTag(position, tagHolder.tagId, tagTitle);
            tagHolder.card.setTag(feedTag);
            tagHolder.comment.setTag(feedTag);
            tagHolder.title.setTag(feedTag);

            tagHolder.card.setOnClickListener(mOnClickListener);
            tagHolder.comment.setOnClickListener(mOnClickListener);
            tagHolder.title.setOnClickListener(mOnClickListener);
        }
        String authorName = String.valueOf(holder.author.getText());
        feedTag = getClickTag(position, holder.firstTagId, authorName);
        holder.footer.setTag(feedTag);
        holder.author.setTag(feedTag);
        holder.created.setTag(feedTag);
    }

    private ClickTag getClickTag(int position, int tag_id, String title) {
        Uri feedUri;
        ClickTag feedTag;
        if (tag_id > 0){
            feedUri = RutubeApp.getFeedUri(R.string.video_by_tag_uri, tag_id);
            feedTag = new ClickTag(position, feedUri, title);
        } else {
            feedTag = new ClickTag(position, null, null);
        }
        return feedTag;
    }
}
