package com.s23010162.safewalk;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RecordingDao {
    @Insert
    void insert(Recording recording);

    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    LiveData<List<Recording>> getAllRecordings();
} 