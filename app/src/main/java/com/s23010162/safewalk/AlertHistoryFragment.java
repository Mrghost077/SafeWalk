package com.s23010162.safewalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.s23010162.safewalk.R;

import java.util.ArrayList;
import java.util.List;

public class AlertHistoryFragment extends Fragment {

    private RecyclerView rvAlertHistory;
    private AlertHistoryAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert_history, container, false);

        rvAlertHistory = view.findViewById(R.id.rvAlertHistory);
        rvAlertHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        db = AppDatabase.getDatabase(requireContext());

        adapter = new AlertHistoryAdapter(new ArrayList<>());
        rvAlertHistory.setAdapter(adapter);

        loadAlerts();

        return view;
    }

    private void loadAlerts() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            final List<Alert> alerts = db.alertDao().getAllAlerts();
            AppExecutors.getInstance().mainThread().execute(() -> {
                adapter.setAlerts(alerts);
            });
        });
    }
} 