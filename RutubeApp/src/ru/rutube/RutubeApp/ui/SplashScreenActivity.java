package ru.rutube.RutubeApp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;

import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.SplashScreenController;

/**
 * Created by tumbler on 11.03.14.
 */
public class SplashScreenActivity extends ActionBarActivity implements SplashScreenController.SplashScreenView {
    NetworkImageView mBanner;
    SplashScreenController mController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
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
}
