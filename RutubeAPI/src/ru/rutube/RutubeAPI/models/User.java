package ru.rutube.RutubeAPI.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
public class User implements Parcelable {

    public static final String USER_DETAILS = "userdetails";
    protected static final String TOKEN = "token";
    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";
    private static final String LOG_TAG = User.class.getName();
    private static final boolean D = BuildConfig.DEBUG;
    private static final String USERID = "user_id";
    private static final String AVATAR_URL = "avatar_url";
    private static final String BACKGROUND_URL = "background_url";
    private static final String JSON_ID = "id";
    private static final String JSON_AVATAR = "avatar_url";
    private static final String NAME = "name";
    private static final String JSON_NAME = "name";
    private static final String JSON_APPEARANCE = "appearance";
    private static final String JSON_IMAGE = "image";

    private String mToken;
    private String mAvatarUrl;
    private String mBackgroundUrl;
    private String mName;
    private int mUserId;

    /**
     * Возвращает новый объект пользователя.
     * По возможности, инициализирует токен авторизации
     * @return новый объект пользователя
     */
    public static User fromContext() {
        Context context = RutubeApp.getInstance();
        SharedPreferences prefs = context
                .getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN, null);
        int userId = prefs.getInt(USERID, 0);
        String avatarUrl = prefs.getString(AVATAR_URL, null);
        String backgroundUrl = prefs.getString(BACKGROUND_URL, null);
        String name = prefs.getString(NAME, null);
        return new User(userId, name, token, avatarUrl, backgroundUrl);
    }

    public void parseVisitorResponse(JSONObject data) throws JSONException {
        mUserId = data.getInt(JSON_ID);
        mAvatarUrl = data.optString(JSON_AVATAR, null);
        mName = data.optString(JSON_NAME, null);
    }

    public void saveUser() {
        Context context = RutubeApp.getInstance();
        SharedPreferences prefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN, mToken);
        editor.putString(NAME, mName);
        editor.putString(AVATAR_URL, mAvatarUrl);
        editor.putString(BACKGROUND_URL, mBackgroundUrl);
        editor.putInt(USERID, mUserId);
        editor.commit();

    }

    /**
     * Конструирует запрос к API получения токена
     * @param email Емейл пользователя
     * @param password Пароль пользователя
     * @param requestListener обработчик запросов API Rutube
     * @return запрос Volley для получения токена
     */
    public JsonObjectRequest getTokenRequest(String email, String password,
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
    public JsonObjectRequest getVisitorRequest(RequestListener listener) {
        String visitorUri = RutubeApp.getUrl(R.string.visitor_uri);
        JsonObjectRequest request = new AuthJsonObjectRequest(visitorUri, null,
                getVisitorListener(listener), getErrorListener(Requests.VISITOR, listener), mToken);
        request.setShouldCache(false);
        request.setTag(Requests.VISITOR);
        return request;
    }

    private Response.Listener<JSONObject> getVisitorListener(final RequestListener listener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(D) Log.d(LOG_TAG, "Visitor: " + String.valueOf(response));
                try {
                    User.this.parseVisitorResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (listener != null) {
                    Bundle b = new Bundle();
                    b.putParcelable(Constants.Result.USER, User.this);
                    listener.onResult(Requests.VISITOR, b);
                }
            }
        };
    }

    private Response.Listener<JSONObject> getProfileListener(final RequestListener listener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(D) Log.d(LOG_TAG, "Profile: " + String.valueOf(response));
                try {
                    User.this.parseProfileResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (listener != null) {
                    Bundle b = new Bundle();
                    b.putParcelable(Constants.Result.USER, User.this);
                    listener.onResult(Requests.USER_PROFILE, b);
                }
            }
        };
    }

    private void parseProfileResponse(JSONObject response) throws JSONException {
        JSONObject appearance = response.getJSONObject(JSON_APPEARANCE);
        mBackgroundUrl = appearance.getString(JSON_IMAGE);
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
    protected User(int id, String name, String token, String avatarUrl, String backgroundUrl) {
        mName = name;
        mToken = token;
        mUserId = id;
        mAvatarUrl = avatarUrl;
        mBackgroundUrl = backgroundUrl;
    }

    /**
     * Реализует логику обработки ответа от API получения токена.
     * @param response JSON ответа
     * @param requestListener обработчик запросов API Rutube.
     */
    protected void processTokenResponse(JSONObject response, RequestListener requestListener) {
        try {
            mToken = parseToken(response);
            saveUser();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Result.TOKEN, mToken);
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
     * Конструирует
     * @return обработчик ошибок Volley
     */
    private Response.ErrorListener getErrorListener(final int requestId, final RequestListener listener)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (D) Log.d(LOG_TAG, "visitor error: " + String.valueOf(error));
                if (listener != null)
                    listener.onRequestError(requestId,
                            new RequestListener.RequestError(error.getMessage()));
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

    public void deleteToken() {
        mToken = null;
        saveUser();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public JsonObjectRequest getProfileRequest(RequestListener listener) {
        String profileUri = RutubeApp.formatApiUrl(R.string.user_profile_uri, mUserId).toString();
        JsonObjectRequest request = new AuthJsonObjectRequest(profileUri, null,
                getProfileListener(listener), getErrorListener(Requests.USER_PROFILE, listener), mToken);
        request.setShouldCache(false);
        request.setTag(Requests.VISITOR);
        return request;
    }

    public String getBackgroundUrl() {
        return mBackgroundUrl;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public String getName() {
        return mName;
    }
}
