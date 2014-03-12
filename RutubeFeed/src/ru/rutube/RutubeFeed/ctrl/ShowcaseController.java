package ru.rutube.RutubeFeed.ctrl;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.models.ShowcaseTab;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 12.03.14.
 */
public class ShowcaseController implements Parcelable {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = ShowcaseController.class.getName();
    private final Uri mShowcaseUri;
    private Context mContext;
    private RequestQueue mRequestQueue;
    private ShowcaseView mView;
    private boolean mAttached;
    private int mShowcaseId;

    public interface ShowcaseView {

    }

    public ShowcaseController(Uri showcaseUri, int showcaseId) {
        String showcaseSlug =  showcaseUri.getLastPathSegment();
        if (D) Log.d(LOG_TAG, "Showcase slug: " + showcaseSlug);
        mShowcaseUri = RutubeApp.formatApiUrl(R.string.showcase_api, showcaseSlug);
        if (D) Log.d(LOG_TAG, "Showcase uri path: " + mShowcaseUri.toString());
        mShowcaseId = showcaseId;
    }
    public ShowcaseController(Uri showcaseUri) {
        this(showcaseUri, 0);
    }

    public void attach(Context context, ShowcaseView view){
        assert mContext != null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mAttached = true;
        startRequests();
    }

    private void startRequests() {
        JsonObjectRequest request = ShowcaseTab.getShowcaseRequest(mShowcaseUri, mShowcaseId, null);
        mRequestQueue.add(request);
    }

    public void detach() {
        mRequestQueue.cancelAll(Requests.SHOWCASE);
        mRequestQueue.stop();
        mRequestQueue = null;
        mContext = null;
        mView = null;
        mAttached = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mShowcaseUri.toString());
        parcel.writeInt(mShowcaseId);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<ShowcaseController> CREATOR
            = new Parcelable.Creator<ShowcaseController>() {
        public ShowcaseController createFromParcel(Parcel in) {
            return ShowcaseController.fromParcel(in);
        }

        public ShowcaseController[] newArray(int size) {
            return new ShowcaseController[size];
        }
    };

    private static ShowcaseController fromParcel(Parcel in) {
        String url = in.readString();
        int showcaseId = in.readInt();
        return new ShowcaseController(Uri.parse(url), showcaseId);
    }
}
