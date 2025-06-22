package com.s23010162.safewalk;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "walks")
public class Walk {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long startTime;
    public long endTime;
    public long duration; // in milliseconds
} 