package ru.rutube.RutubePlayer;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;

import ru.rutube.RutubePlayer.ctrl.PlayerController;
import ru.rutube.RutubePlayer.ui.PlayerActivity;
import ru.rutube.RutubePlayer.ui.PlayerFragment;

/**
 * Created by tumbler on 13.09.13.
 */

public class PlayerActivityTest extends ActivityInstrumentationTestCase2<PlayerActivity> {
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
        intent = new Intent();
        videoUri = Uri.parse("http://rutube.ru/video/ed1d49491260b778418d970122ef07c8/");
        thumbnailUri = Uri.parse("http://placehold.it/640x360.jpg");
        intent.setData(videoUri);
        mActivity = getActivity();
    }

    /**
     * Проверяет, что PlayerFragment корректно обрабатывает процесс сохранения и загрузки состояния
     */
    public void testPlayerFragmentSaveLoad() {
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(R.id.player_fragment));
        assertNotNull(mFragment);
        Bundle state = new Bundle();
        mFragment.onSaveInstanceState(state);
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

    public void testActivitySaveLoad() throws Throwable {
        final Bundle state = new Bundle();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callActivityOnSaveInstanceState(mActivity, state);
                getInstrumentation().callActivityOnRestoreInstanceState(mActivity, state);

            }
        });
    }
}
