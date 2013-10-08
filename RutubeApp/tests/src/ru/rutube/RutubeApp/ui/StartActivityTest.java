package ru.rutube.RutubeApp.ui;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import ru.rutube.RutubeApp.ctrl.MainPageController;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class ru.rutube.RutubeApp.ui.StartActivityTest \
 * ru.rutube.RutubeApp.tests/android.test.InstrumentationTestRunner
 */
public class StartActivityTest extends ActivityInstrumentationTestCase2<StartActivity> {

    public StartActivityTest() {
        super(StartActivity.class);
    }

    /**
     * Проверяет корректность сохранения и восстановления состояния
     * @throws Throwable
     */
    @UiThreadTest
    public void testSaveLoadState() throws Throwable {
        StartActivity activity = getActivity();

        activity.finish();
        activity = getActivity();
        assertEquals(activity.getSupportActionBar().getSelectedTab().getTag(), MainPageController.TAB_EDITORS);
        activity.selectTab(MainPageController.TAB_MY_VIDEO);
        assertEquals(activity.getSupportActionBar().getSelectedTab().getTag(), MainPageController.TAB_MY_VIDEO);
        activity.finish();
        activity = getActivity();
        assertEquals(activity.getSupportActionBar().getSelectedTab().getTag(), MainPageController.TAB_MY_VIDEO);

    }
}
