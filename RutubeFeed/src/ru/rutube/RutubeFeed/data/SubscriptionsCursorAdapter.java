package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.VideoTag;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.helpers.Typefaces;

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
    }

    public SubscriptionsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mTagColor = context.getResources().getColor(R.color.tag_title);
    }

    @Override
    protected FeedCursorAdapter.ViewHolder getHolder(View view) {
        ViewHolder holder = new ViewHolder();
        super.initHolder(view, holder);
        holder.tags = (LinearLayout)view.findViewById(R.id.tagsListContainer);
        return holder;
    }

    @Override
    protected void bindDescription(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {

    }

    @Override
    public void bindView(@NotNull View view, Context context, @NotNull Cursor cursor) {
        super.bindView(view, context, cursor);
        try {
            ViewHolder holder = (ViewHolder)view.getTag();
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
    }

    @Override
    protected void bindAvatar(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {

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
                holder.author.setTypeface(mNormalFont);
                holder.avatar.setVisibility(View.GONE);
                holder.footer.setVisibility(View.VISIBLE);
            } else {
                holder.tags.addView(tagView);
            }
        }
        holder.description.setVisibility(description_set ? View.VISIBLE : View.GONE);
        holder.tags.setVisibility(View.VISIBLE);
        return holder;
    }
}
