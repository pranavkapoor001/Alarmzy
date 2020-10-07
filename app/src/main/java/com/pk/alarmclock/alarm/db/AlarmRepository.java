package com.pk.alarmclock.alarm.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmRepository {

    private static final int NUMBER_OF_THREADS = 1;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private AlarmDao alarmDao;
    private LiveData<List<AlarmEntity>> allAlarms;

    public AlarmRepository(Application application) {
        // Get handle to Database and get all alarms
        AlarmDatabase alarmDatabase = AlarmDatabase.getInstance(application);
        alarmDao = alarmDatabase.alarmDao();
        allAlarms = alarmDao.getAllAlarms();
    }

    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return allAlarms;
    }

    public void insert(final AlarmEntity alarmEntity) {
        databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.insert(alarmEntity);
            }
        });
    }

    public void deleteAlarm(final int alarmId) {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.deleteAlarm(alarmId);
            }
        });
    }

    public void updateAlarmStatus(final int alarmId, final boolean isAlarmEnabled) {
        databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.updateAlarmStatus(alarmId, isAlarmEnabled);
                Log.e("REPO: ", "updateAlarmStatus: AlarmId: " + alarmId + " Toggle Set To: " + isAlarmEnabled);
            }
        });
    }

    public void updateAlarmId(final int oldAlarmId, final int newAlarmId) {
        databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.updateAlarmId(oldAlarmId, newAlarmId);
                // Update Toggle Value
                updateAlarmStatus(newAlarmId, true);
            }
        });

    }
}
