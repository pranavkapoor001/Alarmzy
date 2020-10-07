package com.pk.alarmclock.alarm.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = AlarmEntity.class, version = 1)
public abstract class AlarmDatabase extends RoomDatabase {

    private static AlarmDatabase instance;

    public static synchronized AlarmDatabase getInstance(Context context) {

        // Build database instance if not already present
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AlarmDatabase.class,
                    "alarm_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    // Method to access Dao object
    // Room creates code for this abstract class when we build its instance
    public abstract AlarmDao alarmDao();
}