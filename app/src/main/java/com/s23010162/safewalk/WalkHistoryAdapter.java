package com.s23010162.safewalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.s23010162.safewalk.utils.DateUtils;

public class WalkHistoryAdapter extends RecyclerView.Adapter<WalkHistoryAdapter.WalkViewHolder> {

    private List<Walk> walks = new ArrayList<>();

    @NonNull
    @Override
    public WalkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_walk, parent, false);
        return new WalkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WalkViewHolder holder, int position) {
        Walk currentWalk = walks.get(position);
        holder.bind(currentWalk);
    }

    @Override
    public int getItemCount() {
        return walks.size();
    }

    public void setWalks(List<Walk> walks) {
        this.walks = walks;
        notifyDataSetChanged();
    }

    static class WalkViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWalkDate;
        private final TextView tvWalkDuration;

        public WalkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWalkDate = itemView.findViewById(R.id.tvWalkDate);
            tvWalkDuration = itemView.findViewById(R.id.tvWalkDuration);
        }

        public void bind(Walk walk) {
            // Format Date
            tvWalkDate.setText(DateUtils.formatWalkDate(new Date(walk.startTime)));

            // Format Duration
            tvWalkDuration.setText("Duration: " + DateUtils.formatTime(walk.duration));
        }
    }
} 