package com.s23010162.safewalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class EditProfileFragment extends Fragment {

    private EditText etFullName, etEmailAddress;
    private Button btnSave;
    private AppDatabase appDatabase;
    private UserProfile currentUserProfile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFullName = view.findViewById(R.id.etFullName);
        etEmailAddress = view.findViewById(R.id.etEmailAddress);
        btnSave = view.findViewById(R.id.btnSave);

        loadUserProfile();

        btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        appDatabase.userProfileDao().getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                currentUserProfile = userProfile;
                etFullName.setText(userProfile.getFullName());
                etEmailAddress.setText(userProfile.getEmailAddress());
            } else {
                // Initialize a new profile if one doesn't exist
                currentUserProfile = new UserProfile();
            }
        });
    }

    private void saveUserProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmailAddress.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserProfile.setFullName(fullName);
        currentUserProfile.setEmailAddress(email);

        AppExecutors.getInstance().diskIO().execute(() -> {
            appDatabase.userProfileDao().insert(currentUserProfile);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(EditProfileFragment.this).navigateUp();
            });
        });
    }
} 