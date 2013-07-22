package ru.rutube.RutubePlayer.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import ru.rutube.RutubePlayer.R;

public class PlayerActivity extends FragmentActivity {

    private FragmentPlayer fragmentPlayer;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        fragmentPlayer = new FragmentPlayer();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content, fragmentPlayer);
        transaction.commit();
    }
}
