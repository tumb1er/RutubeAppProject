package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import ru.rutube.RutubeAPI.models.VideoTag;
import ru.rutube.RutubeFeed.R;

/**
 * Created by tumbler on 14.09.13.
 */
public class TagsListAdapter extends ArrayAdapter<VideoTag> {
    public TagsListAdapter(Context context, int resourceId) {
        super(context, resourceId);
    }

    public TagsListAdapter(Context mContext, int resource_id, VideoTag[] values) {
        super(mContext, resource_id, values);
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
        if (item != null) {
            holder.title.setText("#" + item.getTag());
            String message = item.getMessage();
            holder.comment.setText(message);
            if (message == null || message.isEmpty()) {
                holder.comment.setVisibility(View.GONE);
            } else {
                holder.comment.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }
}
