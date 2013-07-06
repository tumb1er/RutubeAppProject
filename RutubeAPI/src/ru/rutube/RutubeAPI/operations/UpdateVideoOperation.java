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

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 25.05.13
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class UpdateVideoOperation extends CSRFOperation implements RequestService.Operation {
    private static final String LOG_TAG = UpdateVideoOperation.class.getName();

    protected static final String TITLE = "title";
    protected static final String IS_HIDDEN = "is_hidden";
    protected static final String CATEGORY = "category";
    protected static final String DESCRIPTION = "description";

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
        String title = request.getString(Constants.Params.TITLE);
        String description = request.getString(Constants.Params.DESCRIPTION);
        int category_id = request.getInt(Constants.Params.CATEGORY_ID);
        boolean is_hidden = request.getBoolean(Constants.Params.HIDDEN);
        String video_id = request.getString(Constants.Params.VIDEO_ID);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TITLE, title);
        params.put(DESCRIPTION, description);
        params.put(IS_HIDDEN, is_hidden?"checked":"");
        params.put(CATEGORY, String.valueOf(category_id));
        Uri loginUri = Uri.parse(context.getString(R.string.base_uri)).buildUpon()
                .appendPath(String.format(context.getString(R.string.video_uri), video_id))
                .build();
        NetworkConnection connection = new NetworkConnection(context, loginUri.toString());
        connection.setMethod(NetworkConnection.Method.PUT);
        connection.setParameters(params);
        Auth auth = Auth.from(context);
        Log.d(LOG_TAG, "Set token ");
        HashMap<String, String> headers = auth.setToken(connection);
        setCSRF(connection, headers);
        NetworkConnection.ConnectionResult result = connection.execute();
        try {
            JSONObject body = new JSONObject(result.body);
            Log.d(LOG_TAG, "Result: " + body.toString());
            return null;
        }catch(JSONException e) {
            throw new DataException(e.getMessage());
        }
    }
}
