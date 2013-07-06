package ru.rutube.RutubeAPI.operations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;
import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;
import ru.rutube.RutubeAPI.R;
import ru.rutube.RutubeAPI.models.Constants;

import java.io.*;
import java.net.SocketException;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 21.05.13
 * Time: 23:03
 * To change this template use File | Settings | File Templates.
 */
public class UploadOperation implements RequestService.Operation {
    private static final String LOG_TAG = UploadOperation.class.getName();
    private long bytesTotal = 0;
    private long bytesSent = 0;
    private long lastProgressBytes = 0;
    private Context context;

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
        this.context = context;
        String filename = request.getString(Constants.Params.VIDEO_URI);
        Uri.Builder builder = Uri.parse(context.getString(R.string.uploader_uri)).buildUpon();
        builder.appendEncodedPath(String.format(context.getString(R.string.upload_uri),
                request.getString(Constants.Params.UPLOAD_SESSION)));
        Uri uri = builder.build();
        Log.d(LOG_TAG, "Uploading file " + filename + " to " + uri.toString());
        try {
            HttpResponse response = uploadFile(uri, filename);
            Log.d(LOG_TAG, "Response: " + parseResponse(response));
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionException(e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new DataException((e.toString()));
        }
        return null;
    }
    class CountingOutputStream extends FilterOutputStream {

        CountingOutputStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            countByte();
        }
    }

    private void countByte() {
        bytesSent += 1;
        if (bytesSent - lastProgressBytes > 1000000) {
            int progress = (int)((float)bytesSent/(float)bytesTotal* 100);
            sendProgress(progress);
            lastProgressBytes = bytesSent;
        }
    }

    private void sendProgress(int progress) {
        Intent intent = new Intent(Constants.Actions.UPLOAD_PROGRESS);
        intent.putExtra(Constants.Result.PROGRESS, progress);
        context.sendBroadcast(intent);
    }

    protected HttpResponse uploadFile(Uri uploadUri, String filename) throws IOException {
        HttpPost httpPost = new HttpPost(uploadUri.toString());
        MultipartEntity entity = new MultipartEntity(){
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                super.writeTo(new CountingOutputStream(outstream));
            }
        };
        File file = new File(filename);
        bytesTotal = file.length();
        entity.addPart("data", new FileBody(file));
        httpPost.setEntity(entity);
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
        try {
            return httpClient.execute(httpPost);
        } finally {
            httpClient.close();
        }
    }
    protected JSONObject parseResponse(HttpResponse response) throws IOException, JSONException, NullPointerException {
        HttpEntity entity = response.getEntity();
        Log.d(getClass().getName(), response.getStatusLine().toString());
        if (entity == null) {
            return null;
        }
        InputStream is = entity.getContent();
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        try{
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }   catch (SocketException e) {
            if (!e.getMessage().equals("Socket is closed"))
                throw new IOException(e.getMessage());
        }
        return new JSONObject(builder.toString());
    }
}
