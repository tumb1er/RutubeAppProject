package ru.rutube.RutubePlayer.ctrl;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.TrackInfo;
import ru.rutube.RutubeAPI.models.Video;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 27.07.13.
 */
public class PlayerController implements Parcelable, RequestListener {

    /**
     * Интерфейс для представления плеера
     */
    public interface PlayerView{
        /**
         * Задает Uri видеопотока видеоэлементу
         * @param uri Uri видеопотока
         */
        public void setStreamUri(Uri uri);

        public void setVideoTitle(String title);
        /**
         * Отображает сообщение об ошибке
         */
        public void showError();

        /**
         * Начинает воспроизведение видео
         */
        public void startPlayback();

    }

    public static int STATE_NEW = 0;
    public static int STATE_READY = 1;
    public static int STATE_PLAYING = 2;
    public static int STATE_COMPLETED = 3;

    private static final String LOG_TAG = PlayerController.class.getName();
    private Uri mVideoUri;
    private Video mVideo;
    private int mState;

    private RequestQueue mRequestQueue;
    private volatile int mPlayRequestStage;
    private boolean mAttached;
    private PlayerView mView;
    private Context mContext;

    /**
     * Обработка результатов запросов к API.
     * Ждет выполнения запросов TRACK_INFO и PLAY_OPTIONS, после завершения обоих запросов
     * начинает проигрывание видео.
     * @param tag тег запроса
     * @param result данные
     *
     */
    @Override
    public void onResult(int tag, Bundle result) {
        Log.d(LOG_TAG, "Received result for " + String.valueOf(tag));
        if (tag == Requests.TRACK_INFO) {
            TrackInfo trackInfo = result.getParcelable(Constants.Result.TRACKINFO);
            assert trackInfo != null;
            assert mView != null;

            mView.setStreamUri(trackInfo.getBalancerUrl());
            mView.setVideoTitle(trackInfo.getTitle());

            mPlayRequestStage++;
        }
        if (tag == Requests.PLAY_OPTIONS) {
            Boolean allowed = result.getBoolean(Constants.Result.ACL_ALLOWED);
            if (!allowed) {
                Log.w(LOG_TAG, "Playback not allowed");
                mView.showError();
                return;
            }
            mPlayRequestStage++;
        }
        if (mPlayRequestStage == 2) {
            Log.d(LOG_TAG, "OK, playing");
            mState = STATE_READY;
            startPlayback();
        } else
            Log.d(LOG_TAG, "Not ready yet");

    }

    @Override
    public void onVolleyError(VolleyError error) {
        Log.e(LOG_TAG, error.toString());
        mView.showError();

    }

    @Override
    public void onRequestError(int tag, RequestError error) {
        Log.e(LOG_TAG, error.toString());
        mView.showError();
    }

    public PlayerController(Uri videoUri) {
        mContext = null;
        mView = null;
        mVideoUri = videoUri;
        mState = STATE_NEW;
    }

    protected PlayerController(Uri videoUri, int state) {
        this(videoUri);
        mState = state;
    }

    /**
     * Обрабатывает событие окончания воспроизведения видео
     */
    public void onCompletion() {
        mState = STATE_COMPLETED;
    }

    /**
     * Присоединяется к контексту и пользовательскому интерфейсу,
     * инициализирует объекты, зависящие от активити.
     * @param context экземпляр активити
     * @param view пользовательский интерфейс
     */
    public void attach(Context context, PlayerView view) {
        assert mContext == null;
        assert mView == null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mAttached = true;
    }

    /**
     * Отсоединяется от останавливаемой активити
     */
    public  void detach() {
        mContext = null;
        mView = null;
        mRequestQueue.cancelAll(Requests.FEED_PAGE);
        mRequestQueue = null;
        mAttached = false;
    }

    // Реализация Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mVideoUri, i);
        parcel.writeInt(mState);
    }

    public static PlayerController fromParcel(Parcel in) {
        Uri feedUri = in.readParcelable(Uri.class.getClassLoader());
        int state = in.readInt();
        return new PlayerController(feedUri, state);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<PlayerController> CREATOR
            = new Parcelable.Creator<PlayerController>() {
        public PlayerController createFromParcel(Parcel in) {
            return PlayerController.fromParcel(in);
        }

        public PlayerController[] newArray(int size) {
            return new PlayerController[size];
        }
    };

    /**
     * Разбирает Uri видео, получает ID video и запускает цепочку запросов к API,
     * необходимых для начала проигрывания
     */
    public void requestStream() {
        assert mAttached;
        Log.d(LOG_TAG, "Got Uri: " + String.valueOf(mVideoUri));
        if (mVideoUri != null) {
            final List<String> segments = mVideoUri.getPathSegments();
            Log.d(LOG_TAG, "Segments " + String.valueOf(segments));
            assert segments != null;
            if (segments.size() == 2) {
                String videoId = segments.get(1);
                mVideo = new Video(videoId);
                startPlayRequests(mVideo);
            } else if (segments.size() == 3) {
                String videoId = segments.get(2);
                String signature = mVideoUri.getQueryParameter("p");
                mVideo = new Video(videoId, signature);
                startPlayRequests(mVideo);
            } else {
                Log.d(LOG_TAG, "Incorrect Uri");
            }
        }

    }

    /**
     * Обрабатывает процесс старта воспроизведения: создает запрос к yast.rutube.ru
     * и командует плееру начать просмотр
     */
    private void startPlayback() {
        mState = STATE_PLAYING;
        mView.startPlayback();
        JsonObjectRequest request = mVideo.getYastRequest(mContext);
        mRequestQueue.add(request);
    }

    /**
     * Выполняет цепочку запросов к API rutube необходимых для проигрывания видео.
     * @param video объект видео, которое надо воспроизвести.
     */
    private void startPlayRequests(Video video) {
        if (mState != STATE_NEW) {
            Log.d(LOG_TAG, String.format("Can't start play requests in state %d", mState));
            return;
        }
        mPlayRequestStage = 0;
        JsonObjectRequest request = video.getTrackInfoRequest(mContext, this);
        mRequestQueue.add(request);
        request = video.getPlayOptionsRequest(mContext, this);
        mRequestQueue.add(request);
    }


}
