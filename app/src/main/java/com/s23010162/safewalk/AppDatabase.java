package com.s23010162.safewalk;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Alert.class, EmergencyContact.class, UserProfile.class, Recording.class, Walk.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlertDao alertDao();
    public abstract EmergencyContactDao emergencyContactDao();
    public abstract UserProfileDao userProfileDao();
    public abstract RecordingDao recordingDao();
    public abstract WalkDao walkDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "safewalk_database")
                            .fallbackToDestructiveMigration() // Not for production
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 