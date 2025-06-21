package com.s23010162.safewalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager = new PreferencesManager(this);

        checkAppState();  // <--- smart launcher logic
    }

    private void checkAppState() {
        if (preferencesManager.isFirstLaunch()) {
            navigateToOnboarding();
        } else if (!preferencesManager.isProfileComplete()) {
            navigateToRegistration();
        } else {
            setupMainActivity(); // Load the actual UI
        }
    }

    private void navigateToOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupMainActivity() {
        // Enable full-screen layout with padding for system bars
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load and show user profile
        UserProfile userProfile = preferencesManager.getUserProfile();
        if (userProfile != null) {
            TextView tvWelcome = findViewById(R.id.tvWelcome);
            String welcomeText = "Welcome, " + userProfile.getFullName() + "!";
            tvWelcome.setText(welcomeText);
        }
    }
}
