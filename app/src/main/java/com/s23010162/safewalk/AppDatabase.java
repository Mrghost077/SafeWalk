package com.s23010162.safewalk;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Alert.class, EmergencyContact.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlertDao alertDao();
    public abstract EmergencyContactDao emergencyContactDao();

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