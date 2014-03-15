package ru.rutube.RutubeApp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.SplashScreenController;
import ru.rutube.RutubeFeed.ui.ShowcaseActivity;

/**
 * Created by tumbler on 11.03.14.
 */
public class SplashScreenActivity extends ActionBarActivity
        implements SplashScreenController.SplashScreenView {
    private static boolean D = BuildConfig.DEBUG;
    private static String LOG_TAG = SplashScreenActivity.class.getName();
    NetworkImageView mBanner;
    SplashScreenController mController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        try {
            actionBar.hide();
        } catch (NullPointerException ignored) {}
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

    @Override
    public void setBannerUrl(String url) {
        mBanner.setImageUrl(url, mController.getImageLoader());
    }

    @Override
    public void openShowCase(Uri url) {
        if (D) Log.d(LOG_TAG, "Open showcase: " + url.toString());
        Intent i = new Intent(this, StartActivity.class);
        i.setData(url);
        startActivity(i);
    }
}
