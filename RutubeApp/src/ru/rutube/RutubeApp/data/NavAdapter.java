package ru.rutube.RutubeApp.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeApp.R;

/**
 * Created by tumbler on 11.03.14.
 */
public class NavAdapter extends SimpleCursorAdapter {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = NavAdapter.class.getName();
    public NavAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView)inflater.inflate(R.layout.drawer_list_item, null);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView tv = (TextView)view;
        int name_pos = cursor.getColumnIndex(FeedContract.Navigation.NAME);
        String name = cursor.getString(name_pos);
        int link_pos = cursor.getColumnIndex(FeedContract.Navigation.LINK);
        String link = cursor.getString(link_pos);
        if (D) Log.d(LOG_TAG, "Bind Nav: " + name);
        tv.setText(name);
        tv.setTag(link);
    }
}
