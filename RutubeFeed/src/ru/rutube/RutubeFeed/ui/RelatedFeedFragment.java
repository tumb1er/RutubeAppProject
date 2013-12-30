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
import ru.rutube.RutubeAPI.models.Video;
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

/**
 * Фрагмент для ленты похожих.
 * Принимает из getArguments параметр String VIDEO_ID, загружает через API ленту похожих.
 * ID видео при отсутствии в аргументах может получаться из Uri, сохраненного в
 * getIntent().getData().
 */
public class RelatedFeedFragment extends FeedFragment {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String LOG_TAG = RelatedFeedFragment.class.getName();
    @Override
    protected void initFeedUri() {
        Bundle args = getArguments();
        String videoId = null;
        if (args != null) {
            videoId = args.getString(Constants.Params.VIDEO_ID);
            if (D) Log.d(LOG_TAG, "VideoID from args: " + String.valueOf(videoId));
        }
        if (videoId == null) {
            Activity activity = getActivity();
            assert activity != null;
            Uri uri = activity.getIntent().getData();
            assert uri != null;
            videoId = uri.getLastPathSegment();
            if (D) Log.d(LOG_TAG, "VideoID last segment: " + String.valueOf(videoId));
        }
        if (videoId != null) {
            if (videoId.length() != 32) {
                try {
                    int trackId = Integer.parseInt(videoId);
                    Video video = new Video(trackId, null);
                    videoId = video.getVideoId();
                } catch (NumberFormatException ignored) {
                    videoId = null;
                }
            }
            if (videoId != null)
                setFeedUri(Uri.parse(RutubeApp.getUrl(R.string.related_video_uri)).buildUpon()
                        .appendEncodedPath(videoId).build());
            else
                setFeedUri(Uri.parse(RutubeApp.getUrl(R.string.editors_uri)));
        } else
            setFeedUri(Uri.parse(RutubeApp.getUrl(R.string.editors_uri)));
    }

    @Override
    public void showError() {}
}
