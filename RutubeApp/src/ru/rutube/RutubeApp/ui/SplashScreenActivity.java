package ru.rutube.RutubeApp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.SplashScreenController;

/**
 * Created by tumbler on 11.03.14.
 */
public class SplashScreenActivity extends ActionBarActivity implements SplashScreenController.SplashScreenView {
    private static boolean D = BuildConfig.DEBUG;
    private static String LOG_TAG = SplashScreenActivity.class.getName();
    NetworkImageView mBanner;
    SplashScreenController mController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        mBanner = (NetworkImageView)findViewById(R.id.banner);
        mController = new SplashScreenController();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mController.attach(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mController.detach();
    }

    public void setBannerUrl(String url) {
        mBanner.setImageUrl(url, mController.getImageLoader());
    }

    @Override
    public void openShowCase(String url) {
        if (D) Log.d(LOG_TAG, "Open showcase: " + url);
        Intent i = new Intent(this, StartActivity.class); // Your list's Intent
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
        startActivity(i);
    }
}
