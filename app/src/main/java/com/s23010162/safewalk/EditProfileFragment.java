package com.s23010162.safewalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPassword;
    private PreferencesManager preferencesManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        preferencesManager = new PreferencesManager(requireContext());

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        loadUserProfile();

        Button btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void loadUserProfile() {
        UserProfile userProfile = preferencesManager.getUserProfile();
        if (userProfile != null) {
            etName.setText(userProfile.getFullName());
            etEmail.setText(userProfile.getEmailAddress());
        }
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfile userProfile = preferencesManager.getUserProfile();
        if (userProfile == null) {
            userProfile = new UserProfile();
        }
        userProfile.setFullName(name);
        userProfile.setEmailAddress(email);
        if (!password.isEmpty()) {
            userProfile.setPassword(password);
        }

        preferencesManager.setUserProfile(userProfile);

        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
} 