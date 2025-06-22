package com.s23010162.safewalk;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WalkDao {
    @Insert
    void insert(Walk walk);

    @Query("SELECT * FROM walks ORDER BY startTime DESC")
    LiveData<List<Walk>> getAllWalks();
} 