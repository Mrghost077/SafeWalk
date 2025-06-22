package com.s23010162.safewalk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class RecordingService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Recording Service Started", Toast.LENGTH_LONG).show();

        // In a real app, you would initialize the camera and start recording here.
        // This is a placeholder for that logic.

        // Return START_STICKY to ensure the service is restarted if it's killed.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Recording Service Stopped", Toast.LENGTH_LONG).show();
        // Stop recording and release camera resources here.
    }
} 