package com.s23010162.safewalk;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alerts")
public class Alert {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long timestamp;
    public String type; // e.g., "Shake Detected", "SOS Button"
    public double latitude;
    public double longitude;
    public boolean recordingStarted; // whether a recording was started for this alert

    public Alert(long timestamp, String type, double latitude, double longitude) {
        this.timestamp = timestamp;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }
} 