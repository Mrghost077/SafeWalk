package com.s23010162.safewalk;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager = new PreferencesManager(this);

        // Check the app state. Redirect if necessary.
        if (!isSetupComplete()) {
            return; // isSetupComplete will handle the redirect and finish this activity
        }

        // If setup is complete, proceed to load the main UI
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        // Start the shake detector service
        Intent intent = new Intent(this, ShakeDetectorService.class);
        startService(intent);
    }

    private boolean isSetupComplete() {
        if (preferencesManager.isFirstLaunch()) {
            // User hasn't even finished onboarding
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return false;
        }

        if (!preferencesManager.isProfileComplete()) {
            // Onboarding is done, but registration is not
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
            return false;
        }

        // Both onboarding and registration are complete
        return true;
    }
}
