package com.s23010162.safewalk;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.navigation.Navigation;

public class SettingsFragment extends Fragment {

    private PreferencesManager preferencesManager;
    private SwitchCompat switchShake, switchAutoRecording;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencesManager = new PreferencesManager(requireContext());

        // Find views
        switchShake = view.findViewById(R.id.switchShake);
        switchAutoRecording = view.findViewById(R.id.switchAutoRecording);

        // Load current settings
        loadSettings();

        view.findViewById(R.id.rlAlertHistory).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Alert History - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.rlEditProfile).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.rlEmergencyContacts).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Emergency Contacts - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Set up listeners for the new settings options
        view.findViewById(R.id.rlEmergencyPin).setOnClickListener(v ->
                Toast.makeText(getContext(), "Change PIN clicked", Toast.LENGTH_SHORT).show());

        // Set listeners for switches
        switchShake.setOnCheckedChangeListener((buttonView, isChecked) -> preferencesManager.setShakeDetectionEnabled(isChecked));
        switchAutoRecording.setOnCheckedChangeListener((buttonView, isChecked) -> preferencesManager.setAutoRecordingEnabled(isChecked));
    }

    private void loadSettings() {
        switchShake.setChecked(preferencesManager.isShakeDetectionEnabled());
        switchAutoRecording.setChecked(preferencesManager.isAutoRecordingEnabled());
    }
} 