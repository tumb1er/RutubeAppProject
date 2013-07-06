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
import org.json.JSONException;
import org.json.JSONObject;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.models.Auth;
import ru.rutube.RutubeAPI.models.Constants;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 22.05.13
 * Time: 7:49
 * To change this template use File | Settings | File Templates.
 */
public class GetUploadSessionOperation extends CSRFOperation implements RequestService.Operation {
    private static final String LOG_TAG = GetFeedOperation.class.getName();
    public static final String JSON_SID = "sid";
    public static final String JSON_VIDEO_ID = "video";

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
        Log.d(LOG_TAG, "getting upload session");
        Uri uploadSessionUri = Uri.parse(context.getString(R.string.base_uri)).buildUpon()
                .appendEncodedPath(context.getString(R.string.upload_session_uri))
                .build();
        Log.d(LOG_TAG, "Uri: " + uploadSessionUri.toString());
        NetworkConnection connection = new NetworkConnection(context, uploadSessionUri.toString());
        connection.setMethod(NetworkConnection.Method.POST);
        Auth auth = Auth.from(context);
        Log.d(LOG_TAG, "Set token ");
        HashMap<String, String> headers = auth.setToken(connection);
        setCSRF(connection, headers);
        Log.d(LOG_TAG, "Executing");
        NetworkConnection.ConnectionResult result = connection.execute();
        try {
            Log.d(LOG_TAG, String.valueOf(result));
            JSONObject body = new JSONObject(result.body);
            Log.d(LOG_TAG, "Result: " + body.toString());
            String sid = body.getString(JSON_SID);
            String video_id = body.getString(JSON_VIDEO_ID);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Params.UPLOAD_SESSION, sid);
            bundle.putString(Constants.Params.VIDEO_ID, video_id);
            return bundle;
        } catch (JSONException e) {
            throw new DataException(e.getMessage());
        }
    }

}
