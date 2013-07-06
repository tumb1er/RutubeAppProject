package ru.rutube.RutubeAPI.requests;

import android.os.Bundle;

import com.android.volley.VolleyError;

/**
 * Created by tumbler on 16.06.13.
 */

public interface RequestListener {
    public class RequestError extends Exception {
        public RequestError(String message) {
            super(message);
        }
    }
    public void onResult(int tag, Bundle result);
    public void onVolleyError(VolleyError error);
    public void onRequestError(int tag, RequestError error);
}
