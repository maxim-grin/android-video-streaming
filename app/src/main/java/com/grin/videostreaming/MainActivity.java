package com.grin.videostreaming;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity implements RtspClient.Callback, Session.Callback, SurfaceHolder.Callback {

    // log tag
    public final static String TAG = MainActivity.class.getSimpleName();

    // surface view
    private static SurfaceView surfaceView;

    // rtsp session
    private Session session;
    private static RtspClient rtspClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surface);
        surfaceView.getHolder().addCallback(this);

        // initialize RTSP client
        initRtspClient();
    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleStreaming();
    }

    @Override
    protected void onPause() {
        super.onPause();

        toggleStreaming();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        rtspClient.release();
        session.release();
        surfaceView.getHolder().removeCallback(this);
    }

    private void initRtspClient() {
        // session builder configuration
        session = SessionBuilder.getInstance()
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(8000, 16000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setSurfaceView(surfaceView).setPreviewOrientation(0)
                .setCallback(this)
                .build();

        // RTSP client configuration
        rtspClient = new RtspClient();
        rtspClient.setSession(session);
        rtspClient.setCallback(this);
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

        String ip, port, path;

        // parsing URI written in Editext
        Pattern uri = Pattern.compile("rtsp://(.+):(\\d)/(.+)");
        Matcher matcher = uri.matcher(AppConfig.STREAM_URL);
        matcher.find();

        ip = matcher.group(1);
        port = matcher.group(2);
        path = matcher.group(3);

        rtspClient.setCredentials(AppConfig.PUBLISHER_USERNAME, AppConfig.PUBLISHER_PASSWORD);
        rtspClient.setServerAddress(ip, Integer.parseInt(port));
        rtspClient.setStreamPath("/" + path);
    }

    private void toggleStreaming() {
        if (!rtspClient.isStreaming()) {
            // start camera preview
            session.startPreview();

            // start video stream
            rtspClient.startStream();
        } else {
// stop camera preview
            session.stopPreview();

            // stop video stream
            rtspClient.stopStream();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBitrateUpdate(long l) {

    }

    @Override
    public void onPreviewStarted() {

    }

    @Override
    public void onSessionConfigured() {

    }

    @Override
    public void onSessionStarted() {

    }

    @Override
    public void onSessionStopped() {

    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        switch (reason) {
            case Session.ERROR_CAMERA_ALREADY_IN_USE:
                break;
            case Session.ERROR_CAMERA_HAS_NO_FLASH:
                break;
            case Session.ERROR_INVALID_SURFACE:
                break;
            case Session.ERROR_STORAGE_NOT_READY:
                break;
            case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
                break;
            case Session.ERROR_OTHER:
                break;
        }

        if (e != null) {
            alertError(e.getMessage());
            e.printStackTrace();
        }
    }

    private void alertError(final String message) {
        final String error = (message == null) ? "Unknown error" : message;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(error).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onRtspUpdate(int message, Exception e) {
        switch (message) {
            case RtspClient.ERROR_CONNECTION_FAILED:
            case RtspClient.ERROR_WRONG_CREDENTIALS:
                alertError(e.getMessage());
                e.printStackTrace();
        }
    }
}
