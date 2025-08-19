package com.s23010162.safewalk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class ShakeDetectorService extends Service {

    private static final String TAG = "ShakeDetectorService";
    private static final long RATE_LIMIT_DURATION = 30000; // 30 seconds between alerts
    
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;
    private static boolean isAlertActive = false;
    private static long lastAlertTime = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();
        PreferencesManager preferencesManager = new PreferencesManager(this);

        shakeDetector.setOnShakeListener(count -> {
            if (preferencesManager.isShakeDetectionEnabled() && !isAlertActive && !isRateLimited()) {
                Log.d(TAG, "Shake detected, triggering alert");
                isAlertActive = true;
                lastAlertTime = System.currentTimeMillis();
                
                // Vibrate to give feedback
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);

                // Launch the AlertActivity with type = Shake Detected
                Intent intent = new Intent(this, AlertActivity.class);
                intent.putExtra(AlertActivity.EXTRA_ALERT_TYPE, "Shake Detected");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (isRateLimited()) {
                Log.d(TAG, "Alert rate limited, ignoring shake");
            }
        });
    }

    private boolean isRateLimited() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastAlertTime) < RATE_LIMIT_DURATION;
    }

    public static void setAlertInactive() {
        isAlertActive = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(shakeDetector);
    }
} 