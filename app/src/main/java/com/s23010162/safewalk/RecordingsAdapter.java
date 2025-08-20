package com.s23010162.safewalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Date;
import java.util.List;

import com.s23010162.safewalk.utils.DateUtils;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.RecordingViewHolder> {

    private List<Recording> recordings;

    public RecordingsAdapter(List<Recording> recordings) {
        this.recordings = recordings;
    }

    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording, parent, false);
        return new RecordingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        Recording recording = recordings.get(position);
        holder.tvName.setText(recording.getName());
        holder.tvTimestamp.setText(formatTimestamp(recording.timestamp));
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    private String formatTimestamp(long timestamp) {
        return DateUtils.formatDateTime24H(new Date(timestamp));
    }

    public void setRecordings(List<Recording> recordings) {
        this.recordings = recordings;
        notifyDataSetChanged();
    }

    static class RecordingViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvTimestamp;

        public RecordingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecordingName);
            tvTimestamp = itemView.findViewById(R.id.tvRecordingTimestamp);
        }
    }
} 