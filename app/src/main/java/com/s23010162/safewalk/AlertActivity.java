package com.s23010162.safewalk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AlertActivity extends AppCompatActivity {

    private static final int EMERGENCY_PERMISSIONS_REQUEST_CODE = 123;
    private TextView tvTimer;
    private Button btnCancelAlert, btnRecordingStatus;
    private CheckBox cbContactsAlerted, cbLocationShared, cbRecordingStarted;
    private CountDownTimer countDownTimer;
    private PreferencesManager preferencesManager;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        preferencesManager = new PreferencesManager(this);
        tvTimer = findViewById(R.id.tvTimer);
        btnCancelAlert = findViewById(R.id.btnCancelAlert);
        btnRecordingStatus = findViewById(R.id.btnRecordingStatus);
        cbContactsAlerted = findViewById(R.id.cbContactsAlerted);
        cbLocationShared = findViewById(R.id.cbLocationShared);
        cbRecordingStarted = findViewById(R.id.cbRecordingStarted);
        startTime = System.currentTimeMillis();

        // Simulate actions being taken
        simulateEmergencyActions();

        btnCancelAlert.setOnClickListener(v -> {
            showPinDialog();
        });
    }

    private void showPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pin_entry, null);
        builder.setView(dialogView);
        builder.setTitle("Enter PIN to Cancel Alert");
        builder.setCancelable(false);

        EditText etPin = dialogView.findViewById(R.id.etPin);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitPin);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPin);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSubmit.setOnClickListener(v -> {
            String enteredPin = etPin.getText().toString().trim();
            
            // Validate PIN input
            if (enteredPin.isEmpty()) {
                Toast.makeText(this, "Please enter your PIN", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (enteredPin.length() != 4 || !enteredPin.matches("\\d{4}")) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                etPin.setText("");
                return;
            }
            
            try {
                if (preferencesManager.verifyEmergencyPin(enteredPin)) {
                    dialog.dismiss();
                    stopTimer();
                    finish();
                    Toast.makeText(this, "Alert Canceled Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    etPin.setText("");
                }
            } catch (Exception e) {
                Log.e("AlertActivity", "Error verifying PIN", e);
                Toast.makeText(this, "Error verifying PIN. Please try again.", Toast.LENGTH_SHORT).show();
                etPin.setText("");
            }
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void simulateEmergencyActions() {
        // Start recording timer
        startRecordingTimer();
        cbRecordingStarted.setChecked(true);

        // Simulate sending SMS and sharing location after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            cbContactsAlerted.setChecked(true);
            cbLocationShared.setChecked(true);
        }, 1500); // 1.5 second delay
    }

    private void startRecordingTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
                btnRecordingStatus.setText(String.format(Locale.getDefault(), "RECORDING: %02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
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
        stopTimer();
        // Reset the flag so a new alert can be triggered
        ShakeDetectorService.setAlertInactive();
    }
} 