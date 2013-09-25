package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;
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

    protected static class ViewHolder extends FeedCursorAdapter.ViewHolder {
        public ListView tags;
    }

    public SubscriptionsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(item_layout_id, null);
        assert view != null;
        ViewHolder holder = new ViewHolder();
        initHolder(view, holder);
        view.setTag(holder);
        return view;
    }

    protected ViewHolder initHolder(View view, ViewHolder holder) {
        super.initHolder(view, holder);
        holder.tags = (ListView)view.findViewById(R.id.tagsList);
        return holder;
    }

    @Override
    public void bindView(@NotNull View view, Context context, @NotNull Cursor cursor) {
        super.bindView(view, context, cursor);
        try {
            int tagsListIndex = cursor.getColumnIndexOrThrow(FeedContract.Subscriptions.TAGS_JSON);
            String tagsJson = cursor.getString(tagsListIndex);
            JSONArray tags = new JSONArray(tagsJson);
            VideoTag[] tagValues = new VideoTag[tags.length()];
            for(int i=0;i<tags.length();i++) {
                VideoTag tag = VideoTag.fromJSON(tags.getJSONObject(i));
                tagValues[i] = tag;
            }
            ViewHolder holder = (ViewHolder)view.getTag();
            holder.tags.setAdapter(new TagsListAdapter(mContext, R.layout.tag_item, tagValues));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
