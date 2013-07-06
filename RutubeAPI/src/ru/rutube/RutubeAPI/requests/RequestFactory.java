package ru.rutube.RutubeAPI.requests;

import android.net.Uri;
import android.util.Log;
import com.foxykeep.datadroid.requestmanager.Request;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.models.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:18
 * To change this template use File | Settings | File Templates.
 */
public final class RequestFactory {
    public static final int REQUEST_TRACKINFO = 1;
    public static final int REQUEST_EDITORS = 2;
    public static final int REQUEST_TOKEN = 3;
    public static final int REQUEST_UPLOAD = 4;
    public static final int REQUEST_UPLOAD_SESSION = 5;
    public static final int REQUEST_UPDATE_VIDEO = 6;

    public static Request getTrackInfoRequest(String video_id) {
        Request request = new Request(REQUEST_TRACKINFO);
        request.put(Constants.Params.VIDEO_ID, video_id);
        return request;
    }

    public static Request getFeedRequest(int page, Uri feedUri, Uri contentUri) {
        Log.d(RequestFactory.class.getName(), "GetFeedRequest: " + String.valueOf(feedUri) + "; " + String.valueOf(contentUri));
        Request request = new Request(REQUEST_EDITORS);
        request.put(Constants.Params.FEED_URI, feedUri);
        request.put(Constants.Params.CONTENT_URI, contentUri);
        request.put(Constants.Params.PAGE, page);
        return request;
    }

    private RequestFactory() {
    }

    public static Request getTokenRequest(String login, String password) {
        Request request = new Request(REQUEST_TOKEN);
        request.put(Constants.Params.EMAIL, login);
        request.put(Constants.Params.PASSWORD, password);
        return request;
    }

    public static Request getUploadRequest(String filename, String sid) {
        Request request = new Request(REQUEST_UPLOAD);
        request.put(Constants.Params.VIDEO_URI, filename);
        request.put(Constants.Params.UPLOAD_SESSION, sid);
        return request;
    }

    public static Request getUploadSessionRequest() {
        return new Request(REQUEST_UPLOAD_SESSION);
    }

    public static Request getUpdateVideoRequest(String videoId, String title, String description, boolean isHidden, int categoryId) {
        Request request = new Request(REQUEST_UPDATE_VIDEO);
        request.put(Constants.Params.VIDEO_ID, videoId);
        request.put(Constants.Params.TITLE, title);
        request.put(Constants.Params.DESCRIPTION, description);
        request.put(Constants.Params.HIDDEN, isHidden);
        request.put(Constants.Params.CATEGORY_ID, categoryId);
        return request;
    }
}
