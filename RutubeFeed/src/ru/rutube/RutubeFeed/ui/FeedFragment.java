package ru.rutube.RutubeFeed.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.FeedController;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public class FeedFragment extends SherlockFragment implements FeedController.FeedView, AdapterView.OnItemClickListener {
    private static final String LOG_TAG = FeedFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    protected FeedController mController;
    private MenuItem mRefreshItem;
    private Uri feedUri;
    private ListView sgView;
    private SearchView mSearchView;
    private Menu mMenu;
    private Animation mRotateAnimation;

    private View.OnClickListener mRefreshClickListener = new View.OnClickListener() {
        // Обработчик клика по ImageView для кнопки "обновить"
        @Override
        public void onClick(View view) {
            if (D) Log.d(LOG_TAG, "IV onClick");
            refreshFeed();
        }
    };

    @Override
    public void openFeed(Uri feedUri) {
        RutubeApp.getInstance().openFeed(feedUri, getActivity());
    }

    public ListAdapter getListAdapter() {
        return sgView.getAdapter();
    }

    public void setListAdapter(ListAdapter adapter) {
        sgView.setAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (D) Log.d(LOG_TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);

        mMenu = menu;
        if (menu.size() == 0)
            inflater.inflate(R.menu.feed_menu, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        assert searchItem != null;
        Activity activity = getActivity();
        // Иногда успевает вызваться в момент, когда активити недоступно.
        if (activity != null)
            setupSearchMenuItem(searchItem, activity);

    }

    private void setupSearchMenuItem(MenuItem searchItem, Activity activity) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) searchItem.getActionView();
        assert mSearchView != null;
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        mSearchView.setFocusable(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (D) Log.d(LOG_TAG, "onOptionsItemSelected");
        int id = item.getItemId();
        // Клик по Refresh работает с ImageView.onClick
        if (id == R.id.menu_search) {
            if (D) Log.d(LOG_TAG, "Search btn!");
            return false;
        }
        if (D) Log.d(LOG_TAG, "super.onOptionsItemSelected");
        return super.onOptionsItemSelected(item);

    }

    protected void refreshFeed() {
        mController.refresh();

    }

    @Override
    public void openPlayer(Uri uri, Uri thumbnailUri) {
        Activity activity = getActivity();
        assert activity != null;
        Intent intent = new Intent("ru.rutube.player.play");
        intent.setData(uri);
        intent.putExtra(Constants.Params.THUMBNAIL_URI, thumbnailUri);
        if (D) Log.d(LOG_TAG, "Starting player");
        startActivityForResult(intent, 0);
        if (D) Log.d(LOG_TAG, "Player started");
    }

    @Override
    public FeedCursorAdapter initAdapter() {
        return new FeedCursorAdapter(getActivity(),
            R.layout.feed_item,
            null,
            new String[]{FeedContract.FeedColumns.TITLE, FeedContract.FeedColumns.THUMBNAIL_URI},
            new int[]{R.id.titleTextView, R.id.thumbnailImageView},
            0);
    }

    public void showError() {
        Activity activity = getActivity();
        if (activity != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.
                    setTitle(android.R.string.dialog_alert_title).
                    setMessage(getString(R.string.failed_to_load_data)).
                    create().
                    show();
        }
        doneRefreshing();
    }

    private void init() {
        initFeedUri();
        mController = new FeedController(getFeedUri());
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
        if (D) Log.d(LOG_TAG, "onActivityCreated");
        setHasOptionsMenu(true);
        init();
    }

    protected void initFeedUri() {
        Bundle args = getArguments();
        if (args != null)
            setFeedUri(((Uri)args.getParcelable(Constants.Params.FEED_URI)));
        if (getFeedUri() == null) {
            Activity activity = getActivity();
            assert activity != null;
            setFeedUri(activity.getIntent().getData());
        }
        if (D) Log.d(LOG_TAG, "Feed Uri:" + String.valueOf(getFeedUri()));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (D) Log.d(LOG_TAG, "onPrepareOptionsMenu" + String.valueOf(getTag()));
        mRefreshItem = menu.findItem(R.id.menu_refresh);
        FragmentActivity activity = getActivity();
        // NPE, куда же без него
        if (activity == null)
            return;
        mRotateAnimation = AnimationUtils.loadAnimation(activity, R.anim.rotate_icon);
        assert mRotateAnimation != null;
        mRotateAnimation.setRepeatCount(Animation.INFINITE);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_btn, null);
        iv.setOnClickListener(mRefreshClickListener);
        mRefreshItem.setActionView(iv);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSearchView != null)
            mSearchView.clearFocus();
    }

    public void setRefreshing() {
        boolean isLoading = RutubeApp.isLoadingFeed();
        if (isLoading)
            return;
        RutubeApp.startLoading();
        if (mRefreshItem == null) {
            if (D) Log.d(LOG_TAG, "empty refresh item");
            return;
        }
        Activity activity = getActivity();
        if (activity == null)
            return;
        if (!isLoading)
            mRefreshItem.getActionView().startAnimation(mRotateAnimation);
    }

    public void doneRefreshing() {
        RutubeApp.stopLoading();
        if (mRefreshItem == null)
            return;
        mRefreshItem.getActionView().clearAnimation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.feed_fragment, container, false);
        sgView = (ListView)view.findViewById(android.R.id.list);
        assert sgView != null;
        sgView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mController.onListItemClick(position);
    }

    @Override
    public boolean onItemClick(FeedCursorAdapter.ClickTag dataTag, String viewTag) {
        if (D) Log.d(LOG_TAG, String.format("onItemClick: %d %s", dataTag.position, viewTag));
        return false;
    }

    protected Uri getFeedUri() {
        return feedUri;
    }

    protected void setFeedUri(Uri feedUri) {
        this.feedUri = feedUri;
    }
}
