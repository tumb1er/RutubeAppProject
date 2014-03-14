package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.NaviItem;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.helpers.Typefaces;

/**
 * Created by tumbler on 11.03.14.
 */
public class NavAdapter extends SimpleCursorAdapter {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = NavAdapter.class.getName();
    private final Typeface mNormalFont;
    private final Typeface mLightFont;
    private int mCurrentItemPosition;
    public NavAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mNormalFont = Typefaces.get(context, "fonts/opensansregular.ttf");
        mLightFont = Typefaces.get(context, "fonts/opensanslight.ttf");
    }

    public void setCurrentItemPosition(int pos) {
        mCurrentItemPosition = pos;
    }

    public class ViewHolder {
        public TextView title;
        public TextView name;
        public NaviItem naviItem;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.drawer_list_item, null);
        ViewHolder holder = initHolder(view);
        assert view != null;
        view.setTag(holder);
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        assert view != null;
        ViewHolder h = (ViewHolder)view.getTag();
        if (position == mCurrentItemPosition) {
            h.name.setTextColor(mContext.getResources().getColor(R.color.active_nav_text));
            h.name.setTypeface(mNormalFont, Typeface.BOLD);
        } else {
            h.name.setTextColor(mContext.getResources().getColor(R.color.inactive_nav_text));
            h.name.setTypeface(mLightFont, Typeface.NORMAL);
        }
        return view;
    }

    private ViewHolder initHolder(ViewGroup view) {
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView)view.findViewById(R.id.nameTextView);
        return holder;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        ViewHolder holder = (ViewHolder)view.getTag();
        NaviItem item = NaviItem.fromCursor(cursor);
        holder.name.setText(item.getName());
        holder.naviItem = item;
    }
}
