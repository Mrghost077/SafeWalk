package com.s23010162.safewalk;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private RecyclerView rvContacts;
    private ContactsAdapter contactsAdapter;
    private AppDatabase db;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getDatabase(getContext());
        rvContacts = view.findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with an empty list
        contactsAdapter = new ContactsAdapter(new ArrayList<>());
        rvContacts.setAdapter(contactsAdapter);

        // Observe the LiveData from the database
        db.emergencyContactDao().getAllContacts().observe(getViewLifecycleOwner(), contacts -> {
            contactsAdapter.setContacts(contacts);
        });

        // Set up the button to launch the AddEditContactActivity
        view.findViewById(R.id.btnAddContact).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddEditContactActivity.class));
        });
    }
} 