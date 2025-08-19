package com.s23010162.safewalk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordingService extends Service {

    private static final String TAG = "RecordingService";
    private static final String CHANNEL_ID = "SafeWalkRecordingChannel";
    private static final int NOTIFICATION_ID = 1001;

    private ExecutorService serviceExecutor;
    private boolean isRecording = false;
    private File outputFile;
    private MediaRecorder mediaRecorder;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceExecutor = Executors.newSingleThreadExecutor();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Recording Service Started");

        // Start foreground service with notification immediately
        startForeground(NOTIFICATION_ID, createNotification("Emergency recording in progress..."));

        // Start emergency recording simulation in background
        serviceExecutor.execute(() -> {
            startEmergencyRecording();
        });

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SafeWalk Emergency Recording",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Emergency recording service notification");
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeWalk Emergency Recording")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_record)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();
    }

    private void startEmergencyRecording() {
        if (isRecording) {
            Log.w(TAG, "Emergency recording already in progress");
            return;
        }

        // Check permissions first
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Audio recording permission not granted");
            updateNotification("Emergency recording failed: Audio permission not granted");
            return;
        }

        try {
            isRecording = true;
            Log.d(TAG, "Emergency audio recording started");

            // Create emergency recording file
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "SAFEWALK_EMERGENCY_" + timeStamp + ".3gp";

            File mediaDir = new File(getExternalFilesDir(null), "SafeWalk");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }

            outputFile = new File(mediaDir, fileName);

            // Start audio recording using MediaRecorder
            startAudioRecording();

            // Update notification
            updateNotification("Emergency audio recording active: " + fileName);

            // Save to database
            saveRecordingToDatabase(outputFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Error starting emergency recording", e);
            updateNotification("Emergency recording failed: " + e.getMessage());
            isRecording = false;
        }
    }

    private void startAudioRecording() {
        try {
            if (outputFile != null) {
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                
                mediaRecorder.prepare();
                mediaRecorder.start();
                
                Log.d(TAG, "Audio recording started: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error starting audio recording", e);
            isRecording = false;
            updateNotification("Audio recording failed: " + e.getMessage());
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.release();
                } catch (Exception ex) {
                    Log.e(TAG, "Error releasing MediaRecorder", ex);
                }
                mediaRecorder = null;
            }
        }
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

    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            Log.d(TAG, "Emergency recording stopped");
            
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    Log.d(TAG, "MediaRecorder stopped and released");
                } catch (Exception e) {
                    Log.e(TAG, "Error stopping MediaRecorder", e);
                }
            }
            
            updateNotification("Emergency recording stopped");
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
        
        if (serviceExecutor != null) {
            serviceExecutor.shutdown();
        }
        
        Log.d(TAG, "Recording Service Stopped");
    }
} 