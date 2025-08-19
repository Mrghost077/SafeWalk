package com.s23010162.safewalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class AlertHistoryAdapter extends RecyclerView.Adapter<AlertHistoryAdapter.AlertViewHolder> {

    private List<Alert> alerts;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

    public AlertHistoryAdapter(List<Alert> alerts) {
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);
        holder.tvTimestamp.setText(dateFormat.format(new Date(alert.timestamp)));
        holder.tvLocation.setText("Lat: " + alert.latitude + ", Lon: " + alert.longitude);
        holder.tvType.setText("Type: " + (alert.type == null ? "Unknown" : alert.type));
        holder.tvRecordingStatus.setText(alert.recordingStarted ? "Recording: Started" : "Recording: Not started");
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
        notifyDataSetChanged();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp;
        TextView tvLocation;
        TextView tvType;
        TextView tvRecordingStatus;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvAlertTimestamp);
            tvLocation = itemView.findViewById(R.id.tvAlertLocation);
            tvType = itemView.findViewById(R.id.tvAlertType);
            tvRecordingStatus = itemView.findViewById(R.id.tvRecordingStatus);
        }
    }
} 