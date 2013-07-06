package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.content.SharedPreferences;
// import com.foxykeep.datadroid.network.NetworkConnection;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 22.05.13
 * Time: 8:05
 * To change this template use File | Settings | File Templates.
 */
public class Auth {
    public static final String USER_DETAILS = "userdetails";
    public static final String USER_TOKEN = "token";
    public static final String AUTH_COOKIE = "cookie";

    private Context context;
    private static Auth instance;
    protected Auth(Context context) {
        this.context = context;
    }
    public synchronized static Auth from(Context context) {
        if (instance == null)
            instance = new Auth(context);
        return instance;
    }
    public boolean checkLoginState() {
        String token = getToken();
        return token != null;
    }

    private String getToken() {
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        return prefs.getString(USER_TOKEN, null);
    }
    private String getCookie(){
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        return prefs.getString(AUTH_COOKIE, null);
    }

    public void saveToken(String token) {
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_TOKEN, token);
        editor.commit();
    }
//    TODO: удалить вместе с DataDroid
//    public HashMap<String, String> setToken(NetworkConnection connection) {
//        String token = getToken();
//        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("Authorization", String.format("token %s", token));
//        String cookie = getCookie();
//        if (cookie!=null)
//            headers.put("Cookie", String.format("sessionid=%s", cookie));
//        connection.setHeaderList(headers);
//        return headers;
//    }

    public void saveCookie(String cookie) {
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AUTH_COOKIE, cookie);
        editor.commit();
    }
}
