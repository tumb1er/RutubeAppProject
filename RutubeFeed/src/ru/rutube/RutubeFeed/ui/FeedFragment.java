package ru.rutube.RutubeFeed.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
//import com.huewu.pla.lib.MultiColumnListView;
//import com.huewu.pla.lib.internal.PLA_AdapterView;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.content.ContentMatcher;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.Feed;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.FeedController;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
import ru.rutube.RutubePlayer.ui.PlayerActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public class FeedFragment extends ListFragment implements FeedController.FeedView {
    private static final String LOG_TAG = FeedFragment.class.getName();
    private MenuItem refreshItem;
    private Uri feedUri;
//    private MultiColumnListView sgView;
    private ListView sgView;
    private FeedController mController;

    public ListAdapter getListAdapter() {
        return sgView.getAdapter();
    }
    public void setListAdapter(ListAdapter adapter) {
        sgView.setAdapter(adapter);
    }


//    private PLA_AdapterView.OnItemClickListener onItemClickListener = new PLA_AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
//            Cursor c = (Cursor) getListAdapter().getItem(position);
//            int videoIdIndex = c.getColumnIndex(FeedContract.FeedColumns._ID);
//            String videoId = c.getString(videoIdIndex);
//            Log.d(getClass().getName(), "Clicked " + videoId);
//            Intent intent = new Intent(getActivity(), PlayerActivity.class);
//            Uri uri = Uri.parse(getActivity().getString(R.string.base_uri))
//                    .buildUpon()
//                    .appendPath("video")
//                    .appendPath(videoId).build();
//            intent.setData(uri);
//            startActivity(intent);
//        }
//    };

//
//    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
//        @Override
//        public void onScrollStateChanged(AbsListView absListView, int i) {
//
//        }
//
//        @Override
//        public void onScroll(AbsListView absListView, int firstVisible, int visibleCount, int totalCount) {
//            boolean loadMore = /* maybe add a padding */
//                    firstVisible + visibleCount >= totalCount - perPage / 2;
//
//            Log.d(LOG_TAG, String.format("OnScroll: %d + %d >= %d - %d", firstVisible, visibleCount, totalCount, perPage / 2));
//            if (loadMore) {
//                int new_page = (totalCount + perPage) / perPage;
//                Log.d(LOG_TAG, "new page: " + String.valueOf(new_page));
//                if (!loading) {
//                    Log.d(LOG_TAG, "Load more");
//                    loadPage(new_page);
//                }
//
//            }
//        }
//    };

    //@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = (Cursor) getListAdapter().getItem(position);
        int videoIdIndex = c.getColumnIndex(FeedContract.FeedColumns._ID);
        String videoId = c.getString(videoIdIndex);
        Log.d(getClass().getName(), "Clicked " + videoId);
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        Uri uri = Uri.parse(getActivity().getString(R.string.base_uri))
                .buildUpon()
                .appendPath("video")
                .appendPath(videoId).build();
        intent.setData(uri);
        startActivity(intent);
    }

    public void showError() {
        Activity activity = getActivity();
        if (activity != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.
                    setTitle(android.R.string.dialog_alert_title).
                    setMessage(getString(R.string.faled_to_load_data)).
                    create().
                    show();
        }
        doneRefreshing();
    }

    private void init() {
        initFeedUri();
        mController = new FeedController(feedUri);
        mController.attach(getActivity(), this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController.detach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
        setHasOptionsMenu(true);
        init();
    }

    private void initFeedUri() {
        Bundle args = getArguments();
        if (args != null)
            feedUri = args.getParcelable(Constants.Params.FEED_URI);
        if (feedUri == null)
            feedUri = getActivity().getIntent().getData();
        Log.d(LOG_TAG, "Feed Uri:" + String.valueOf(feedUri));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onPrepareOptionsMenu");
        refreshItem = menu.findItem(R.id.menu_refresh);
        super.onPrepareOptionsMenu(menu);
    }

    public void setRefreshing() {
        if (refreshItem == null) {
            Log.d(LOG_TAG, "empty refresh item");
            return;
        }
        Activity activity = getActivity();
        if (activity == null)
            return;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_btn, null);
        assert iv != null;
        Animation rotation = AnimationUtils.loadAnimation(activity, R.anim.rotate_icon);
        assert rotation != null;
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        refreshItem.setActionView(iv);
    }

    public void doneRefreshing() {
        if (refreshItem == null)
            return;
        View actionView = refreshItem.getActionView();
        if (actionView != null)
            actionView.clearAnimation();
        refreshItem.setActionView(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d(LOG_TAG, "onCreateView");
//        sgView = (MultiColumnListView)inflater.inflate(R.layout.feed_fragment, container, false);
//        sgView.setOnItemClickListener(onItemClickListener);
        sgView = (ListView)inflater.inflate(R.layout.feed_fragment, container, false);
        return sgView;
    }

}
