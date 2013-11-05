package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import ru.rutube.RutubeAPI.BuildConfig;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.RutubeApp;
import ru.rutube.RutubeAPI.requests.AuthJsonObjectRequest;
import ru.rutube.RutubeAPI.requests.RequestListener;
import ru.rutube.RutubeAPI.requests.Requests;

/**
 * Created by tumbler on 16.06.13.
 */


/**
 * Класс пользователя Rutube
 */
public class User {

    public static final String USER_DETAILS = "userdetails";
    protected static final String TOKEN = "token";
    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";
    private static final String LOG_TAG = User.class.getName();
    private static final boolean D = BuildConfig.DEBUG;

    private String mToken = null;

    /**
     * Возвращает новый объект пользователя.
     * По возможности, инициализирует токен авторизации
     * @param context контекст для доступа к файлу настроек
     * @return новый объект пользователя
     */
    public static User load(Context context) {
        String token = loadToken(context);
        return new User(token);
    }

    /**
     * Загружает токен из файла настроек приложения
     * @param context контекст для доступа к файлу настроек
     * @return токен авторизации
     */
    public static String loadToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN, null);
    }

    /**
     * Сохраняет данные авторизации в файле настроек приложения
     * @param context контекст для доступа к файлу настроек
     * @param token токен авторизации
     */
    public static void saveToken(Context context, String token) {

        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN, token);
        editor.commit();
    }

    /**
     * Конструирует запрос к API получения токена
     * @param email Емейл пользователя
     * @param password Пароль пользователя
     * @param context контекст для доступа к ресурсам
     * @param requestListener обработчик запросов API Rutube
     * @return запрос Volley для получения токена
     */
    public JsonObjectRequest getTokenRequest(String email, String password, Context context,
                                             RequestListener requestListener) {
        String loginUri = RutubeApp.getUrl(R.string.token_uri);
        JSONObject requestData = getTokenRequestData(email, password);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, loginUri,
                requestData, getTokenListener(requestListener),
                getErrorListener(requestListener));
        request.setShouldCache(true);
        request.setTag(Requests.TOKEN);
        return request;
    }

    /**
     * Конструирует запрос к API Visitor для отслеживания пользовательской активности на сайте.
     * @return запрос Volley для получения данных о пользователе
     */
    public JsonObjectRequest getVisitorRequest() {
        String visitorUri = RutubeApp.getUrl(R.string.visitor_uri);
        JsonObjectRequest request = new AuthJsonObjectRequest(visitorUri, null,
                getVisitorListener(), null, mToken);
        request.setShouldCache(false);
        request.setTag(Requests.VISITOR);
        return request;
    }

    private Response.Listener<JSONObject> getVisitorListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        };
    }

    /**
     * Возвращает токен авторизации
     * @return токен авторизации или null, если токен не задан.
     */
    public String getToken() {
        return mToken;
    }

    /**
     * Проверяет, авторизован ли пользователь на Rutube
     * @return false, если пользователь авторизован
     */
    public boolean isAnonymous() {
        return (mToken == null);
    }

    /**
     * Создает новый объект пользователя
     * @param token - токен авторизации
     */
    protected User(String token) {
        mToken = token;
    }

    /**
     * Реализует логику обработки ответа от API получения токена.
     * @param response JSON ответа
     * @param requestListener обработчик запросов API Rutube.
     */
    protected void processTokenResponse(JSONObject response, RequestListener requestListener) {
        try {
            String token = parseToken(response);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Result.TOKEN, token);
            requestListener.onResult(Requests.TOKEN, bundle);
        } catch (JSONException e) {
            RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
            requestListener.onRequestError(Requests.TOKEN, error);
        }
    }

    /**
     * Разбирает JSON ответа API получения токена
     * @param data JSON ответа
     * @return токен авторизации
     * @throws JSONException
     */
    protected String parseToken(JSONObject data) throws JSONException {
        if (D) Log.d(LOG_TAG, "Result: " + data.toString());
        return data.getString(TOKEN);
    }

    /**
     * Конструирует прокси для обработки ответа от API получения токена.
     * @param requestListener обработчик запросов API Rutube
     * @return обработчик Volley
     */
    private Response.Listener<JSONObject> getTokenListener(final RequestListener requestListener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processTokenResponse(response, requestListener);
            }

        };
    }

    /**
     * Конструирует прокси для обработки ошибок Volley
     * @param requestListener обработчик запросов API Rutube.
     * @return обработчик ошибок Volley
     */
    private Response.ErrorListener getErrorListener(final RequestListener requestListener)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onVolleyError(error);
            }
        };
    }

    /**
     * Конструирует тело запроса к API получения токена
     * @param email
     * @param password
     * @return JSON с телом запроса
     */
    private JSONObject getTokenRequestData(String email, String password) {
        JSONObject requestData = new JSONObject();
        try {
            requestData.put(USERNAME, email);
            requestData.put(PASSWORD, password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestData;
    }

    public void deleteToken(Context context) {
        mToken = null;
        User.saveToken(context, null);
    }
}
