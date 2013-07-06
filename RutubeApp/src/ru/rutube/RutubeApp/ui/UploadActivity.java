package ru.rutube.RutubeApp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.foxykeep.datadroid.requestmanager.Request;
import ru.rutube.RutubeAPI.models.Auth;
import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeAPI.requests.RequestFactory;
import ru.rutube.RutubeAPI.requests.RutubeRequestManager;
import ru.rutube.RutubeApp.R;


/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 21.05.13
 * Time: 22:00
 * To change this template use File | Settings | File Templates.
 */
public class UploadActivity extends Activity {
    private static final String LOG_TAG = UploadActivity.class.getName();
    private static final int LOGIN_REQUEST_CODE = 1;
    private static final String UPLOAD_DONE = "upload_done";
    private static final String UPLOAD_STARTED = "upload_started";
    private RutubeRequestManager requestManager;
    private Auth auth;
    private String session_id = null;
    private String video_id = null;
    private String filename;
    private Context context;
    private boolean uploadDone = false;
    private boolean uploadStarted = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        auth = Auth.from(this);
        requestManager = RutubeRequestManager.from(this);
        setContentView(R.layout.upload);
        Button btn = (Button) findViewById(R.id.saveButton);
        if (savedInstanceState != null) {
            uploadDone = savedInstanceState.getBoolean(UPLOAD_DONE);
            uploadStarted = savedInstanceState.getBoolean(UPLOAD_STARTED);
        } else {
            IntentFilter filter = new IntentFilter(Constants.Actions.UPLOAD_PROGRESS);
            registerReceiver(broadcastReceiver, filter);
        }
        if (uploadStarted)
            btn.setEnabled(false);
        btn.setOnClickListener(onClickListener);
        processIntent();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(UPLOAD_DONE, uploadDone);
        outState.putBoolean(UPLOAD_STARTED, uploadStarted);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            unregisterReceiver(broadcastReceiver);
        }catch (IllegalArgumentException ignored) {}
    }

    private void processIntent() {
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        Intent intent = getIntent();
        Uri file_uri = intent.getData();
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            file_uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }
        iv.setImageBitmap(getThumbnail(file_uri));
        if (!auth.checkLoginState()) {
            Log.d(LOG_TAG, "Not authorised, starting login activity");
            startLoginActivity();
        } else {
            startUploadProcess();

        }
    }

    private void startUploadProcess() {
        if (uploadDone || uploadStarted) {
            Log.d(LOG_TAG, "Upload already done, don't start upload");
            return;
        }
        Log.d(LOG_TAG, "Begin uploading");
        uploadStarted = true;
        Request uploadSessionRequest = RequestFactory.getUploadSessionRequest();
        findViewById(R.id.saveButton).setEnabled(false);
        requestManager.execute(uploadSessionRequest, requestListener);
    }

    private Bitmap getThumbnail(Uri uri) {
        filename = getFilePath(uri);
        return ThumbnailUtils.createVideoThumbnail(filename, MediaStore.Video.Thumbnails.MINI_KIND);
    }

    private String getFilePath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void startLoginActivity() {
        Intent intent = new Intent("ru.rutube.api.login_required");
        startActivityForResult(intent, LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startUploadProcess();
            } else {
                Log.e(LOG_TAG, "Not authorized");
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(Constants.Result.PROGRESS, 0);
            Log.d(LOG_TAG, "(Activity) Progress: " + String.valueOf(progress));
            ((ProgressBar) findViewById(R.id.progress_bar)).setProgress(progress);

        }
    };

    protected View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.saveButton) {
                Log.d(LOG_TAG, "Save button clicked");
                saveVideo();
            }
        }
    };

    private void saveVideo() {
        String title = ((TextView) findViewById(R.id.titleEditText)).getText().toString();
        String description = "";
        boolean isHidden = !((ToggleButton) findViewById(R.id.hiddenToggleButton)).isChecked();
        int category_id = 13;
        Request uploadRequest = RequestFactory.getUpdateVideoRequest(video_id, title, description, isHidden, category_id);
        findViewById(R.id.saveButton).setEnabled(false);
        requestManager.execute(uploadRequest, requestListener);
    }

    protected RutubeRequestManager.RequestListener requestListener = new RutubeRequestManager.RequestListener() {

        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
            int rtype = request.getRequestType();
            if (rtype == RequestFactory.REQUEST_UPLOAD_SESSION) {
                session_id = resultData.getString(Constants.Params.UPLOAD_SESSION);
                video_id = resultData.getString(Constants.Params.VIDEO_ID);
                startFileUpload();
                return;
            }
            if (rtype == RequestFactory.REQUEST_UPDATE_VIDEO) {
                findViewById(R.id.saveButton).setEnabled(true);
                return;
            }
            if (rtype == RequestFactory.REQUEST_UPLOAD) {
                uploadDone = true;
                uploadStarted = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.
                        setTitle(getString(R.string.completed)).
                        setMessage(getString(R.string.upload_done)).
                        create().
                        show();
                findViewById(R.id.saveButton).setEnabled(true);
            }
        }

        void showError() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.
                    setTitle(android.R.string.dialog_alert_title).
                    setMessage(getString(ru.rutube.RutubeFeed.R.string.faled_to_load_data)).
                    create().
                    show();

        }

        @Override
        public void onRequestConnectionError(Request request, int statusCode) {
            int rtype = request.getRequestType();
            if (rtype == RequestFactory.REQUEST_UPDATE_VIDEO) {
                findViewById(R.id.saveButton).setEnabled(true);
            }
            if (rtype == RequestFactory.REQUEST_UPLOAD) {
                uploadStarted = false;
            }
            showError();
        }

        @Override
        public void onRequestDataError(Request request) {
            int rtype = request.getRequestType();
            if (rtype == RequestFactory.REQUEST_UPLOAD) {
                uploadStarted = false;
            }
            showError();
        }

        @Override
        public void onRequestCustomError(Request request, Bundle resultData) {
            int rtype = request.getRequestType();
            if (rtype == RequestFactory.REQUEST_UPLOAD) {
                uploadStarted = false;
            }
            showError();
        }
    };

    private void startFileUpload() {
        Log.d(LOG_TAG, "Starting file upload");
        Request request = RequestFactory.getUploadRequest(filename, session_id);
        requestManager.execute(request, requestListener);
    }
}