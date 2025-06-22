package com.s23010162.safewalk;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserProfile userProfile);

    @Query("SELECT * FROM user_profile LIMIT 1")
    LiveData<UserProfile> getUserProfile();

    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfile getProfile();
} 