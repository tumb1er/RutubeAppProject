package ru.rutube.RutubePlayer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubePlayer.R;
import ru.rutube.RutubePlayer.ctrl.PlayerController;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class PlayerFragment extends Fragment
        implements PlayerController.PlayerView, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    private ProgressBar mLoadProgressBar;

    public void replay() {
        mController.replay();
    }

    /**
     * Интерфейс общения с активити, в которое встроен фрагмент с плеером
     */
    public interface PlayerStateListener {

        /**
         * Событие начала воспроизведения
         */
        public void onPlay();

        /**
         * Событие окончания воспроизведения
         */
        public void onComplete();

        /**
         * Событие невозможности воспроизведения
         */
        public void onFail();
    }
    private static final String CONTROLLER = "controller";
    private static final String LOG_TAG = PlayerFragment.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    protected PlayerController mController;
    protected VideoView mVideoView;
    protected Uri mStreamUri;
    protected PlayerStateListener mPlayerStateListener;
    protected MediaController mMediaController;
    protected DialogInterface.OnDismissListener mErrorListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            if (mPlayerStateListener != null)
                mPlayerStateListener.onFail();
        }
    };

    //
    // переопределенные методы из Fragment
    //

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) Log.d(LOG_TAG, "onActivityCreated");
        init(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mController.onResume();
        if (D) Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.d(LOG_TAG, "onPause");
        mController.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (D) Log.d(LOG_TAG, "Controller detached");
        mController.detach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (D) Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putParcelable(CONTROLLER, mController);
    }

    //
    // Обработчики событий MediaPlayer
    //

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mController.onCompletion();
        onComplete();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mController.onViewReady();
//        startPlayback();

    }

    //
    // Реализация интерфейса PlayerController.PlayerView
    //

    @Override
    public int getCurrentOffset() {
        return mVideoView.getCurrentPosition();
    }

    @Override
    public void stopPlayback() {
        mVideoView.setVideoURI(null);
        mVideoView.stopPlayback();
    }

    @Override
    public void seekTo(int millis) {
        mVideoView.seekTo(millis);
    }

    @Override
    public void pauseVideo() {
        mVideoView.pause();
    }

    @Override
    public void onComplete() {
        if (mPlayerStateListener != null) {
            if (D) Log.d(LOG_TAG, "onComplete");
            mPlayerStateListener.onComplete();
        }
        stopPlayback();
        toggleMediaController(false);
    }

    @Override
    public void setStreamUri(Uri uri) {
        if (D) Log.d(LOG_TAG, "setStreamUri " + String.valueOf(uri));
        setVideoUri(uri);
        mStreamUri = uri;
    }

    @Override
    public void showError(String error) {
        Activity activity = getActivity();
        if (activity == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog dialog = builder.
                setTitle(android.R.string.dialog_alert_title).
                setMessage(error).
                create();
        dialog.setOnDismissListener(mErrorListener);
        dialog.show();
    }

    @Override
    public void startPlayback() {
        if (D) Log.d(LOG_TAG, "StartPlayback");
        if (mPlayerStateListener != null)
            mPlayerStateListener.onPlay();
        startVideoPlayback();
    }
    @Override
    public void setLoading() {
        mLoadProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setLoadingCompleted() {
        mLoadProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void toggleThumbnail(boolean visible) {
        int visibility = (visible)? View.VISIBLE : View.INVISIBLE;
        View view = getView();
        assert view != null;
        View thumbnail = view.findViewById(R.id.thumbnail);
        thumbnail.setVisibility(visibility);
    }

    @Override
    public void setVideoTitle(String title) {

    }

    @Override
    public void setThumbnailUri(Uri uri) {
        View view = getView();
        assert view != null;
        NetworkImageView netImgView = (NetworkImageView) view.findViewById(R.id.thumbnail);
        ImageLoader imageLoader = mController.getImageLoader();
        if (imageLoader == null)
            throw new NullPointerException("no image loader");
        netImgView.setImageUrl(uri.toString(), imageLoader);
    }

    //
    // Собственные публичные методы
    //

    /**
     * Инициализирует обрабочтик событий PlayerStateListener
     * @param playerStateListener контейнер фрагмента, обрабатывающий события
     */
    public void setPlayerStateListener(PlayerStateListener playerStateListener) {
        mPlayerStateListener = playerStateListener;
    }

    /**
     * Меняет видимость элементов управления плеером
     * @param visible true если необходимо сделать элементы управления видимыми
     */
    protected void toggleMediaController(boolean visible) {
        if (visible)
            mMediaController.show();
        else
            mMediaController.hide();
    }

    /**
     * Инициализация видеоэлемента
     */
    protected void initVideoView() {
        View view = getView();
        assert view != null;
        mLoadProgressBar = (ProgressBar) view.findViewById(R.id.load);
        mVideoView = (VideoView) view.findViewById(R.id.video_view);
        mMediaController = new MediaController(getActivity());
        mVideoView.setMediaController(mMediaController);
        mVideoView.setPadding(10, 0, 0, 0);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);
    }

    /**
     * Задание Uri видеопотока для видеоэлемента
     * @param uri Uri видеопотока
     */
    protected void setVideoUri(Uri uri) {
        if (uri != null)
            mVideoView.setVideoURI(uri);
    }

    /**
     * Начинает воспроизведение видео
     */
    protected void startVideoPlayback() {
        if (D) Log.d(LOG_TAG, "startVideoPlayback");
        //mVideoiew.setVideoURI(mStreamUri);
        mVideoView.start();
    }

    /**
     * Инициализация логики плеера
     * @param savedInstanceState сохраненное состояние активити
     */
    private void init(Bundle savedInstanceState) {
        mStreamUri = null;
        Activity activity = getActivity();
        assert activity != null;
        Intent intent = activity.getIntent();
        Uri videoUri = intent.getData();
        Uri thumbnailUri = intent.getParcelableExtra(Constants.Params.THUMBNAIL_URI);
        initVideoView();
        mController = null;
        if (savedInstanceState != null) {
            mController = savedInstanceState.getParcelable(CONTROLLER);
        }
        if (mController == null) {
            mController = new PlayerController(videoUri, thumbnailUri);
        }
        mController.attach(activity, this);
        if (savedInstanceState == null)
            mController.requestStream();
    }
}
