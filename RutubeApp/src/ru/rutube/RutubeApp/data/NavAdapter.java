package ru.rutube.RutubeApp.data;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by tumbler on 11.03.14.
 */
public class NavAdapter extends ArrayAdapter<String>{
    public NavAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }
}
