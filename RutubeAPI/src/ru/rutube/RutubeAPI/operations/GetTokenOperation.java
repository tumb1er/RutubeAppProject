package ru.rutube.RutubeAPI.operations;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.models.Constants;

import java.net.CookieManager;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 20.05.13
 * Time: 22:32
 * To change this template use File | Settings | File Templates.
 */
public class GetTokenOperation implements RequestService.Operation {
    private static final String LOG_TAG = GetTokenOperation.class.getName();

    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";
    protected static final String TOKEN = "token";
    private static final String EMAIL = "email";

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
        String email = request.getString(Constants.Params.EMAIL);
        String password = request.getString(Constants.Params.PASSWORD);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(USERNAME, email);
        params.put(PASSWORD, password);
        Uri baseUri = Uri.parse(context.getString(R.string.base_uri));
        Uri loginUri = baseUri.buildUpon()
                .appendPath(context.getString(R.string.token_uri))
                .build();
        NetworkConnection connection = new NetworkConnection(context, loginUri.toString());
        connection.setMethod(NetworkConnection.Method.POST);
        connection.setParameters(params);
        NetworkConnection.ConnectionResult result = connection.execute();
        Bundle bundle = new Bundle();
        try {
            JSONObject body = new JSONObject(result.body);
            Log.d(LOG_TAG, "Result: " + body.toString());
            String token = body.getString(TOKEN);
            bundle.putString(Constants.Result.TOKEN, token);
        }catch(JSONException e) {
            throw new DataException(e.getMessage());
        }
        loginUri = baseUri.buildUpon()
                .appendPath(context.getString(R.string.login_uri))
                .build();
        connection = new NetworkConnection(context, loginUri.toString());
        connection.setMethod(NetworkConnection.Method.POST);
        params.remove(USERNAME);
        params.put(EMAIL, email);
        connection.setParameters(params);
        result = connection.execute();
        String cookie = extractAuthCookie(loginUri, result);
        if (cookie != null)
            bundle.putString(Constants.Result.AUTH_COOKIE, cookie);
        return bundle;

    }

    private String extractAuthCookie(Uri loginUri, NetworkConnection.ConnectionResult result) {
        List<String> cookieValues = result.headerMap.get("Set-Cookie");
        CookieSpec spec = new BrowserCompatSpec();
        CookieOrigin cookieOrigin = new CookieOrigin(loginUri.getHost(), 80, "/", false);
        for (String cookie: cookieValues) {
            try {
                Header setCookie = new BasicHeader("Set-Cookie", cookie);
                List<Cookie> cookies = spec.parse(setCookie, cookieOrigin);
                for (Cookie c: cookies) {
                    Log.d(LOG_TAG, c.toString());
                    if (c.getName().equals("sessionid"))
                        return c.getValue();
                }
            } catch (MalformedCookieException ignored) {}
        }
        return null;
    }
}
