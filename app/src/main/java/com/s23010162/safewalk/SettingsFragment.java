package com.s23010162.safewalk;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnManageEmergencyContacts).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_contacts));

        view.findViewById(R.id.btnPrivacyPolicy).setOnClickListener(v ->
                Toast.makeText(getContext(), "Privacy Policy clicked", Toast.LENGTH_SHORT).show());
    }
} 