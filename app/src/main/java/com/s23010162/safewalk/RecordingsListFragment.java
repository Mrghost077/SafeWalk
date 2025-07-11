package com.s23010162.safewalk;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecordingsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecordingsAdapter adapter;
    private TextView tvNoRecordings;
    private AppDatabase appDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordings_list, container, false);

        recyclerView = view.findViewById(R.id.rvRecordings);
        tvNoRecordings = view.findViewById(R.id.tvNoRecordings);
        
        adapter = new RecordingsAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        observeRecordings();
        
        return view;
    }

    private void observeRecordings() {
        appDatabase.recordingDao().getAllRecordings().observe(getViewLifecycleOwner(), recordings -> {
            if (recordings != null && !recordings.isEmpty()) {
                adapter.setRecordings(recordings);
                tvNoRecordings.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                tvNoRecordings.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
} 