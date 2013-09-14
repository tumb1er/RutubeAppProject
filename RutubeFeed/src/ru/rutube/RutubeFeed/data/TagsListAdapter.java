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

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.tag_item, null);
        }

        VideoTag item = getItem(position);
        if (item != null) {
            TextView tv = (TextView) view.findViewById(R.id.title);
            tv.setText(item.getTag());
            tv = (TextView)view.findViewById(R.id.comment);
            tv.setText(item.getMessage());
        }

        return view;
    }
}
