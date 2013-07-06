package ru.rutube.RutubeFeed.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
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

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.internal.PLA_AdapterView;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.content.ContentMatcher;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.Feed;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
import ru.rutube.RutubePlayer.ui.PlayerActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public class FeedFragment extends Fragment {
    private static final int LOADER_ID = 1;
    private static final String LOG_TAG = FeedFragment.class.getName();
    private static final String[] PROJECTION = {
            FeedContract.FeedColumns._ID,
            FeedContract.FeedColumns.TITLE,
            FeedContract.FeedColumns.DESCRIPTION,
            FeedContract.FeedColumns.CREATED,
            FeedContract.FeedColumns.THUMBNAIL_URI,
            FeedContract.FeedColumns.AUTHOR_NAME,
            FeedContract.FeedColumns.AVATAR_URI
    };
    protected static final int SELECT_VIDEO_REQUEST = 0;
    protected static final int SHOOT_VIDEO_REQUEST = 1;
    private ContentMatcher contentMatcher;
    private MenuItem refreshItem;
    private boolean loading;
    private int perPage;
    private Uri feedUri;
    private Uri contentUri;
    private MultiColumnListView sgView;
    private RequestQueue mRequestQueue;
    private Feed mFeed;

    private ListAdapter getListAdapter() {
        return sgView.getAdapter();
    }
    private void setListAdapter(ListAdapter adapter) {
        sgView.setAdapter(adapter);
    }
    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            Log.d(LOG_TAG, "onCreateLoader: " + contentUri.toString());
            return new CursorLoader(
                    getActivity(),
                    contentUri,
                    PROJECTION,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            Log.d(LOG_TAG, "onLoadFinished " + String.valueOf(cursor.getCount()));
            ((CursorAdapter) getListAdapter()).swapCursor(cursor);
            if (cursor.getCount() < perPage) {
                Log.d(LOG_TAG, "load more from olf");
                loadPage((cursor.getCount() + perPage) / perPage);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            ((CursorAdapter) getListAdapter()).swapCursor(null);
        }
    };

    private PLA_AdapterView.OnItemClickListener onItemClickListener = new PLA_AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
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
    };

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

//    //@Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        Cursor c = (Cursor) getListAdapter().getItem(position);
//        int videoIdIndex = c.getColumnIndex(FeedContract.FeedColumns._ID);
//        String videoId = c.getString(videoIdIndex);
//        Log.d(getClass().getName(), "Clicked " + videoId);
//        Intent intent = new Intent(getActivity(), PlayerActivity.class);
//        Uri uri = Uri.parse(getActivity().getString(R.string.base_uri))
//                .buildUpon()
//                .appendPath("video")
//                .appendPath(videoId).build();
//        intent.setData(uri);
//        startActivity(intent);
//    }
    private void showError() {
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
        loading = false;
    }
    private FeedCursorAdapter.LoadMoreListener loadMoreListener = new FeedCursorAdapter.LoadMoreListener(){

        @Override
        public void onLoadMore() {
            ListAdapter adapter = getListAdapter();
            loadPage((adapter.getCount() + perPage) / perPage);
        }
    };

    private void loadPage(int page) {
        loading = true;
        setRefreshing();
        Log.d(LOG_TAG, "Started loading page: " + feedUri.toString() + "; " + String.valueOf(contentUri));
        JsonObjectRequest request = mFeed.getFeedRequest(page);
        mRequestQueue.add(request);
    }

    private RequestListener mLoadPageRequestListener = new RequestListener() {
        @Override
        public void onResult(int tag, Bundle result) {
            Log.d(LOG_TAG, "onRequestFinished");
            if (sgView.getAdapter().getCount() == 0)
                getActivity().getContentResolver().notifyChange(contentUri, null);
            perPage = result.getInt(Constants.Result.PER_PAGE);
            doneRefreshing();
            loading = false;
        }

        @Override
        public void onVolleyError(VolleyError error) {
            showError();
        }

        @Override
        public void onRequestError(int tag, RequestError error) {
            showError();
        }
    };

    private void init() {
        // TODO: remove after transfer to DRF-2.3
        mRequestQueue = Volley.newRequestQueue(getActivity(),
                new HttpClientStack(HttpTransport.getHttpClient()));
        initFeedUri();
        initContentUri();
        mFeed = new Feed(null, feedUri.toString(), getActivity(), mLoadPageRequestListener);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
        init();
        setHasOptionsMenu(true);
        perPage = 20;
        FeedCursorAdapter adapter = new FeedCursorAdapter(getActivity(),
                R.layout.feed_item,
                null,
                new String[]{FeedContract.FeedColumns.TITLE, FeedContract.FeedColumns.THUMBNAIL_URI},
                new int[]{R.id.titleTextView, R.id.thumbnailImageView},
                0);
        adapter.setLoadMoreListener(loadMoreListener);
        setListAdapter(adapter);
        getLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
        //getListView().setOnScrollListener(onScrollListener);
        loadPage(1);
    }

    private void initContentUri() {
        contentMatcher = ContentMatcher.from(getActivity());
        contentUri = contentMatcher.getContentUri(feedUri);
        Log.d(LOG_TAG, "CUri: " + String.valueOf(contentUri));
    }

    private void initFeedUri() {
        Bundle args = getArguments();
        if (args != null)
            feedUri = args.getParcelable(Constants.Params.FEED_URI);
        if (feedUri == null)
            feedUri = getActivity().getIntent().getData();
        Log.d(LOG_TAG, "Feed Uri:" + String.valueOf(feedUri));
    }
//
//    @Override
//    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
//        int menu_id = item.getItemId();
//        if (menu_id == R.id.menu_refresh) {
//            loadPage(1);
//        }
//        if (menu_id == R.id.menu_upload_gallery) {
//            selectVideo();
//        }
//        if (menu_id == R.id.menu_upload_shoot) {
//            shootVideo();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void shootVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, SHOOT_VIDEO_REQUEST);
    }

    private void selectVideo() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        startActivityForResult(Intent.createChooser(i, getString(R.string.select_video)), SELECT_VIDEO_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_VIDEO_REQUEST:
                uploadVideo(data.getData());
                break;
            case SHOOT_VIDEO_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                    uploadVideo(data.getData());
                break;
            default:
                break;
        }
    }

    private void uploadVideo(Uri videoUri) {
        String uploadIntentAction = "ru.rutube.api.upload";
        Intent intent = new Intent(uploadIntentAction);
        intent.setData(videoUri);
        getActivity().startActivity(intent);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onPrepareOptionsMenu");
        refreshItem = menu.findItem(R.id.menu_refresh);
        super.onPrepareOptionsMenu(menu);
    }

    private void setRefreshing() {
        if (refreshItem == null) {
            Log.d(LOG_TAG, "empty refresh item");
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_btn, null);

        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_icon);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        refreshItem.setActionView(iv);
    }

    private void doneRefreshing() {
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
        sgView = (MultiColumnListView)inflater.inflate(R.layout.feed_fragment, container, false);
        sgView.setOnItemClickListener(onItemClickListener);
        return sgView;
    }

}
