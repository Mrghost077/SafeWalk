package com.s23010162.safewalk;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AlertDao {
    @Insert
    void insert(Alert alert);

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    List<Alert> getAllAlerts();
} 