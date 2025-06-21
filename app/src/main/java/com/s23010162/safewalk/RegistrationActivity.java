package com.s23010162.safewalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.s23010162.safewalk.R;
import com.s23010162.safewalk.UserProfile;
import com.s23010162.safewalk.PreferencesManager;

/**
 * Registration Activity for user profile setup
 * This activity appears only during first-time app setup
 */
public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private TextInputLayout tilFullName, tilEmailAddress, tilPhoneNumber, tilEmergencyPin, tilConfirmPin;
    private TextInputEditText etFullName, etEmailAddress, etPhoneNumber, etEmergencyPin, etConfirmPin;
    private CheckBox cbLocationAccess, cbCameraAccess;
    private Button  btnCreateAccountBottom;

    // Utilities
    private PreferencesManager preferencesManager;

    // Validation flags
    private boolean isFormValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize preferences manager
        preferencesManager = new PreferencesManager(this);

        // Check if user should see this screen
        if (!preferencesManager.isFirstLaunch() && preferencesManager.isProfileComplete()) {
            navigateToMainActivity();
            return;
        }

        initializeViews();
        setupTextWatchers();
        setupClickListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });

    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        // Text Input Layouts
        tilFullName = findViewById(R.id.tilFullName);
        tilEmailAddress = findViewById(R.id.tilEmailAddress);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilEmergencyPin = findViewById(R.id.tilEmergencyPin);
        tilConfirmPin = findViewById(R.id.tilConfirmPin);

        // Edit Texts
        etFullName = findViewById(R.id.etFullName);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmergencyPin = findViewById(R.id.etEmergencyPin);
        etConfirmPin = findViewById(R.id.etConfirmPin);

        // Checkboxes
        cbLocationAccess = findViewById(R.id.cbLocationAccess);
        cbCameraAccess = findViewById(R.id.cbCameraAccess);

        // Buttons
        btnCreateAccountBottom = findViewById(R.id.btnCreateAccountBottom);

        // Initially disable buttons
        updateButtonState(false);
    }

    /**
     * Setup text watchers for real-time validation
     */
    private void setupTextWatchers() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };

        etFullName.addTextChangedListener(validationWatcher);
        etEmailAddress.addTextChangedListener(validationWatcher);
        etPhoneNumber.addTextChangedListener(validationWatcher);
        etEmergencyPin.addTextChangedListener(validationWatcher);
        etConfirmPin.addTextChangedListener(validationWatcher);
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        View.OnClickListener createAccountListener = v -> handleCreateAccount();

        btnCreateAccountBottom.setOnClickListener(createAccountListener);

        // Checkbox listeners for permission requests
        cbLocationAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestLocationPermission();
            }
        });

        cbCameraAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestCameraPermission();
            }
        });
    }

    /**
     * Validate the entire form
     */
    private void validateForm() {
        boolean isValid = true;

        // Validate full name
        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty() || fullName.length() < 2) {
            tilFullName.setError("Please enter a valid full name");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // Validate email
        String email = etEmailAddress.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmailAddress.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmailAddress.setError(null);
        }

        // Validate phone number
        String phone = etPhoneNumber.getText().toString().trim();
        String cleanPhone = phone.replaceAll("[^\\d]", "");
        if (cleanPhone.length() < 10) {
            tilPhoneNumber.setError("Please enter a valid phone number");
            isValid = false;
        } else {
            tilPhoneNumber.setError(null);
        }

        // Validate emergency PIN
        String emergencyPin = etEmergencyPin.getText().toString().trim();
        if (emergencyPin.length() != 4 || !emergencyPin.matches("\\d{4}")) {
            tilEmergencyPin.setError("PIN must be exactly 4 digits");
            isValid = false;
        } else {
            tilEmergencyPin.setError(null);
        }

        // Validate PIN confirmation
        String confirmPin = etConfirmPin.getText().toString().trim();
        if (!confirmPin.equals(emergencyPin)) {
            tilConfirmPin.setError("PINs do not match");
            isValid = false;
        } else if (emergencyPin.length() == 4) {
            tilConfirmPin.setError(null);
        }

        isFormValid = isValid;
        updateButtonState(isValid);
    }

    /**
     * Update button enabled state
     */
    private void updateButtonState(boolean enabled) {
        btnCreateAccountBottom.setEnabled(enabled);

        // Update button appearance
        float alpha = enabled ? 1.0f : 0.5f;
        btnCreateAccountBottom.setAlpha(alpha);
    }

    /**
     * Handle create account button click
     */
    private void handleCreateAccount() {
        if (!isFormValid) {
            Toast.makeText(this, "Please fix all errors before proceeding", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showLoadingState(true);

        // Create user profile
        UserProfile userProfile = createUserProfile();

        // Save profile
        boolean saveSuccess = preferencesManager.saveUserProfile(userProfile);

        if (saveSuccess) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

            // Navigate to main activity
            navigateToMainActivity();
        } else {
            showLoadingState(false);
            Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create user profile from form data
     */
    private UserProfile createUserProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmailAddress.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String emergencyPin = etEmergencyPin.getText().toString().trim();
        boolean locationAccess = cbLocationAccess.isChecked();
        boolean cameraAccess = cbCameraAccess.isChecked();

        return new UserProfile(fullName, email, phone, emergencyPin, locationAccess, cameraAccess);
    }

    /**
     * Show/hide loading state
     */
    private void showLoadingState(boolean loading) {
        btnCreateAccountBottom.setEnabled(!loading);

        if (loading) {
            btnCreateAccountBottom.setText("Creating Account...");
        } else {
            btnCreateAccountBottom.setText("Create Account");
        }
    }

    /**
     * Request location permission
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Request camera and microphone permissions
     */
    private void requestCameraPermission() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean needsPermission = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needsPermission = true;
                break;
            }
        }

        if (needsPermission) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handle permission request results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                // Check which checkbox was being processed and uncheck if permission denied
                for (String permission : permissions) {
                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                        if (ContextCompat.checkSelfPermission(this, permission)
                                != PackageManager.PERMISSION_GRANTED) {
                            cbLocationAccess.setChecked(false);
                            Toast.makeText(this, "Location permission is required for safety features",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else if (permission.equals(Manifest.permission.CAMERA) ||
                            permission.equals(Manifest.permission.RECORD_AUDIO)) {

                        if (ContextCompat.checkSelfPermission(this, permission)
                                != PackageManager.PERMISSION_GRANTED) {
                            cbCameraAccess.setChecked(false);
                            Toast.makeText(this, "Camera/microphone permission is required for evidence recording",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



    /**
     * Show exit confirmation dialog
     */
    private void showExitConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit Registration")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Continue", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}