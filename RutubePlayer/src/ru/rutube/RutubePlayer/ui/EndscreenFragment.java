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

import ru.rutube.RutubePlayer.R;

/**
 * Created by tumbler on 30.07.13.
 */
public class EndscreenFragment extends Fragment {

    public interface ReplayListener {
        public void replay();
    }

    private final String LOG_TAG = getClass().getName();

    private ReplayListener mReplayListener;


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
            share();
        }
    };

    private void share() {
        Activity activity = getActivity();
        assert activity != null;
        Uri videoUri = activity.getIntent().getData();
        assert videoUri != null;
        Log.d(LOG_TAG, "Sharing: " + String.valueOf(videoUri));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, videoUri.toString());
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.share_text));
        startActivity(Intent.createChooser(intent, activity.getString(R.string.share)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "OnCreateView");
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




}
