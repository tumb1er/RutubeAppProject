package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeAPI;
import ru.rutube.RutubeAPI.requests.RequestListener;

/**
 * Created by tumbler on 16.06.13.
 */
public class User {
    public static final String USER_DETAILS = "userdetails";
    public static final int TOKEN_RESULT = 1;
    public static final int LOGIN_RESULT = 2;
    protected static final String TOKEN = "token";
    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";
    private static final String LOG_TAG = User.class.getName();
    private static final String EMAIL = "email";
    private static final String AUTH_COOKIE = "cookie";
    private String mToken;


    public User(String token) {
        mToken = token;
    }
    public User() {
        this(null);
    }

    Response.Listener<JSONObject> getTokenListener(final RequestListener requestListener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String token = parseToken(response);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.Result.TOKEN, token);
                    requestListener.onResult(TOKEN_RESULT, bundle);
                } catch (JSONException e) {
                    RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                    requestListener.onRequestError(TOKEN_RESULT, error);
                }
            }
        };
    }
    Response.Listener<JSONObject> getLoginListener(final RequestListener requestListener)
    {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG_TAG, "onLoginResponse");
                Bundle bundle = new Bundle();
                requestListener.onResult(LOGIN_RESULT, bundle);
            }
        };
    }
    Response.ErrorListener getErrorListener(final RequestListener requestListener)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onVolleyError(error);
            }
        };
    }

//    public void saveCookie(String cookie) {
//        SharedPreferences prefs = mContext.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(AUTH_COOKIE, cookie);
//        editor.commit();
//    }

    protected String parseToken(JSONObject data) throws JSONException {
        Log.d(LOG_TAG, "Result: " + data.toString());
        return data.getString(TOKEN);
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN, token);
        editor.commit();
    }

    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN, null);
    }

//    public String getAuthCookie() {
//        SharedPreferences prefs = mContext.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
//        return prefs.getString(AUTH_COOKIE, null);
//    }

    public String getToken() {
        return mToken;
    }

    public JsonObjectRequest getTokenRequest(String email, String password, Context context,
                                             RequestListener requestListener) {
        String loginUri = RutubeAPI.getUrl(context, R.string.token_uri);
        JSONObject requestData = new JSONObject();
        try {
            requestData.put(USERNAME, email);
            requestData.put(PASSWORD, password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, loginUri,
                requestData, getTokenListener(requestListener),
                getErrorListener(requestListener));
        request.setShouldCache(true);
        return request;
    }

//    public JsonObjectRequest getLoginRequest(String email, String password) {
//        String loginUri = RutubeAPI.getUrl(mContext, R.string.login_uri);
//        JSONObject requestData = new JSONObject();
//        try {
//            requestData.put(EMAIL, email);
//            requestData.put(PASSWORD, password);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, loginUri,
//                requestData, loginListener, vollerErrorListener);
//        request.setShouldCache(false);
//        return request;
//    }

    public boolean isAuthenticated() {
       return (mToken != null);
    }
}
