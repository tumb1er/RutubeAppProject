package ru.rutube.RutubePlayer;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;

import ru.rutube.RutubePlayer.ctrl.PlayerController;
import ru.rutube.RutubePlayer.ui.PlayerActivity;
import ru.rutube.RutubePlayer.ui.PlayerFragment;

/**
 * Created by tumbler on 13.09.13.
 */

public class PlayerActivityTest extends ActivityInstrumentationTestCase2<PlayerActivity> {
    private final String LOG_TAG = PlayerActivityTest.class.getName();
    private final boolean D = BuildConfig.DEBUG;
    private PlayerActivity mActivity;
    private PlayerFragment mFragment;
    private Uri videoUri;
    private Uri thumbnailUri;
    private Intent intent;

    public PlayerActivityTest() {
        super(PlayerActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (D) Log.d(LOG_TAG, "setUp start");
        intent = new Intent();
        videoUri = Uri.parse("http://rutube.ru/video/ed1d49491260b778418d970122ef07c8/");
        thumbnailUri = Uri.parse("http://placehold.it/640x360.jpg");
        intent.setData(videoUri);
        setActivityIntent(intent);
        if (D) Log.d(LOG_TAG, "getActivity");
        mActivity = getActivity();
        if (D) Log.d(LOG_TAG, "setUp end");

    }

    /**
     * Проверяет, что PlayerFragment корректно обрабатывает процесс сохранения и загрузки состояния
     */
    public void testPlayerFragmentSaveLoad() {
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(R.id.player_fragment));
        assertNotNull(mFragment);
        Bundle state = new Bundle();
        mFragment.onSaveInstanceState(state);
        mFragment.onPause();
        mFragment.onActivityCreated(state);
    }

    /**
     * Проверяет, что PlayerController корректно обрабатывает процесс сохранения и загрузки состояния
     * @throws Throwable
     */
    public void testControllerSaveLoad() throws Throwable {
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(R.id.player_fragment));
        assertNotNull(mFragment);
        final PlayerController controller = new PlayerController(videoUri, thumbnailUri);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                controller.attach(mActivity, mFragment);
            }
        });
        Bundle b = new Bundle();
        b.putParcelable("KEY", controller);
        Object res  = b.getParcelable("KEY");
        assertNotNull(res);
        assertTrue(res.equals(controller));
    }

    /**
     * Проверяет корректность сохранения и восстановления состояния активити
     * @throws Throwable
     */
    @UiThreadTest
    public void testActivitySaveLoad() throws Throwable {
        mActivity.finish();
        mActivity = getActivity();
        Uri uri = mActivity.getIntent().getData();
        assertEquals(uri, videoUri);

    }

    /**
     * Проверяет восстановление контроллера из состояния ошибки
     */
    @UiThreadTest
    public void testChangeState() {
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(R.id.player_fragment));
        PlayerController ctrl = mFragment.getController();
        ctrl.onPlaybackError();
        assertEquals(PlayerController.STATE_ERROR, ctrl.getState());
        mFragment.getDialog().dismiss();
        Bundle state = new Bundle();
        mFragment.onSaveInstanceState(state);
        mFragment.onPause();
        mFragment.onActivityCreated(state);
        ctrl = mFragment.getController();
        assertEquals(PlayerController.STATE_STARTING, ctrl.getState());
    }

}
