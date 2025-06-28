package com.s23010162.safewalk;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.view.WindowManager;
import android.view.Surface;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class RecordingService extends Service {

    private static final String TAG = "RecordingService";
    private static final String CHANNEL_ID = "SafeWalkRecordingChannel";
    private static final int NOTIFICATION_ID = 1001;

    @SuppressLint("RestrictedApi")
    private VideoCapture videoCapture;
    private ExecutorService cameraExecutor;
    private boolean isRecording = false;
    private File outputFile;

    @Override
    public void onCreate() {
        super.onCreate();
        cameraExecutor = Executors.newSingleThreadExecutor();
        createNotificationChannel();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Recording Service Started");

        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification("Recording in progress..."));

        // Start recording
        startRecording();

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SafeWalk Recording",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows recording status");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeWalk")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_record)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @SuppressLint("RestrictedApi")
    private void startRecording() {
        if (isRecording) {
            Log.w(TAG, "Recording already in progress");
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Get rotation safely for a Service
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                int rotation = Surface.ROTATION_0;
                if (wm != null && wm.getDefaultDisplay() != null) {
                    rotation = wm.getDefaultDisplay().getRotation();
                }
                // Create video capture use case
                videoCapture = new VideoCapture.Builder()
                        .setTargetRotation(rotation)
                        .build();

                // Bind use cases to camera using application lifecycle
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        ProcessLifecycleOwner.get(), cameraSelector, videoCapture);

                // Start recording
                startVideoRecording();

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    private void startVideoRecording() {
        if (videoCapture == null) {
            Log.e(TAG, "VideoCapture is null");
            return;
        }

        // Create output file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "SAFEWALK_" + timeStamp + ".mp4";

        File mediaDir = new File(getExternalFilesDir(null), "SafeWalk");
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }

        outputFile = new File(mediaDir, fileName);

        @SuppressLint("RestrictedApi") VideoCapture.OutputFileOptions fileOptions =
                new VideoCapture.OutputFileOptions.Builder(outputFile).build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        videoCapture.startRecording(
                fileOptions,
                ContextCompat.getMainExecutor(this),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(VideoCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "Video saved: " + outputFile.getAbsolutePath());
                        isRecording = true;

                        // Save to database
                        saveRecordingToDatabase(outputFile.getAbsolutePath());

                        // Update notification
                        updateNotification("Recording saved: " + fileName);
                    }

                    @Override
                    public void onError(int videoCaptureError, String message, Throwable cause) {
                        Log.e(TAG, "Video recording error: " + message, cause);
                        isRecording = false;
                        updateNotification("Recording failed: " + message);
                    }
                }
        );
    }

    private void saveRecordingToDatabase(String filePath) {
        Recording recording = new Recording();
        recording.filePath = filePath;
        recording.timestamp = System.currentTimeMillis();
        
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getDatabase(this).recordingDao().insert(recording);
            Log.d(TAG, "Recording saved to database: " + filePath);
        });
    }

    private void updateNotification(String content) {
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification(content));
    }

    @SuppressLint("RestrictedApi")
    private void stopRecording() {
        if (videoCapture != null && isRecording) {
            videoCapture.stopRecording();
            isRecording = false;
            Log.d(TAG, "Recording stopped");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        
        Log.d(TAG, "Recording Service Stopped");
    }
} 