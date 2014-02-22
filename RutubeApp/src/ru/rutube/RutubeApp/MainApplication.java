package ru.rutube.RutubeApp;

import android.app.Activity;
import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

import ru.rutube.RutubeAPI.RutubeApp;

/**
 * Created by oleg on 7/30/13.
 */
public class MainApplication extends RutubeApp {
    private static VideoCastManager mCastMgr;

    public static VideoCastManager getVideoCastManager(Context ctx) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(ctx, CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID, null, null);
            mCastMgr.enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
                    VideoCastManager.FEATURE_LOCKSCREEN |
                    VideoCastManager.FEATURE_DEBUGGING);
        }
        mCastMgr.setContext(ctx);
        return mCastMgr;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getVideoCastManager(getContext());
    }

    public static void mainActivityStart(Activity activity) {
        EasyTracker tracker = getTracker(activity);
        tracker.set(Fields.SCREEN_NAME, "Main screen");
        tracker.activityStart(activity);
    }

    public static void activityStop(Activity activity) {
        getTracker(activity).activityStop(activity);
    }

    protected static EasyTracker getTracker(Context context) {
        return EasyTracker.getInstance(context);
    }

    public static void searchActivityStart(Activity activity, String searchQuery) {
        EasyTracker tracker = getTracker(activity);
        tracker.set(Fields.SCREEN_NAME, "Search screen");
        tracker.activityStart(activity);
        tracker.send(MapBuilder
                .createEvent("stats", "open_search", searchQuery, null)
                .build());
    }

    public static void playerActivityStart(Activity activity, String videoUrl) {
        EasyTracker tracker = getTracker(activity);
        tracker.set(Fields.SCREEN_NAME, "Video page screen");
        tracker.activityStart(activity);
        tracker.send(MapBuilder
                .createEvent("stats", "open_player", videoUrl, null)
                .build());
    }

    public static void loginDialogStart(Activity activity) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createEvent("ui_actions", "login", "open", null).build());
    }

    public static void loginDialogSuccess(Activity activity) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createEvent("ui_actions", "login", "success", null).build());
    }

    public static void loginDialogFailed(Activity activity) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createEvent("ui_actions", "login", "failed", null).build());
    }


    public static void cardClick(Activity activity, String tag) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createEvent("ui_actions", "card_click", tag, null).build());
    }

    public static void cardClick(Activity activity) {
        cardClick(activity, "card");
    }

    public static void relatedCardClick(Activity activity, String tag) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createEvent("ui_actions", "related_click", tag, null).build());
    }

    public static void playerError(Activity activity, String message, String videoId) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createEvent("errors", message, videoId, null).build());
    }

    public static void playerOpened(Activity activity, String tag) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createEvent("conversion", "open_player", tag, null).build());
    }

    public static void feedActivityStart(Activity activity, String tag) {
        EasyTracker tracker = getTracker(activity);
        tracker.set(Fields.SCREEN_NAME, "Feed");
        tracker.activityStart(activity);
        tracker.send(MapBuilder
                .createEvent("stats", "open_feed", tag, null)
                .build());

    }

    public void reportError(Activity activity, String message) {
        EasyTracker tracker = getTracker(activity);
        tracker.send(MapBuilder.createException(message, false).build());
    }

}
