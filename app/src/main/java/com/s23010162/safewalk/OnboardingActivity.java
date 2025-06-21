package com.s23010162.safewalk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnGetStarted;
    private Button btnNext;
    private Button btnSkip;
    private View[] indicators;
    private PreferencesManager preferencesManager;


    private OnboardingPagerAdapter pagerAdapter;
    private int currentPage = 0;

    private static final String PREFS_NAME = "SafeWalkPrefs";
    private static final String KEY_FIRST_TIME = "first_time_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user has already seen onboarding
        if (hasSeenOnboarding()) {
            proceedToMainApp();
            return;
        }

        setupStatusBar();
        preferencesManager = new PreferencesManager(this);

// Check if user has already completed onboarding and registration
        if (!preferencesManager.isFirstLaunch() && preferencesManager.isProfileComplete()) {
            proceedToMainApp(); // Already exists
            return;
        }

        setContentView(R.layout.activity_onboarding); // Keep after checking


        initViews();
        setupViewPager();
        setupClickListeners();
        setupPageChangeListener();
    }

    private boolean hasSeenOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return !prefs.getBoolean(KEY_FIRST_TIME, true);
    }

    private void markOnboardingAsSeen() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply();
    }

    private void setupStatusBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.background_color));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        btnGetStarted = findViewById(R.id.btn_get_started);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);

        // Initialize indicators
        indicators = new View[3];
        indicators[0] = findViewById(R.id.indicator_0);
        indicators[1] = findViewById(R.id.indicator_1);
        indicators[2] = findViewById(R.id.indicator_2);
    }

    private void setupViewPager() {
        pagerAdapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Set initial state
        updateUI(0);
    }

    private void setupClickListeners() {
        btnGetStarted.setOnClickListener(v -> {
            animateButton(v);
            markOnboardingAsSeen();

            if (preferencesManager.isFirstLaunch() || !preferencesManager.isProfileComplete()) {
                // Go to Registration
                Intent intent = new Intent(OnboardingActivity.this, RegistrationActivity.class);
                startActivity(intent);
            } else {
                proceedToMainApp();
            }

            finish(); // Prevent going back to onboarding
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButton(v);
                if (currentPage < 2) {
                    viewPager.setCurrentItem(currentPage + 1, true);
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButton(v);
                markOnboardingAsSeen();
                proceedToMainApp();
            }
        });
    }

    private void setupPageChangeListener() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateUI(position);
            }
        });
    }

    private void updateUI(int position) {
        // Update indicators
        for (int i = 0; i < indicators.length; i++) {
            if (i == position) {
                indicators[i].setBackgroundResource(R.drawable.indicator_active);
                // Make active indicator wider
                indicators[i].getLayoutParams().width = dpToPx(24);
            } else {
                indicators[i].setBackgroundResource(R.drawable.indicator_inactive);
                // Make inactive indicators smaller
                indicators[i].getLayoutParams().width = dpToPx(8);
            }
            indicators[i].requestLayout();
        }

        // Update buttons based on current page
        if (position == 2) { // Last page
            btnNext.setVisibility(View.GONE);
            btnGetStarted.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
            btnGetStarted.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void animateButton(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    }
                })
                .start();
    }

    private void proceedToMainApp() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        if (currentPage > 0) {
            // Go to previous page
            viewPager.setCurrentItem(currentPage - 1, true);
        } else {
            // Exit app or handle as needed
            super.onBackPressed();
        }
    }
}
