package com.s23010162.safewalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;

public class AlertActivity extends AppCompatActivity {

    private static final int EMERGENCY_PERMISSIONS_REQUEST_CODE = 123;
    private TextView tvTimer;
    private EditText etPinEntry;
    private Button btnCancelAlert;
    private CountDownTimer countDownTimer;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        preferencesManager = new PreferencesManager(this);
        tvTimer = findViewById(R.id.tvTimer);
        etPinEntry = findViewById(R.id.etPinEntry);
        btnCancelAlert = findViewById(R.id.btnCancelAlert);

        btnCancelAlert.setOnClickListener(v -> cancelAlert());

        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.format(Locale.getDefault(), "%d", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                triggerEmergencyProtocol();
            }
        }.start();
    }

    private void cancelAlert() {
        String pin = etPinEntry.getText().toString();
        if (preferencesManager.verifyEmergencyPin(pin)) {
            countDownTimer.cancel();
            Toast.makeText(this, "Alert Canceled", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private void triggerEmergencyProtocol() {
        // First, check for necessary permissions
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.CAMERA}, EMERGENCY_PERMISSIONS_REQUEST_CODE);
        } else {
            // Permissions are already granted
            executeEmergencyActions();
        }
    }

    private void executeEmergencyActions() {
        // 1. Save alert to database
        AppDatabase db = AppDatabase.getDatabase(this);
        new Thread(() -> {
            db.alertDao().insert(new Alert(System.currentTimeMillis(), "Shake Detected", 0.0, 0.0));
            List<EmergencyContact> contacts = db.emergencyContactDao().getAllContactsBlocking();
            runOnUiThread(() -> {
                sendSmsToAll(contacts);
                startRecordingService();
                Toast.makeText(this, "EMERGENCY ALERT TRIGGERED!", Toast.LENGTH_LONG).show();
                finish(); // Close the alert screen
            });
        }).start();
    }

    private void sendSmsToAll(List<EmergencyContact> contacts) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = "Emergency Alert! This is a distress signal from a SafeWalk user. Last known location: [Location Data Here]";
        for (EmergencyContact contact : contacts) {
            smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null);
        }
        Toast.makeText(this, "SMS alerts sent to " + contacts.size() + " contacts.", Toast.LENGTH_SHORT).show();
    }

    private void startRecordingService() {
        Intent intent = new Intent(this, RecordingService.class);
        startService(intent);
        Toast.makeText(this, "Recording started.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EMERGENCY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with actions
                executeEmergencyActions();
            } else {
                Toast.makeText(this, "Permissions are required to send alerts and record video.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Reset the flag so a new alert can be triggered
        ShakeDetectorService.setAlertInactive();
    }
} 