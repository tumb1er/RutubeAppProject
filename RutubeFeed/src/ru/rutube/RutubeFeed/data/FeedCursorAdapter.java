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
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeFeed.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 11.05.13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class FeedCursorAdapter extends SimpleCursorAdapter {
    protected static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static final SimpleDateFormat reprDateFormat = new SimpleDateFormat("d MMMM y");
    protected ImageLoader imageLoader;
    protected static int item_layout_id = R.layout.feed_item;
    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;
    private Context context;
    private int mPerPage;
    private boolean mHasMore;

    public void setHasMore(boolean hasMore) {
        mHasMore = hasMore;
    }

    protected static class ViewHolder {
        TextView title;
        TextView created;
        TextView description;
        TextView author;
        NetworkImageView thumbnail;
        NetworkImageView avatar;
        View commentLine;
    }

    public int getPerPage() {
        return mPerPage;
    }

    public void setPerPage(int perPage) {
        this.mPerPage = perPage;
    }

    public interface LoadMoreListener
    {
        public void onLoadMore();
        public void onItemRequested(int position);
    }

    private LoadMoreListener loadMoreListener = null;

    public void setLoadMoreListener(LoadMoreListener listener) {
        loadMoreListener = listener;
    }
    public FeedCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        mPerPage = 20;
        mHasMore = true;
        initImageLoader(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(item_layout_id, null);
        assert view != null;
        ViewHolder holder = new ViewHolder();
        initHolder(view, holder);
        holder.avatar.setDefaultImageResId(R.drawable.editors_av);
        holder.avatar.setImageResource(R.drawable.editors_av);
        view.setTag(holder);
        return view;
    }

    protected ViewHolder initHolder(View view, ViewHolder holder) {
        holder.title = (TextView)view.findViewById(R.id.titleTextView);
        holder.description = (TextView)view.findViewById(R.id.descriptionTextView);
        holder.author = (TextView)view.findViewById(R.id.authorTextView);
        holder.created = (TextView)view.findViewById(R.id.createdTextView);
        holder.commentLine = view.findViewById(R.id.commentLine);
        holder.avatar = (NetworkImageView)view.findViewById(R.id.avatarImageView);
        holder.avatar.setDefaultImageResId(R.drawable.stub);
        holder.thumbnail = (NetworkImageView)view.findViewById(R.id.thumbnailImageView);
        holder.thumbnail.setDefaultImageResId(R.drawable.stub);
        return holder;
    }

    @Override
    public void bindView(@NotNull View view, Context context, @NotNull Cursor cursor) {
        try {
            ViewHolder holder = (ViewHolder)view.getTag();
            bindTitle(cursor, holder);
            bindCreated(cursor, holder);
            bindDescription(cursor, holder);
            bindAuthor(cursor, holder);
            bindThumbnail(cursor, holder);
            bindAvatar(cursor, holder);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    protected void bindAvatar(Cursor cursor, ViewHolder holder) {
        int avatarIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.AVATAR_URI);
        String avatarUri = cursor.getString(avatarIndex);
        int visibility;
        // При отсутствии аватара скрываем его ImageView и горизонтальную черту
        visibility = (avatarUri == null) ? View.GONE : View.VISIBLE;
        holder.avatar.setVisibility(visibility);
        holder.commentLine.setVisibility(visibility);
        holder.avatar.setImageUrl(avatarUri, imageLoader);
    }

    protected void bindAuthor(Cursor cursor, ViewHolder holder) {
        int authorNameIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.AUTHOR_NAME);
        String authorName = cursor.getString(authorNameIndex);
        // При отсутствии имени автора скрываем соответствующий TextField
        int visibility = (authorName == null) ? View.GONE : View.VISIBLE;
        holder.author.setVisibility(visibility);
        holder.author.setText(authorName);
    }

    protected void bindCreated(Cursor cursor, ViewHolder holder) {
        int createdIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.CREATED);
        Date created = null;
        try {
            String created_str = cursor.getString(createdIndex);
            created = sqlDateFormat.parse(created_str);
        } catch (ParseException ignored) {
            if (D) Log.e(getClass().getName(), "CR Parse error");
        }
        if (created != null)
            holder.created.setText(getCreatedText(created));
    }

    protected void bindDescription(Cursor cursor, ViewHolder holder) {
        int descriptionIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.DESCRIPTION);
        String description = cursor.getString(descriptionIndex);
        assert description!=null;
        if (description.indexOf('<') >= 0)
            holder.description.setText(Html.fromHtml(description));
        else
            holder.description.setText(description);
    }

    protected void bindThumbnail(Cursor cursor, ViewHolder holder) {
        int thumbnailUriIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.THUMBNAIL_URI);
        String thumbnailUri = cursor.getString(thumbnailUriIndex);
        holder.thumbnail.setImageUrl(thumbnailUri, imageLoader);
    }

    protected void bindTitle(Cursor cursor, ViewHolder holder) {
        int titleIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.TITLE);
        String title = cursor.getString(titleIndex);
        holder.title.setText(title);
    }

    protected String getCreatedText(Date created) {
        Date now = new Date();
        long seconds = (now.getTime() - created.getTime()) / 1000;
        if (seconds < 3600)
            return context.getString(R.string.now);
        if (seconds < 24 * 3600)
            return context.getString(R.string.today);
        if (seconds < 2 * 24 * 3600)
            return context.getString(R.string.yesterday);
        if (seconds < 5 * 24 * 3600)
            return String.format(context.getString(R.string.days_ago_24, seconds / (24 * 3600)));
        if (seconds < 7 * 24 * 3600)
            return String.format(context.getString(R.string.days_ago_59, seconds / (24 * 3600)));
        if (seconds < 14 * 24 * 3600)
            return String.format(context.getString(R.string.week_ago, seconds / (7 * 24 * 3600)));
        if (seconds < 31 * 24 * 3600)
            return String.format(context.getString(R.string.weeks_ago, seconds / (7 * 24 * 3600)));
        return reprDateFormat.format(created);
    }

    protected void initImageLoader(Context context) {
        imageLoader = new ImageLoader(Volley.newRequestQueue(context), RutubeApp.getBitmapCache());
    }

    protected ImageLoader getImageLoader(){
        return imageLoader;
    }

    @Override
    public Object getItem(int position) {
        processPosition(position);
        return super.getItem(position);
    }

    private void processPosition(int position) {
        if (mHasMore && (position > getCount() - mPerPage / 2)) {
            if (D) Log.d(LOG_TAG, String.format("Load more: %d of %d", position, getCount()));
            loadMore();
        } else {
            if (loadMoreListener != null)
                loadMoreListener.onItemRequested(position);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        processPosition(position);
        return super.getView(position, convertView, parent);
    }

    private void loadMore() {
        if (D) Log.i(LOG_TAG, "Load more");
        if (loadMoreListener!=null)
            loadMoreListener.onLoadMore();
    }
}
