package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import ru.rutube.RutubeAPI.models.VideoTag;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.helpers.Typefaces;

/**
 * Created by tumbler on 14.09.13.
 */
public class TagsListAdapter extends ArrayAdapter<VideoTag> {
    protected Typeface mNormalFont;
    protected Typeface mLightFont;

    public TagsListAdapter(Context context, int resourceId) {
        super(context, resourceId);
        initTypefaces(context);
    }

    public TagsListAdapter(Context context, int resource_id, VideoTag[] values) {
        super(context, resource_id, values);
        initTypefaces(context);
    }

    protected void initTypefaces(Context mContext) {
        mNormalFont = Typefaces.get(mContext, "fonts/opensansregular.ttf");
        mLightFont = Typefaces.get(mContext, "fonts/opensanslight.ttf");
    }

    public class ViewHolder {
        public TextView title;
        public TextView comment;
    }

    protected ViewHolder init_holder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) view.findViewById(R.id.title);
        holder.comment = (TextView) view.findViewById(R.id.comment);
        return holder;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.tag_item, null);
            assert view != null;
            view.setTag(init_holder(view));
        }
        ViewHolder holder = (ViewHolder)view.getTag();
        VideoTag item = getItem(position);
        initView(holder, item);
        return view;
    }

    protected void initView(TagsListAdapter.ViewHolder holder, VideoTag item) {
        if (item != null) {
            holder.title.setText("#" + item.getTag());
            holder.title.setTypeface(mNormalFont);
            String message = item.getMessage();
            holder.comment.setText(message);
            holder.comment.setTypeface(mLightFont);

            if (message == null || message.isEmpty()) {
                holder.comment.setVisibility(View.GONE);
            } else {
                holder.comment.setVisibility(View.VISIBLE);
            }
        }
    }
}
