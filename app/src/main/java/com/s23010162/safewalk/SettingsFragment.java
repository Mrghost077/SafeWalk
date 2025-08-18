package com.s23010162.safewalk;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsFragment extends Fragment {

    private Switch switchShakeSensitivity, switchBackgroundLocation, switchAutoRecord, switchVibrate, switchSoundAlerts;
    private PreferencesManager preferencesManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize switches
        switchShakeSensitivity = view.findViewById(R.id.switchShakeSensitivity);
        switchBackgroundLocation = view.findViewById(R.id.switchBackgroundLocation);
        switchAutoRecord = view.findViewById(R.id.switchAutoRecord);
        switchVibrate = view.findViewById(R.id.switchVibrate);
        switchSoundAlerts = view.findViewById(R.id.switchSoundAlerts);

        // Initialize preferences
        preferencesManager = new PreferencesManager(requireContext());

        // Set initial states from preferences
        switchShakeSensitivity.setChecked(preferencesManager.isShakeDetectionEnabled());
        switchBackgroundLocation.setChecked(preferencesManager.isLocationTrackingEnabled());
        switchAutoRecord.setChecked(preferencesManager.isAutoRecordingEnabled());

        // Navigation buttons
        view.findViewById(R.id.btnNavManageContacts).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_contacts));

        view.findViewById(R.id.btnNavEditProfile).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_edit_profile));
        
        view.findViewById(R.id.btnNavPrivacyPolicy).setOnClickListener(v -> 
                Toast.makeText(getContext(), "Privacy Policy Clicked", Toast.LENGTH_SHORT).show());

        // Switch listeners
        switchShakeSensitivity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Persist preference
            preferencesManager.setShakeDetectionEnabled(isChecked);
            // Handle shake sensitivity toggle
            Toast.makeText(getContext(), "Shake sensitivity: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        switchBackgroundLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Persist preference
            preferencesManager.setLocationTrackingEnabled(isChecked);
            // Handle background location toggle
            Toast.makeText(getContext(), "Background location: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        switchAutoRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Persist preference
            preferencesManager.setAutoRecordingEnabled(isChecked);
            // Handle auto-record toggle
            Toast.makeText(getContext(), "Auto-record on SOS: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle vibrate toggle
            Toast.makeText(getContext(), "Vibrate on alert: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        switchSoundAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle sound alerts toggle
            Toast.makeText(getContext(), "Sound alerts: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });
    }
} 