package ru.rutube.RutubeAPI.operations;

import android.util.Log;
import com.foxykeep.datadroid.network.NetworkConnection;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 01.06.13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class CSRFOperation {

    protected HashMap<String, String> setCSRF(NetworkConnection connection, HashMap<String, String> headers) {
        String csrftoken = UUID.randomUUID().toString().replace("-", "");
        String cookie = headers.get("Cookie");
        if (cookie == null)
            cookie = String.format("csrftoken=%s", csrftoken);
        else
            cookie += String.format("; csrftoken=%s", csrftoken);
        headers.put("Cookie", cookie);
        headers.put("X-CSRFToken", csrftoken);
        connection.setHeaderList(headers);
        return headers;
    }
}
