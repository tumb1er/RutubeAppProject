package ru.rutube.RutubeApp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import ru.rutube.RutubeAPI.HttpTransport;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.User;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeApp.R;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 30.05.13
 * Time: 9:07
 * To change this template use File | Settings | File Templates.
 */
public class LoginFragment extends Fragment {

    private Button mLoginButton;

    public interface LoginListener {
        public void onLoginResult(int result);
    }
    private static final String LOG_TAG = LoginActivity.class.getName();
    private static final int RESULT_FAILED = Activity.RESULT_FIRST_USER + 1;
    private View mView;

    private RequestQueue mRequestQueue;
    private User mUser;

    protected View.OnClickListener loginButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Log.d(LOG_TAG, "login button clicked");
            view.setEnabled(false);
            String email = ((TextView)mView.findViewById(R.id.emailEditText)).getText().toString();
            String password = ((TextView)mView.findViewById(R.id.passwordEditText)).getText().toString();
            runLoginRequests(email, password);
        }
    };

    private void runLoginRequests(String email, String password) {
        mRequestQueue.add(mUser.getTokenRequest(email, password, getActivity(),
                userRequestListener));
    }

    void showError() {
        mLoginButton.setEnabled(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.
                setTitle(android.R.string.dialog_alert_title).
                setMessage(getString(ru.rutube.RutubeFeed.R.string.faled_to_load_data)).
                create().
                show();

    }

    protected RequestListener userRequestListener = new RequestListener() {

        @Override
        public void onResult(int tag, Bundle result) {
            String token = result.getString(Constants.Result.TOKEN);
            User.saveToken(getActivity(), token);
            ((LoginListener)getActivity()).onLoginResult(Activity.RESULT_OK);
        }

        @Override
        public void onVolleyError(VolleyError error) {
            showError();
        }

        @Override
        public void onRequestError(int tag, RequestError error) {
            showError();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mUser = new User(User.getToken(getActivity()));

        // TODO: remove after transfer to DRF-2.3
        mRequestQueue = Volley.newRequestQueue(getActivity(),
                new HttpClientStack(HttpTransport.getHttpClient()));
    }

    private void setAuthCookie(String auth_cookie) {
        // TODO: remove after transfer to DRF-2.3
        CookieStore cookieStore = HttpTransport.getHttpClient().getCookieStore();
        cookieStore.addCookie(new BasicClientCookie("sessionid", auth_cookie));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.login_fragment, container, false);
        mLoginButton = (Button)mView.findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(loginButtonListener);
        return mView;
    }

}
