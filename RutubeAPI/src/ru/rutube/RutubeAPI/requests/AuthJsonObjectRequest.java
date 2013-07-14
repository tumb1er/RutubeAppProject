package ru.rutube.RutubeAPI.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tumbler on 08.07.13.
 */
public class AuthJsonObjectRequest extends JsonObjectRequest {
    private String mToken;

    public AuthJsonObjectRequest(int method, String url, JSONObject jsonRequest,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener, String token) {
        super(method, url, jsonRequest, listener, errorListener);
        mToken = token;
    }

    public AuthJsonObjectRequest(String url, JSONObject jsonRequest,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener, String token) {
        super(url, jsonRequest, listener, errorListener);
        mToken = token;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>();
        if (mToken != null)
            headers.put("Authorization", String.format("Token %s", mToken));
        return headers;
    }
}
