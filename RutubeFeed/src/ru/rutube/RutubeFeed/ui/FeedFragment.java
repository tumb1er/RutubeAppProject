package ru.rutube.RutubeFeed.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.FeedController;
import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
import ru.rutube.RutubeFeed.feed.BasicFeedImpl;
import ru.rutube.RutubeFeed.feed.SubscriptionsFeedImpl;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public class FeedFragment extends Fragment implements FeedController.FeedView, AdapterView.OnItemClickListener {
    private static final String LOG_TAG = FeedFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final String CONTROLLER = "controller";
    protected FeedController mController;
    private MenuItem mRefreshItem;
    private MenuItem mSearchItem;
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
    private FeedFragment.FeedImpl mFeedImpl;
    protected SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            if (D) Log.d(LOG_TAG, "SEARCH SUBMIT");
            closeSearchWidget();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    protected SearchView.OnSuggestionListener mOnSuggestionListener = new SearchView.OnSuggestionListener() {

        @Override
        public boolean onSuggestionSelect(int i) {
            if (D) Log.d(LOG_TAG, "SEARCH-SUGGEST SELECT");
            return false;
        }

        @Override
        public boolean onSuggestionClick(int i) {
            if (D) Log.d(LOG_TAG, "SEARCH-SUGGEST CLICK");
            closeSearchWidget();
            return false;
        }
    };

    public FeedFragment(FeedImpl feedImpl) {
        mFeedImpl = feedImpl;
    }

    public FeedFragment() {
        mFeedImpl = new BasicFeedImpl();
    }

    @Override
    public void openFeed(Uri feedUri, String title) {
        RutubeApp.getInstance().openFeed(feedUri, getActivity(), title);
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
        mSearchItem = menu.findItem(R.id.menu_search);
        assert mSearchItem != null;
        Activity activity = getActivity();
        // Иногда успевает вызваться в момент, когда активити недоступно.
        if (activity != null)
            setupSearchMenuItem(mSearchItem, activity);
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

    protected void setupSearchMenuItem(MenuItem searchItem, Activity activity) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        mSearchView = (SearchView)MenuItemCompat.getActionView(searchItem);
        // mSearchView = (SearchView) searchItem.getActionView();

        if (mSearchView != null) {
            // Assumes current activity is the searchable activity
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
            mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            mSearchView.setFocusable(false);
            mSearchView.setQueryRefinementEnabled(true);
            mSearchView.setOnQueryTextListener(mOnQueryTextListener);
            mSearchView.setOnSuggestionListener(mOnSuggestionListener);
        }
    }

    protected void closeSearchWidget() {
        if (mSearchItem != null) {
            if (D) Log.d(LOG_TAG, "collapsing");
            MenuItemCompat.collapseActionView(mSearchItem);
            mSearchView.onActionViewCollapsed();
        }
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
        return mFeedImpl.initAdapter();
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

    private void init(Bundle savedInstanceState) {
        initFeedUri();
        if (savedInstanceState == null)
            mController = new FeedController(getFeedUri(), 0);
        else
            mController = savedInstanceState.getParcelable(CONTROLLER);
        // в FeedImpl необходимо наличие валидного контекста на момент вызова Controller.attach
        mFeedImpl.setContext(getActivity());
        mController.attach(getActivity(), this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (D) Log.d(LOG_TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController.detach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTROLLER, mController);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) Log.d(LOG_TAG, "onActivityCreated");
        setHasOptionsMenu(true);
        init(savedInstanceState);
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

        MenuItemCompat.setActionView(mRefreshItem, iv);
        // mRefreshItem.setActionView(iv);

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
            MenuItemCompat.getActionView(mRefreshItem).startAnimation(mRotateAnimation);
            // mRefreshItem.getActionView().startAnimation(mRotateAnimation);
    }

    public void doneRefreshing() {
        RutubeApp.stopLoading();
        if (mRefreshItem == null)
            return;
        MenuItemCompat.getActionView(mRefreshItem).clearAnimation();
        //mRefreshItem.getActionView().clearAnimation();
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

    public void checkLoadMore() {
        try {
            if (D) Log.d(LOG_TAG, "checkLoadMore");
            if (getListAdapter().getCount() == 0) {
                if (D) Log.d(LOG_TAG, "Feed is empty");
                mController.checkLoadMore();
            }
        } catch (NullPointerException ignored) {}

    }

    @Override
    public void setSelectedItem(int position) {
        try {
            sgView.setSelection(position);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getCurrentPosition() {
        try{
            return sgView.getFirstVisiblePosition();
        } catch (NullPointerException ignored) {
            return 0;
        }
    }

    public void logout() {
        mController.logout();
    }

    public void setFeedImplementation(FeedImpl feedImpl) {
        feedImpl.setContext(getActivity());
        mFeedImpl = feedImpl;
    }

    public void setEmptyText(String text) {
        View v = getView();
        if (v == null)
            return;
        TextView tv = (TextView)v.findViewById(R.id.empty);
        if (tv!= null)
            tv.setText(text);
    }


    public interface FeedImpl {
        public FeedCursorAdapter initAdapter();
        public void setContext(Context context);
    }
}
