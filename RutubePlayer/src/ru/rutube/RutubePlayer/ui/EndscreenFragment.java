package ru.rutube.RutubePlayer.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubePlayer.R;

/**
 * Created by tumbler on 30.07.13.
 */
public class EndscreenFragment extends Fragment {

    public interface ReplayListener {
        public void replay();
    }

    private final String LOG_TAG = getClass().getName();
    private static final boolean D = BuildConfig.DEBUG;

    private ReplayListener mReplayListener;
    private RutubeMediaController.ShareListener mShareListener;

    protected View.OnClickListener mReplayBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mReplayListener != null)
                mReplayListener.replay();
        }
    };

    protected View.OnClickListener mShareBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mShareListener != null)
                mShareListener.onShare();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(LOG_TAG, "OnCreateView");
        View view = inflater.inflate(R.layout.endscreen_fragment, container, false);
        assert view != null;
        Button btn = (Button)view.findViewById(R.id.replay_btn);
        btn.setOnClickListener(mReplayBtnListener);
        btn = (Button)view.findViewById(R.id.share_btn);
        btn.setOnClickListener(mShareBtnListener);
        return view;
    }

    public void setReplayListener(ReplayListener listener) {
        mReplayListener = listener;
    }

    public void setShareListener(RutubeMediaController.ShareListener listener) {
        mShareListener = listener;
    }




}
