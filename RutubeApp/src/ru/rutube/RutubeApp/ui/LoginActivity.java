package ru.rutube.RutubeApp.ui;

import android.app.Activity;
import android.os.Bundle;
import ru.rutube.RutubeApp.R;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 20.05.13
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class LoginActivity extends Activity implements LoginFragment.LoginListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    @Override
    public void onLoginResult(int result) {
        setResult(result);
        if (result == RESULT_OK)
            finish();
    }
}