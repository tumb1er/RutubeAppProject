package ru.rutube.RutubeApp.ctrl;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeAPI;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeAPI.requests.RequestListener;

/**
 * Created by tumbler on 14.07.13.
 */
public class MainPageController implements Parcelable, RequestListener {
    private static final String TAB_EDITORS = "editors";
    private static final String TAB_MY_VIDEO = "my_video";
    private static final String TAB_SUBSCRIPTIONS = "subscription";
    private static final String LOG_TAG = MainPageController.class.getName();

    private HashMap<String, Uri> feedUriMap;
    private RequestQueue mRequestQueue;

    @Override
    public void onResult(int tag, Bundle result) {
        String token = result.getString(Constants.Result.TOKEN);
        User.saveToken(mContext, token);
        loginSuccessful();
    }

    @Override
    public void onVolleyError(VolleyError error) {
        String response = new String(error.networkResponse.data);
        Log.e(LOG_TAG, "Volley Error" + response);
        if (response.contains("Unable to login with provided credentials") ||
                response.contains("username") ||
                response.contains("password"))
            showLoginDialog();
        else {
            mView.showError();
            loginCanceled();
        }
    }

    @Override
    public void onRequestError(int tag, RequestError error) {
        Log.e(LOG_TAG, "Request Error" + String.valueOf(error));
        mView.showError();
        loginCanceled();
    }

    public void loginCanceled() {
        Log.d(LOG_TAG, "Login cancelled, switching to " + mSelectedTab);
        mView.selectTab(mSelectedTab);
    }

    public interface MainPageView {
        void addFeedTab(String name, String tag);
        void selectTab(String tag);

        void showFeedFragment(String tag, Uri feedUri);

        void showLoginDialog();

        void showError();
    }

    private Context mContext = null;
    private MainPageView mView = null;
    private User mUser = null;
    private boolean mAttached = false;
    private String mSelectedTab;
    private String mAfterLoginTab;
    private boolean mTabsInited;

    public MainPageController() {
        this(TAB_EDITORS);
    }

    public MainPageController(String selectedTab) {
        mSelectedTab = selectedTab;
        mAfterLoginTab = null;
        mTabsInited = false;
    }

    public void startLoginRequests(String email, String password) {
        mRequestQueue.add(mUser.getTokenRequest(email, password, mContext, this));
    }

    public void loginSuccessful() {
        mUser = User.load(mContext);
        Uri feedUri = feedUriMap.get(mAfterLoginTab);
        mSelectedTab = mAfterLoginTab;
        mView.showFeedFragment(mAfterLoginTab, feedUri);
    }

    public void attach(Context context, MainPageView view) {
        assert mContext == null;
        assert mView == null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
            new HttpClientStack(HttpTransport.getHttpClient()));
        mUser = User.load(context);
        initFeedUriMap();
        mAttached = true;
    }

    private void initFeedUriMap() {
        feedUriMap = new HashMap<String, Uri>();
        feedUriMap.put(TAB_EDITORS, Uri.parse(RutubeAPI.getUrl(mContext, R.string.editors_uri)));
        feedUriMap.put(TAB_MY_VIDEO, Uri.parse(RutubeAPI.getUrl(mContext, R.string.my_video_uri)));
        feedUriMap.put(TAB_SUBSCRIPTIONS, Uri.parse(RutubeAPI.getUrl(mContext, R.string.subscription_uri)));
    }

    public  void detach() {
        mContext = null;
        mView = null;
        mAttached = false;
    }

    public void initTabs() {
        assert mAttached;
        // После добавления вкладки она автоматически делается активной, поэтому опускаем
        // флажок, разрешающий обработку события смены активной вкладки.
        mTabsInited = false;
        mView.addFeedTab(mContext.getString(ru.rutube.RutubeFeed.R.id.editors_feed), TAB_EDITORS);
        mView.addFeedTab(mContext.getString(ru.rutube.RutubeFeed.R.id.my_video), TAB_MY_VIDEO);
        mView.addFeedTab(mContext.getString(ru.rutube.RutubeFeed.R.id.subscriptions), TAB_SUBSCRIPTIONS);
        mTabsInited = true;
        Log.d(LOG_TAG, "selecting tab" + mSelectedTab);
        mView.selectTab(mSelectedTab);
        mView.showFeedFragment(mSelectedTab, feedUriMap.get(mSelectedTab));
    }


    public void onTabSelected(String tag) {
        if (!mTabsInited) {
            Log.d(LOG_TAG, "skip onTabSelected");
            return;
        }
        if (tag != TAB_EDITORS && !mUser.isAuthenticated()) {
            mAfterLoginTab = tag;
            showLoginDialog();
            return;
        }
        Uri feedUri = feedUriMap.get(tag);
        mSelectedTab = tag;
        Log.d(LOG_TAG, "Show fragment " + tag);
        mView.showFeedFragment(tag, feedUri);
    }

    private void showLoginDialog() {
        mView.showLoginDialog();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mSelectedTab);
    }

    public static MainPageController fromParcel(Parcel in) {
        String selectedTab = in.readString();
        Log.d(LOG_TAG, "From parcel: " + selectedTab);
        return new MainPageController(selectedTab);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<MainPageController> CREATOR
            = new Parcelable.Creator<MainPageController>() {
        public MainPageController createFromParcel(Parcel in) {
            return MainPageController.fromParcel(in);
        }

        public MainPageController[] newArray(int size) {
            return new MainPageController[size];
        }
    };

}
