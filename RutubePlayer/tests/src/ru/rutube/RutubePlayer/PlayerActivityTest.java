package ru.rutube.RutubePlayer;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;

import java.util.List;

import ru.rutube.RutubePlayer.ctrl.PlayerController;
import ru.rutube.RutubePlayer.ui.VideoPageActivity;
import ru.rutube.RutubePlayer.ui.PlayerFragment;

/**
 * Created by tumbler on 13.09.13.
 */

public class PlayerActivityTest extends ActivityInstrumentationTestCase2<VideoPageActivity> {
    private final String LOG_TAG = PlayerActivityTest.class.getName();
    private final boolean D = BuildConfig.DEBUG;
    private VideoPageActivity mActivity;
    private PlayerFragment mFragment;
    private Uri videoUri;
    private Uri thumbnailUri;
    private Intent intent;
    private PlayerController ctrl;

    public PlayerActivityTest() {
        super(VideoPageActivity.class);
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

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (ctrl != null)
            try {
                ctrl.detach();
            } catch (NullPointerException ignored) {}
    }

    /**
     * Проверяет, что PlayerFragment корректно обрабатывает процесс сохранения и загрузки состояния
     */
    @UiThreadTest
    public void testPlayerFragmentSaveLoad() {
        if (D)Log.d(LOG_TAG, "START");
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(
                R.id.player_fragment));
        assertNotNull(mFragment);
        Bundle state = new Bundle();
        if (D) Log.d(LOG_TAG, "onSaveInstanceState");
        mFragment.onSaveInstanceState(state);
        if (D) Log.d(LOG_TAG, "1onPause");
        mFragment.onPause();
        if (D) Log.d(LOG_TAG, "onActivityCreated");
        mFragment.onActivityCreated(state);
        if (D) Log.d(LOG_TAG, "getController");
        ctrl = mFragment.getController();
    }

    /**
     * Проверяет, что PlayerController корректно обрабатывает процесс сохранения и загрузки состояния
     * @throws Throwable
     */
    @UiThreadTest
    public void testControllerSaveLoad() throws Throwable {
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(
                R.id.player_fragment));
        assertNotNull(mFragment);
        ctrl = new PlayerController(videoUri, thumbnailUri);
        ctrl.attach(mActivity, mFragment);
        Bundle b = new Bundle();
        b.putParcelable("KEY", ctrl);
        Object res  = b.getParcelable("KEY");
        assertNotNull(res);
        assertTrue(res.equals(ctrl));
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
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(
                R.id.player_fragment));
        ctrl = mFragment.getController();
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

    /**
     * Проверяет корректность определения плеера по URL.
     */
    public void testIntentFilters() {
        Instrumentation.ActivityMonitor m = getInstrumentation().addMonitor(
                VideoPageActivity.class.getName(), null, true);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        String[] playerUrls = new String[] {
                "http://rutube.ru/video/embed/6550500/",
                "http://rutube.ru/video/private/c667beb7752cf85deaed08e4fd3e9372/?p=bBuEb_zFgMBx75k5OaSm_A",
                "http://rutube.ru/video/411228a1ff96f0a3c296f5c437cb4907/"
        };
        PackageManager packageManager = getActivity().getPackageManager();
        assert packageManager != null;
        for (String playerUrl : playerUrls) {
            intent.setData(Uri.parse(playerUrl));
            List<ResolveInfo> resolution = packageManager.queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY);
            boolean rutubeFound = false;
            for (ResolveInfo value : resolution) {
                rutubeFound = rutubeFound || String.valueOf(value.activityInfo).contains(
                        VideoPageActivity.class.getName());
            }
            assertTrue(playerUrl, rutubeFound);
        }

        String[] otherUrls = new String[] {
                "http://rutube.ru/api/video/editors/",
                "http://rutube.ru/video/person/",
                "http://rutube.ru/video/person/350/",
        };
        for (String otherUrl : otherUrls) {
            intent.setData(Uri.parse(otherUrl));
            List<ResolveInfo> resolution = packageManager.queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY);
            boolean rutubeFound = false;
            for (ResolveInfo value : resolution) {
                rutubeFound = rutubeFound || String.valueOf(value.activityInfo).contains(
                        VideoPageActivity.class.getName());
                Log.d(LOG_TAG, String.valueOf(value));
            }
            assertFalse(otherUrl, rutubeFound);
        }
    }
}
