package ru.rutube.RutubeFeed.ui;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.HashMap;
import java.util.Map;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.NavigationController;
import ru.rutube.RutubeFeed.data.NavAdapter;
import ru.rutube.RutubeFeed.helpers.Typefaces;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseActivity extends ActionBarActivity implements NavigationController.NavigationView {
    private static final String LOG_TAG = ShowcaseActivity.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavAdapter mNaviAdapter;

    private NavigationController mController;
    private NetworkImageView mAvatarView;
    private View mAnonymousView;
    private View mAuthUserView;
    private NetworkImageView mBackgroundView;
    private TextView mUsernameView;
    private Typeface mNormalFont;
    private View mLogoutView;


    private class ShowcaseFragmentCache {
        Map<Uri, Fragment> mFragmentMap = new HashMap<Uri, Fragment>();
        public Fragment getShowcaseFragment(Uri uri, int showcaseId) {
            if (D) Log.d(LOG_TAG, "get fragment for " + String.valueOf(uri));
            Fragment f = mFragmentMap.get(uri);
//            if (f != null) {
//                if (D)Log.d(LOG_TAG, "got from cache");
//                return f;
//            }
            if (D) Log.d(LOG_TAG, "creating new fragment");
            f = initShowcaseFragment();
            Bundle args = new Bundle();
            args.putParcelable(Constants.Params.SHOWCASE_URI, uri);
            args.putInt(Constants.Params.SHOWCASE_ID, showcaseId);
            f.setArguments(args);
//            mFragmentMap.put(uri, f);
            return f;
        }
    }

    protected ShowcaseFragment initShowcaseFragment() {
        return new ShowcaseFragment();
    }

    private ShowcaseFragmentCache mFragmentCache = new ShowcaseFragmentCache();

    private User mUser;
    private AdapterView.OnItemClickListener mOnNavigationClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long rowId) {
            if (D) Log.d(LOG_TAG, "onItemClick: " + String.valueOf(pos));
            if (pos == 1) {
                // Login click
                if (mUser.isAnonymous())
                    showLoginDialog();
                return;
            }
            if (pos == 0) {
                // Authorized header click
                return;
            }
            if (D)Log.d(LOG_TAG, String.format("Count: %d Headers: %d Footers %d", mDrawerList.getCount(),
                    mDrawerList.getHeaderViewsCount(), mDrawerList.getFooterViewsCount()));
            if (pos == mDrawerList.getCount() - 1) {
                // Logout click
                mUser = mController.logout();
                showHeader(mUser);
                return;
            }
            NavAdapter.ViewHolder holder = (NavAdapter.ViewHolder)view.getTag();
            Uri showcaseUri = holder.naviItem.getUri();
            int showcaseId = holder.naviItem.getId();
            if (D)Log.d(LOG_TAG, "On Nav Click: " + String.valueOf(showcaseId));
            navigateToShowcase(showcaseUri, showcaseId);
            mNaviAdapter.setCurrentItemPosition(pos - mDrawerList.getHeaderViewsCount());
            mDrawerLayout.closeDrawers();
            mNaviAdapter.notifyDataSetChanged();
        }
    };

    private void showLoginDialog() {
        LoginDialogFragment.InputDialogFragmentBuilder builder = new LoginDialogFragment.InputDialogFragmentBuilder(this);
        builder.setOnDoneListener(new LoginDialogFragment.OnDoneListener() {
            @Override
            public void onFinishInputDialog(String emailText, String passwordText) {
                mController.requestToken(emailText, passwordText);
            }
        }).show();
    }

    public void navigateToShowcase(Uri showcaseUri, int showcaseId) {
        if (D) Log.d(LOG_TAG, "Navigating to " + String.valueOf(showcaseUri));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment f = mFragmentCache.getShowcaseFragment(showcaseUri, showcaseId);
        Fragment old = fm.findFragmentById(R.id.content_placeholder);
        if (old != null) {
            ft.remove(old);
        }
        ft.add(R.id.content_placeholder, f);
        ft.commit();
    }


    @Override
    public CursorAdapter getNavAdapter() {
        ListAdapter adapter = mDrawerList.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        }
        return (CursorAdapter)adapter;
    }

    @Override
    public void showHeader(User user) {
        mUser = user;
        if (user.isAnonymous()) {
            mAnonymousView.setVisibility(View.VISIBLE);
            mBackgroundView.setVisibility(View.GONE);
            mAuthUserView.setVisibility(View.GONE);
            mLogoutView.setVisibility(View.GONE);

        } else {
            mAnonymousView.setVisibility(View.GONE);
            String backgroundUrl = user.getBackgroundUrl();
            String avatarUrl = user.getAvatarUrl();
            mBackgroundView.setVisibility(View.VISIBLE);
            mBackgroundView.setImageUrl(backgroundUrl, mController.getImageLoader());
            mAvatarView.setImageUrl(avatarUrl, mController.getImageLoader());
            mUsernameView.setText(user.getName());
            mLogoutView.setVisibility(View.VISIBLE);
            mAuthUserView.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.left_drawer).forceLayout();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showcase_activity);
        mController = new NavigationController();
        initViews();
        mUser = User.fromContext();
        showHeader(mUser);
        initNavigationToggle();

        initNavigationAdapter();

        mDrawerList.setAdapter(mNaviAdapter);
        mDrawerList.setOnItemClickListener(mOnNavigationClickListener);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        Uri uri = getIntent().getData();
        navigateToShowcase(uri, 0);


        initCurrentNavItem();
    }

    private void initViews() {
        mNormalFont = Typefaces.get(this, "fonts/opensansregular.ttf");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.nav_list_view);
        ViewGroup v;
        mBackgroundView = (NetworkImageView) findViewById(R.id.nav_background);

        v = (ViewGroup)getLayoutInflater().inflate(R.layout.authorized_header, null);
        assert v != null;
        mDrawerList.addHeaderView(v);

        mAuthUserView = v.findViewById(R.id.headerContent);
        mAuthUserView.setVisibility(View.GONE);

        v = (ViewGroup)getLayoutInflater().inflate(R.layout.anonymous_header, null);
        assert v != null;
        mDrawerList.addHeaderView(v);

        mAnonymousView = v.findViewById(R.id.loginMenu);
        mAnonymousView.setVisibility(View.GONE);
        ((TextView)v.findViewById(R.id.loginTextView)).setTypeface(mNormalFont);

        v = (ViewGroup)getLayoutInflater().inflate(R.layout.logout_footer, null);
        assert v != null;
        mDrawerList.addFooterView(v);
        mLogoutView = v.findViewById(R.id.logoutView);
        ((TextView)v.findViewById(R.id.logoutTextView)).setTypeface(mNormalFont);

        findViewById(R.id.list_footer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // иначе клик попадает на карточку видео
            }
        });

        mAvatarView = (NetworkImageView)mAuthUserView.findViewById(R.id.avatarImageView);
        mUsernameView = (TextView) mAuthUserView.findViewById(R.id.authorTextView);
        mUsernameView.setTypeface(mNormalFont);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mController.attach(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mController.detach();
    }

    public void initCurrentNavItem() {
        mNaviAdapter.setCurrentItemPosition(0);
    }

    protected void initNavigationToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_closed
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                showHeader(mUser);
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    protected void initNavigationAdapter() {
        // Пустые массивы в аргументах нужны чтобы не было NPE в swapCursor.
        mNaviAdapter = new NavAdapter(
                this,
                R.layout.drawer_list_item,
                null,
                new String[]{},
                new int[]{}, 0);
    }


}
