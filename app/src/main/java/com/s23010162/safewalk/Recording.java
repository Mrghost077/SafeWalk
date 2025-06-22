package com.s23010162.safewalk;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.File;

@Entity(tableName = "recordings")
public class Recording {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String filePath;
    public long timestamp;

    public String getName() {
        if (filePath == null || filePath.isEmpty()) {
            return "Recording";
        }
        return new File(filePath).getName();
    }
} 