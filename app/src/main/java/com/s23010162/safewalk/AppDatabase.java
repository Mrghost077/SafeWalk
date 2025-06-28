package com.s23010162.safewalk;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Alert.class, EmergencyContact.class, UserProfile.class, Recording.class, Walk.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlertDao alertDao();
    public abstract EmergencyContactDao emergencyContactDao();
    public abstract UserProfileDao userProfileDao();
    public abstract RecordingDao recordingDao();
    public abstract WalkDao walkDao();

    private static volatile AppDatabase INSTANCE;

    // Migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add any new columns or tables for version 2
            // Example: database.execSQL("ALTER TABLE user_profile ADD COLUMN new_column TEXT");
        }
    };

    // Migration from version 2 to 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add any new columns or tables for version 3
            // Example: database.execSQL("ALTER TABLE alerts ADD COLUMN severity INTEGER DEFAULT 0");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "safewalk_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 