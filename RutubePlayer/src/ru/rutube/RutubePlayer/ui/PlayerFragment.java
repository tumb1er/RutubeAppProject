package ru.rutube.RutubePlayer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

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

    private MediaController mMediaController;

    public void replay() {
        mController.replay();
    }

    @Override
    public int getCurrentOffset() {
        return mVideoView.getCurrentPosition();
    }

    @Override
    public void stopPlayback() {
        mVideoView.setVideoURI(null);
        mVideoView.stopPlayback();
    }

    public void seekTo(int millis) {
        mVideoView.seekTo(millis);
    }

    public interface PlayerStateListener {
        public void onPlay();
        public void onComplete();
    }

    private static final String CONTROLLER = "controller";

    protected PlayerController mController;
    protected VideoView mVideoView;
    protected Uri mStreamUri;
    protected PlayerStateListener mPlayerStateListener;


    private final String LOG_TAG = getClass().getName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
        init(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        mController.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mController.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void pauseVideo() {
        mVideoView.pause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "Controller detached");
        mController.detach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putParcelable(CONTROLLER, mController);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mController.onCompletion();
        onComplete();
    }

    @Override
    public void onComplete() {
        if (mPlayerStateListener != null) {
            Log.d(LOG_TAG, "onComplete");
            mPlayerStateListener.onComplete();
        }
        stopPlayback();
        toggleMediaController(false);
    }

    protected void toggleMediaController(boolean visible) {
        if (visible)
            mMediaController.show();
        else
            mMediaController.hide();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mController.onViewReady();
        startPlayback();

    }

    /**
     * Сохраняет Uri видеопотока
     * @param uri ссылка на видеопоток (например, http://.../name.m3u8)
     */
    @Override
    public void setStreamUri(Uri uri) {
        Log.d(LOG_TAG, "setStreamUri " + String.valueOf(uri));
        setVideoUri(uri);
        mStreamUri = uri;
    }

    @Override
    public void showError() {
        Activity activity = getActivity();
        if (activity == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.
                setTitle(android.R.string.dialog_alert_title).
                setMessage(getString(R.string.faled_to_load_data)).
                create().
                show();
    }

    /**
     * Проверяет возможность и стартует воспроизведение видео.
     *
     * Возможность воспроизвести видео зависит от двух факторов:
     * 1. Проинициализирован видеоэлемент (mVideoViewInited)
     * 2. Получен Uri видеопотока (mStreamUri)
     * Если оба условия выполнены, начинается собственно воспроизведение (startVideoPlayback)
     */
    @Override
    public void startPlayback() {
        Log.d(LOG_TAG, "StartPlayback");
        if (mPlayerStateListener != null)
            mPlayerStateListener.onPlay();
        startVideoPlayback();
    }

    @Override
    public void setLoading() {

    }

    @Override
    public void setLoadingCompleted() {

    }

    public void toggleThumbnail(boolean visible) {
        int visibility = (visible)? View.VISIBLE : View.INVISIBLE;
        View view = getView();
        assert view != null;
        View thumbnail = view.findViewById(R.id.thumbnail);
        thumbnail.setVisibility(visibility);
    }

    public void setPlayerStateListener(PlayerStateListener playerStateListener) {
        mPlayerStateListener = playerStateListener;
    }

    /**
     * Инициализация видеоэлемента
     */
    protected void initVideoView() {
        View view = getView();
        assert view != null;
        mVideoView = (VideoView) view.findViewById(R.id.video_view);
        mMediaController = new MediaController(getActivity());
        mVideoView.setMediaController(mMediaController);
        mVideoView.setPadding(10, 0, 0, 0);
        mVideoView.setOnCompletionListener(this);
    }

    /**
     * Задание Uri видеопотока для видеоэлемента
     * @param uri Uri видеопотока
     */
    protected void setVideoUri(Uri uri) {
        mVideoView.setVideoURI(uri);
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

    /**
     * Начинает воспроизведение видео
     */
    protected void startVideoPlayback() {
        Log.d(LOG_TAG, "stopPlayback");
        mVideoView.setVideoURI(mStreamUri);
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
