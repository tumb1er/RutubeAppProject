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
import org.apache.http.conn.scheme.Scheme;
import org.json.JSONException;
import org.json.JSONObject;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.models.Trackinfo;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:01
 * To change this template use File | Settings | File Templates.
 */
public class TrackinfoOperation implements RequestService.Operation {
    private static final String LOG_TAG = TrackinfoOperation.class.getName();

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
        Log.d(LOG_TAG, "Started operation");
        String video_id = request.getString(Constants.Params.VIDEO_ID);
        Uri.Builder builder = Uri.parse(context.getString(R.string.base_uri)).buildUpon();
        builder.appendEncodedPath(String.format(context.getString(R.string.trackinfo_uri), video_id));
        Uri uri = builder.build();
        Log.d(LOG_TAG, "Fetching Uri " + uri.toString());
        NetworkConnection connection = new NetworkConnection(context, uri.toString());
        NetworkConnection.ConnectionResult result = connection.execute();
        try {
            Log.d(LOG_TAG, "Received result");
            JSONObject data = new JSONObject(result.body);
            Trackinfo trackinfo = Trackinfo.fromJSON(data);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.Result.TRACKINFO, trackinfo);
            return bundle;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new DataException(e.getMessage());

        }
    }
}
