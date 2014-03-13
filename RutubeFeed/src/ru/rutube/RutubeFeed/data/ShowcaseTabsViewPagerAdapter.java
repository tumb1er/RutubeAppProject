package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import java.util.HashMap;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeFeed.feed.FeedFragmentFactory;
import ru.rutube.RutubeFeed.ui.FeedFragment;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseTabsViewPagerAdapter extends FragmentStatePagerAdapter
        implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = ShowcaseTabsViewPagerAdapter.class.getName();
    protected final FeedFragmentFactory mFragmentFactory = new FeedFragmentFactory();
    protected boolean mDataValid;
    protected Cursor mCursor;
    protected Context mContext;
    protected SparseIntArray mItemPositions;
    protected HashMap<Object, Integer> mObjectMap;
    protected int mRowIDColumn;

    public ShowcaseTabsViewPagerAdapter(Context context, FragmentManager fm, Cursor cursor) {
        super(fm);

        init(context, cursor);
    }

    void init(Context context, Cursor c) {
        mObjectMap = new HashMap<Object, Integer>();
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemPosition(Object object) {
        Integer rowId = mObjectMap.get(object);
        if (rowId != null && mItemPositions != null) {
            return mItemPositions.get(rowId, POSITION_NONE);
        }
        return POSITION_NONE;
    }

    public void setItemPositions() {
        mItemPositions = null;

        if (mDataValid) {
            int count = mCursor.getCount();
            mItemPositions = new SparseIntArray(count);
            mCursor.moveToPosition(-1);
            while (mCursor.moveToNext()) {
                int rowId = mCursor.getInt(mRowIDColumn);
                int cursorPos = mCursor.getPosition();
                mItemPositions.append(rowId, cursorPos);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (D) Log.d(LOG_TAG, "GETITEM: " + String.valueOf(position));
        if (mDataValid) {
            mCursor.moveToPosition(position);
            return getItem(mContext, mCursor);
        } else {
            return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mObjectMap.remove(object);

        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        int rowId = mCursor.getInt(mRowIDColumn);
        Object obj = super.instantiateItem(container, position);
        mObjectMap.put(obj, Integer.valueOf(rowId));

        return obj;
    }

    public Fragment getItem(Context context, Cursor cursor) {
        FeedFragment f = mFragmentFactory.getFeedFragment(FeedFragmentFactory.EDITORS);
        Bundle b = new Bundle();
        b.putParcelable(Constants.Params.FEED_URI, Uri.parse("http://rutube.ru/video/editors/"));
        f.setArguments(b);
        int i = cursor.getColumnIndex(FeedContract.ShowcaseTabs.NAME);
        f.setTitle(cursor.getString(i));
        return f;
    }

    @Override
    public int getCount() {
        if (mDataValid) {
            if (D)Log.d(LOG_TAG, "GetCount: " + String.valueOf(mCursor.getCount()));
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mDataValid) {
            FeedFragment f = (FeedFragment)getItem(position);
            return f.getTitle();
        } else {
            return "";
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (D) Log.d(LOG_TAG, "Swap cursor to: " + String.valueOf(newCursor));
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
        }

        setItemPositions();
        notifyDataSetChanged();

        return oldCursor;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}