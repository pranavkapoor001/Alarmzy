package com.pk.alarmclock.alarm.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.pk.alarmclock.alarm.helper.AlarmHelper;
import com.pk.alarmclock.misc.DaysOfWeek;
import com.pk.alarmclock.misc.MyApplication;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AlarmRepository {

    private static final String TAG = "AlarmRepository";
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

    public AlarmEntity getAlarm(int alarmId) {
        return alarmDao.getAlarm(alarmId);
    }

    public List<AlarmEntity> getAllAlarmsReSched() {
        return alarmDao.getAllAlarmsReSched();
    }

    public void insert(final AlarmEntity alarmEntity) {
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.insert(alarmEntity);
            }
        });
    }

    public void update(final AlarmEntity alarmEntity) {
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.update(alarmEntity);
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
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.updateAlarmStatus(alarmId, isAlarmEnabled);
                Log.e("REPO: ", "updateAlarmStatus: AlarmId: " + alarmId + " Toggle Set To: " + isAlarmEnabled);
            }
        });
    }

    public void updateAlarmIdTime(final int oldAlarmId, final int newAlarmId, final long alarmTime) {
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.updateAlarmIdTime(oldAlarmId, newAlarmId, alarmTime);
                // Update Toggle Value
                updateAlarmStatus(newAlarmId, true);

                // Re enable all enabled
                reEnableAlarmChild(newAlarmId);
            }
        });

    }

    public void setAlarmTitle(final String alarmTitle, final long alarmId) {
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                alarmDao.setAlarmTitle(alarmTitle, alarmId);
            }
        });
    }

    public void reEnableAlarmChild(final int newParentAlarmId) {

        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // wait for updateAlarmStatus to finish
                    MyApplication.databaseWriteExecutor.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                AlarmEntity currentEntity = getAlarm(newParentAlarmId);
                if (currentEntity == null)
                    return;

                Log.i(TAG, "PARENT ALARM SAYS: " + currentEntity.getAlarmEnabled()
                        + " WITH ID: " + currentEntity.getAlarmId());

                AlarmHelper ah = new AlarmHelper();

                Boolean[] daysOfRepeatArr = currentEntity.getDaysOfRepeatArr();
                if (daysOfRepeatArr[DaysOfWeek.IsRECURRING]) {
                    for (int i = 1; i < daysOfRepeatArr.length; i++) {
                        if (daysOfRepeatArr[i]) {
                            // This child alarm toggle is enabled
                            Log.i(TAG, "reEnableAlarm: Going to reEnable Child alarm at: " + i
                                    + " With ParentId: " + newParentAlarmId);
                            ah.repeatingAlarm(currentEntity, i);
                        }
                    }
                }
            }
        });
    }
}
