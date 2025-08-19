package com.s23010162.safewalk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import android.widget.FrameLayout;

public class AlertActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int EMERGENCY_PERMISSIONS_REQUEST_CODE = 123;
    private static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 456;
    private static final String EMERGENCY_SERVICES_NUMBER = "tel:"; // Placeholder for emergency services
    private TextView tvTimer;
    private Button btnCancelAlert, btnRecordingStatus, btnCallEmergency, btnHideAlert;
    private CheckBox cbContactsAlerted, cbLocationShared, cbRecordingStarted;
    private CountDownTimer countDownTimer;
    private PreferencesManager preferencesManager;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long startTime;
    private ImageView imgAppLogo, imgRecordingDot, imgContactsAlerted, imgLocationShared, imgRecordingStarted;
    private TextView tvEmergencyStatus, tvReassure, tvRecordingStatus;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        preferencesManager = new PreferencesManager(this);
        imgAppLogo = findViewById(R.id.imgAppLogo);
        imgRecordingDot = findViewById(R.id.imgRecordingDot);
        imgContactsAlerted = findViewById(R.id.imgContactsAlerted);
        imgLocationShared = findViewById(R.id.imgLocationShared);
        imgRecordingStarted = findViewById(R.id.imgRecordingStarted);
        tvEmergencyStatus = findViewById(R.id.tvEmergencyStatus);
        tvReassure = findViewById(R.id.tvReassure);
        tvRecordingStatus = findViewById(R.id.tvRecordingStatus);
        btnCallEmergency = findViewById(R.id.btnCallEmergency);
        btnHideAlert = findViewById(R.id.btnHideAlert);
        tvTimer = findViewById(R.id.tvTimer);
        btnCancelAlert = findViewById(R.id.btnCancelAlert);
        btnRecordingStatus = findViewById(R.id.btnRecordingStatus);
        cbContactsAlerted = findViewById(R.id.cbContactsAlerted);
        cbLocationShared = findViewById(R.id.cbLocationShared);
        cbRecordingStarted = findViewById(R.id.cbRecordingStarted);
        startTime = System.currentTimeMillis();

        // Animate the recording dot
        animateRecordingDot();

        // Simulate actions being taken
        simulateEmergencyActions();

        btnCancelAlert.setOnClickListener(v -> {
            showPinDialog();
        });

        btnCallEmergency.setOnClickListener(v -> {
            showCallEmergencyDialog();
        });

        btnHideAlert.setOnClickListener(v -> {
            // Minimize or hide the alert activity for discretion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                moveTaskToBack(true);
            } else {
                finish();
            }
        });

        mapView = new MapView(this);
        mapView.onCreate(savedInstanceState);
        ((FrameLayout) findViewById(R.id.mapPreviewContainer)).removeAllViews();
        ((FrameLayout) findViewById(R.id.mapPreviewContainer)).addView(mapView);
        mapView.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start the elapsed timer for the top timer (tvTimer)
        startElapsedTimer();
    }

    private void showCallEmergencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Call Emergency");
        builder.setMessage("Choose who you want to call:");
        
        builder.setPositiveButton("Call Emergency Services", (dialog, which) -> {
            callEmergencyServices();
        });
        
        builder.setNegativeButton("Call Emergency Contact", (dialog, which) -> {
            callEmergencyContact();
        });
        
        builder.setNeutralButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void callEmergencyServices() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_PERMISSION_REQUEST_CODE);
        } else {
            initiateCall(EMERGENCY_SERVICES_NUMBER);
        }
    }

    private void callEmergencyContact() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_PERMISSION_REQUEST_CODE);
        } else {
            // Get emergency contacts from database
            AppDatabase db = AppDatabase.getDatabase(this);
            new Thread(() -> {
                List<EmergencyContact> contacts = db.emergencyContactDao().getAllContactsBlocking();
                runOnUiThread(() -> {
                    if (contacts != null && !contacts.isEmpty()) {
                        // Use the first emergency contact for testing
                        EmergencyContact contact = contacts.get(0);
                        String phoneNumber = "tel:" + contact.phoneNumber;
                        initiateCall(phoneNumber);
                    } else {
                        // Fallback to a test number if no contacts are available
                        String testNumber = "tel:+1234567890"; // Test number for development
                        initiateCall(testNumber);
                        Toast.makeText(this, "No emergency contacts found. Using test number.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }

    private void initiateCall(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse(phoneNumber));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Log.e("AlertActivity", "Permission denied for making calls", e);
            Toast.makeText(this, "Permission denied to make calls", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AlertActivity", "Error initiating call", e);
            Toast.makeText(this, "Error making call", Toast.LENGTH_SHORT).show();
        }
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

    private void animateRecordingDot() {
        if (imgRecordingDot != null) {
            Animation blink = new AlphaAnimation(0.0f, 1.0f);
            blink.setDuration(500);
            blink.setStartOffset(20);
            blink.setRepeatMode(Animation.REVERSE);
            blink.setRepeatCount(Animation.INFINITE);
            imgRecordingDot.startAnimation(blink);
        }
    }

    private void simulateEmergencyActions() {
        // Start recording timer
        startRecordingTimer();
        cbRecordingStarted.setChecked(true);
        imgRecordingStarted.setImageResource(android.R.drawable.checkbox_on_background);
        tvRecordingStatus.setText("Recording...");

        // Simulate sending SMS and sharing location after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            cbContactsAlerted.setChecked(true);
            imgContactsAlerted.setImageResource(android.R.drawable.checkbox_on_background);
            cbLocationShared.setChecked(true);
            imgLocationShared.setImageResource(android.R.drawable.checkbox_on_background);
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

    private void startElapsedTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
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
        } else if (requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, retry the call action
                // We'll need to store the last action and retry it
                Toast.makeText(this, "Call permission granted. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission is required to make emergency calls.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    lastKnownLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
            });
        } else {
            // Show a message if location permission is missing
            Toast.makeText(this, "Location permission not granted. Map preview unavailable.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopTimer();
        // Reset the flag so a new alert can be triggered
        ShakeDetectorService.setAlertInactive();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }
} 