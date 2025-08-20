package com.s23010162.safewalk.utils;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.s23010162.safewalk.AlertActivity;
import com.s23010162.safewalk.PreferencesManager;
import com.s23010162.safewalk.R;

/**
 * Utility class for SOS dialog operations
 * Centralizes SOS countdown and PIN entry dialog logic
 */
public class SosDialogUtils {
    
    private static final long SOS_COUNTDOWN_DURATION = 5000; // 5 seconds
    private static final long COUNTDOWN_INTERVAL = 1000; // 1 second
    
    /**
     * Interface for SOS dialog callbacks
     */
    public interface SosDialogCallback {
        void onSosTriggered();
        void onSosCancelled();
    }
    
    /**
     * Show SOS countdown dialog
     */
    public static void showSosCountdown(Context context, SosDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_sos_countdown, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final TextView tvCountdown = dialogView.findViewById(R.id.tvCountdown);
        Button btnCancelSos = dialogView.findViewById(R.id.btnCancelSos);

        final long[] timeLeft = {SOS_COUNTDOWN_DURATION};
        final boolean[] isPinDialogOpen = {false};

        CountDownTimer sosCountDownTimer = new CountDownTimer(timeLeft[0], COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft[0] = millisUntilFinished;
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (!isPinDialogOpen[0]) {
                    dialog.dismiss();
                    callback.onSosTriggered();
                }
            }
        }.start();

        btnCancelSos.setOnClickListener(v -> {
            sosCountDownTimer.cancel();
            isPinDialogOpen[0] = true;
            showPinEntryDialog(context, dialog, timeLeft[0], callback);
        });
    }
    
    /**
     * Show PIN entry dialog
     */
    private static void showPinEntryDialog(Context context, AlertDialog sosDialog, long timeLeft, SosDialogCallback callback) {
        AlertDialog.Builder pinBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View pinView = inflater.inflate(R.layout.dialog_pin_entry, null);
        pinBuilder.setView(pinView);
        pinBuilder.setCancelable(false);
        final AlertDialog pinDialog = pinBuilder.create();
        pinDialog.show();

        EditText etPin = pinView.findViewById(R.id.etPin);
        Button btnSubmit = pinView.findViewById(R.id.btnSubmitPin);
        Button btnCancel = pinView.findViewById(R.id.btnCancelPin);

        PreferencesManager preferencesManager = new PreferencesManager(context);

        btnSubmit.setOnClickListener(v -> {
            String enteredPin = etPin.getText().toString().trim();
            if (enteredPin.isEmpty()) {
                Toast.makeText(context, "Please enter your PIN", Toast.LENGTH_SHORT).show();
                return;
            }
            if (enteredPin.length() != 4 || !enteredPin.matches("\\d{4}")) {
                Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                etPin.setText("");
                return;
            }
            if (preferencesManager.verifyEmergencyPin(enteredPin)) {
                pinDialog.dismiss();
                sosDialog.dismiss();
                callback.onSosCancelled();
                Toast.makeText(context, "SOS Canceled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                etPin.setText("");
            }
        });

        btnCancel.setOnClickListener(v -> {
            pinDialog.dismiss();
            // Resume countdown if time left
            if (timeLeft > 0) {
                showSosCountdownWithTimeLeft(context, sosDialog, timeLeft, callback);
            }
        });
    }
    
    /**
     * Show SOS countdown with remaining time
     */
    private static void showSosCountdownWithTimeLeft(Context context, AlertDialog oldDialog, long timeLeft, SosDialogCallback callback) {
        if (oldDialog != null && oldDialog.isShowing()) oldDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_sos_countdown, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();
        final TextView tvCountdown = dialogView.findViewById(R.id.tvCountdown);
        Button btnCancelSos = dialogView.findViewById(R.id.btnCancelSos);
        final long[] timeLeftArr = {timeLeft};
        final boolean[] isPinDialogOpen = {false};
        
        CountDownTimer sosCountDownTimer = new CountDownTimer(timeLeftArr[0], COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftArr[0] = millisUntilFinished;
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }
            @Override
            public void onFinish() {
                if (!isPinDialogOpen[0]) {
                    dialog.dismiss();
                    callback.onSosTriggered();
                }
            }
        }.start();
        
        btnCancelSos.setOnClickListener(v -> {
            sosCountDownTimer.cancel();
            isPinDialogOpen[0] = true;
            showPinEntryDialog(context, dialog, timeLeftArr[0], callback);
        });
    }
    
    /**
     * Create intent for AlertActivity
     */
    public static Intent createAlertActivityIntent(Context context, String alertType) {
        Intent intent = new Intent(context, AlertActivity.class);
        intent.putExtra(AlertActivity.EXTRA_ALERT_TYPE, alertType);
        return intent;
    }
}
