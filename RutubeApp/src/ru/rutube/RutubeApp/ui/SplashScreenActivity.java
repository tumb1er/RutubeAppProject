package ru.rutube.RutubeApp.ui;

import android.app.Activity;
import android.os.Bundle;

import com.android.volley.toolbox.NetworkImageView;

import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.SplashScreenController;

/**
 * Created by tumbler on 11.03.14.
 */
public class SplashScreenActivity extends Activity {
    NetworkImageView mBanner;
    SplashScreenController mController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mBanner = (NetworkImageView)findViewById(R.id.banner);
    }

    public void setBannerUrl(String url) {
        mBanner.setImageUrl(url, mController.getImageLoader());
    }
}
