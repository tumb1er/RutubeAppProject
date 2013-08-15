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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.RutubeAPI;
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
        public void setThumbnailUri(Uri uri);

        public void showError();

        public void startPlayback();

        /**
         * Обрабатывает завершение показа видео
         */
        public void onComplete();

        /**
         * Получить текущее смещение видео
         * @return смещение от старта в миллисекундах
         */
        public int getCurrentOffset();

        public void stopPlayback();

        public void pauseVideo();

        public void seekTo(int millis);

        public void setLoading();
        public void setLoadingCompleted();
        public void toggleThumbnail(boolean visible);
    }

    public static final int STATE_NEW = 0;
    public static final int STATE_STARTING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_COMPLETED = 3;

    private static final String LOG_TAG = PlayerController.class.getName();

    protected RequestQueue mRequestQueue;
    protected ImageLoader mImageLoader;

    private Uri mVideoUri;
    private Video mVideo;
    private TrackInfo mTrackInfo;
    private int mState;
    private int mVideoOffset;

    private volatile int mPlayRequestStage;
    private boolean mAttached;
    private PlayerView mView;
    private Context mContext;
    private Uri mThumbnailUri;

    //
    // Реализация интерфейса RequestListener
    //

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

        if (tag == Requests.TRACK_INFO) {
            mTrackInfo = result.getParcelable(Constants.Result.TRACKINFO);
            assert mTrackInfo != null;
            assert mView != null;

            mView.setStreamUri(mTrackInfo.getBalancerUrl());
            mView.setVideoTitle(mTrackInfo.getTitle());

            mPlayRequestStage++;
        }

        if (tag == Requests.PLAY_OPTIONS) {
            Boolean allowed = result.getBoolean(Constants.Result.ACL_ALLOWED);
            if (mThumbnailUri == null){
                Uri thumbnailUri = result.getParcelable(Constants.Result.PLAY_THUMBNAIL);
                mView.setThumbnailUri(thumbnailUri);
            }
            if (!allowed) {
                Log.w(LOG_TAG, "Playback not allowed");
                mView.showError();
                return;
            }
            mPlayRequestStage++;
        }

        checkReadyToPlay();
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

    //
    // Конструкторы
    //

    public PlayerController(Uri videoUri, Uri thumbnailUri) {
        mContext = null;
        mView = null;
        mVideoUri = videoUri;
        mState = STATE_NEW;
        mThumbnailUri = thumbnailUri;
        mVideoOffset = 0;
    }

    protected PlayerController(Uri videoUri, Uri thumbnailUri, int state, int offset, TrackInfo trackInfo) {
        this(videoUri, thumbnailUri);
        mState = state;
        mVideoOffset = offset;
        mTrackInfo = trackInfo;
    }

    // Реализация Parcelable

    public static PlayerController fromParcel(Parcel in) {
        Uri feedUri = in.readParcelable(Uri.class.getClassLoader());
        TrackInfo trackInfo = in.readParcelable(TrackInfo.class.getClassLoader());
        Uri thumbnailUri = in.readParcelable(Uri.class.getClassLoader());
        int state = in.readInt();
        int videoOffset = in.readInt();
        return new PlayerController(feedUri, thumbnailUri, state, videoOffset, trackInfo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mVideoUri, i);
        parcel.writeParcelable(mTrackInfo.getBalancerUrl(), i);
        parcel.writeParcelable(mThumbnailUri, i);
        parcel.writeInt(mState);
        parcel.writeInt(mVideoOffset);
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

    //
    // Собственные публичные методы
    //

    /**
     * Начинает воспроизведение заново.
     *
     * Выключает показ тамнейла, инициализирует видеопоток, обновляет название ролика,
     * стартует воспроизведение
     */
    public void replay() {
        if (mState!= STATE_COMPLETED)
            throw new IllegalStateException(
                    String.format("Can't change state to Starting from %d", mState));
        setState(STATE_PLAYING);
        mVideoOffset = 0;
        mView.toggleThumbnail(false);
        mView.setStreamUri(mTrackInfo.getBalancerUrl());
        mView.setVideoTitle(mTrackInfo.getTitle());
        mView.startPlayback();
    }

    /**
     * Обработка события Fragment.onPause
     *
     * Запоминает текущую секунду видео, останавливает воспроизведение,
     * деинициализирует VideoView
     */
    public void onPause() {
        mVideoOffset = mView.getCurrentOffset();
        Log.d(LOG_TAG, "onPause: offset = " + String.valueOf(mVideoOffset));
        mView.stopPlayback();
        mView.setStreamUri(null);
    }

    /**
     * Обработка события Fragment.onResume
     *
     * Восстанавливает URL видеопотока, название ролика, текущую секунду вопроизведения,
     * запускает воспроизведение.
     */
    public void onResume() {
        if (mState == STATE_PLAYING){
            mView.setStreamUri(mTrackInfo.getBalancerUrl());
            mView.setVideoTitle(mTrackInfo.getTitle());
            mView.seekTo(mVideoOffset);
            mView.startPlayback();
        }
    }

    /**
     * Аксессор для загрузчика картинок
     *
     * @return загрузчик картинок, завязанный на локальную очередь запросов
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Обрабатывает событие окончания воспроизведения видео
     *
     * Включает тамнейл, вызывает у фрагмента обрабочтик onComplete
     */
    public void onCompletion() {
        if (mState!= STATE_PLAYING)
            throw new IllegalStateException(
                    String.format("Can't change state to Starting from %d", mState));
        setState(STATE_COMPLETED);
        mView.toggleThumbnail(true);
        mView.onComplete();
    }

    /**
     * Обработка события инициализации VideoView
     */
    public void onViewReady() {
        mPlayRequestStage++;
        checkReadyToPlay();
    }

    /**
     * Присоединяется к контексту и пользовательскому интерфейсу,
     * инициализирует объекты, зависящие от активити.
     * @param context экземпляр активити
     * @param view фрагмент или активити, реализующие пользовательский интерфейс
     */
    public void attach(Context context, PlayerView view) {
        assert mContext == null;
        assert mView == null;
        mContext = context;
        mView = view;
        mRequestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(HttpTransport.getHttpClient()));
        mImageLoader = new ImageLoader(mRequestQueue, RutubeAPI.getBitmapCache());
        if (mThumbnailUri != null) {
            mView.setThumbnailUri(mThumbnailUri);
        }
        mAttached = true;
        if (mState != STATE_NEW)
            restoreFromState();
    }

    /**
     * Отсоединяется от останавливаемой активити
     *
     * Останавливает очередь запросов, зануляет все ссылки на объекты Android.
     */
    public void detach() {
        mRequestQueue.cancelAll(Requests.TRACK_INFO);
        mRequestQueue.cancelAll(Requests.PLAY_OPTIONS);
        mRequestQueue.cancelAll(Requests.YAST_VIEWED);
        mRequestQueue.stop();
        mRequestQueue = null;
        mContext = null;
        mView = null;
        mAttached = false;
    }

    /**
     * Разбирает Uri видео, получает ID video и запускает цепочку запросов к API,
     * необходимых для начала проигрывания
     */
    public void requestStream() {
        if (!mAttached)
            throw new NullPointerException("Not attached");
        Log.d(LOG_TAG, "Got Uri: " + String.valueOf(mVideoUri));
        mVideo = null;
        if (mVideoUri != null) {
            parseVideoUri();
        }
        if (mVideo != null)
            startPlayRequests(mVideo);

    }

    /**
     * Осуществляет разбор ссылки на видео с целью получить ID видео
     * и подпись для приватного видео.
     */
    private void parseVideoUri() {
        final List<String> segments = mVideoUri.getPathSegments();
        assert segments != null;
        if (segments.size() == 2) {
            String videoId = segments.get(1);
            mVideo = new Video(videoId);
        } else if (segments.size() == 3) {
            String videoId = segments.get(2);
            String signature = mVideoUri.getQueryParameter("p");
            mVideo = new Video(videoId, signature);
        } else {
            throw new IllegalArgumentException("Incorrect Uri: " + String.valueOf(mVideoUri));
        }
    }

    /**
     * Восстанавливает пользовательский интерфейс плеера, в зависимости
     * от состояния контроллера на момент сохранения
     */
    private void restoreFromState() {
        Log.d(LOG_TAG, "Restoring from state " + String.valueOf(mState));
        switch(mState) {
            case STATE_STARTING:
                // на момент сохранения запросы еще не были обработаны, запускаем их заново
                mState = STATE_NEW;
                requestStream();
                break;
            case STATE_PLAYING:
                // На момент сохранения воспроизводилось видео, и есть корректные данные для того,
                // чтобы восстановить процесс просмотра.
                // Восстанавливаем название ролика, Uri видеопотока, состояние элементов управления
                // и текущую секунду воспроизведения.
                // Запускается показ видео без отправки статистики.
                mState = STATE_STARTING;
                mView.setVideoTitle(mTrackInfo.getTitle());
                mView.setStreamUri(mTrackInfo.getBalancerUrl());
                mView.setLoadingCompleted();
                mView.seekTo(mVideoOffset);
                startPlayback(false);
                break;
            case STATE_COMPLETED:
                // На момент сохранения был показан эндскрин.
                // Делаем так, чтобы плеер не начал в фоне воспроизводить видео, восстанавливаем
                // состояние элементов управления,
                mView.setStreamUri(null);
                mView.toggleThumbnail(true);
                mView.stopPlayback();
                mView.setLoadingCompleted();
                mView.onComplete();
                break;
            default:
                break;
        }
    }

    /**
     * Проверяет необходимые условия начала просмотра
     */
    private void checkReadyToPlay() {
        // Для начала воспроизведения необходимо дождаться завершения 2 запросов
        // и вызова onViewReady() - всего 3 стадии.
        if (mPlayRequestStage == 3) {
            startPlayback(true);
        } else
            Log.d(LOG_TAG, "Not ready yet");
    }

    /**
     * Обрабатывает процесс старта воспроизведения: создает запрос к yast.rutube.ru
     * и командует плееру начать просмотр
     */
    private void startPlayback(boolean sendViewed) {
        if (mState!= STATE_STARTING)
            throw new IllegalStateException(String.format("Can't change state to Starting from %d", mState));
        setState(STATE_PLAYING);
        mView.setLoadingCompleted();
        mView.toggleThumbnail(false);
        mView.startPlayback();
        if (sendViewed) {
            JsonObjectRequest request = mVideo.getYastRequest(mContext);
            mRequestQueue.add(request);
        }
    }

    /**
     * Обновляет логическое состояние контроллера с записью в лог
     * @param state STATE_NEW, STATE_STARTING, STATE_PLAYING, STATE_COMPLETED
     */
    private void setState(int state) {
        Log.d(LOG_TAG, String.format("Changing state: %d to %d", mState, state));
        mState = state;
    }

    /**
     * Выполняет цепочку запросов к API rutube необходимых для проигрывания видео.
     *
     * Рассчитывает на то, что на момент вызова метода событие MediaPlayer.onPrepared
     * не было возбуждено.
     * @param video объект видео, которое надо воспроизвести.
     */
    private void startPlayRequests(Video video) {
        if (mState != STATE_NEW)
            throw new IllegalStateException(
                    String.format("can't change state to STARTING from %d", mState));
        mPlayRequestStage = 0;
        JsonObjectRequest request = video.getTrackInfoRequest(mContext, this);
        mRequestQueue.add(request);
        request = video.getPlayOptionsRequest(mContext, this);
        mRequestQueue.add(request);
        setState(STATE_STARTING);

    }


}
