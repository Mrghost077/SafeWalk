package com.s23010162.safewalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsFragment extends Fragment {

    private RecyclerView rvEmergencyContacts;
    private EmergencyContactsAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_contacts, container, false);

        rvEmergencyContacts = view.findViewById(R.id.rvEmergencyContacts);
        rvEmergencyContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        db = AppDatabase.getDatabase(requireContext());

        adapter = new EmergencyContactsAdapter(new ArrayList<>(), this::deleteContact);
        rvEmergencyContacts.setAdapter(adapter);

        loadContacts();

        Button btnAddContact = view.findViewById(R.id.btnAddContact);
        btnAddContact.setOnClickListener(v -> {
            showAddContactDialog();
        });

        return view;
    }

    private void showAddContactDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);

        final TextInputEditText etName = dialogView.findViewById(R.id.etContactName);
        final TextInputEditText etRelationship = dialogView.findViewById(R.id.etContactRelationship);
        final TextInputEditText etPhone = dialogView.findViewById(R.id.etContactPhone);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Emergency Contact")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String relationship = etRelationship.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(getContext(), "Name and phone number cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    EmergencyContact contact = new EmergencyContact(name, relationship, phone);
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        db.emergencyContactDao().insert(contact);
                        loadContacts();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadContacts() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            final List<EmergencyContact> contacts = db.emergencyContactDao().getAll();
            AppExecutors.getInstance().mainThread().execute(() -> {
                adapter.setContacts(contacts);
            });
        });
    }

    private void deleteContact(EmergencyContact contact) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            db.emergencyContactDao().delete(contact);
            loadContacts();
        });
    }
} 