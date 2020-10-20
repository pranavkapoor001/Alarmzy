package com.pk.alarmclock.alarm.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.pk.alarmclock.alarm.db.AlarmEntity;
import com.pk.alarmclock.alarm.db.AlarmRepository;
import com.pk.alarmclock.alarm.helper.AlarmHelper;
import com.pk.alarmclock.misc.MyApplication;

import java.util.List;

public class ReSchedAlarmService extends JobIntentService {

    public static final int JOB_ID = 1;
    private static final String TAG = "ReSchedAlarmService";
    List<AlarmEntity> alarms;

    public static void enqueueWork(Context context, Intent jobServiceIntent) {
        enqueueWork(context, ReSchedAlarmService.class, JOB_ID, jobServiceIntent);
    }

    // JobIntentService already holds a wakelock for us: Simply run the job
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "onHandleWork: Triggered");

        AlarmHelper ah = new AlarmHelper();

        // Fetch all alarms in a List using Repo(then Dao)
        AlarmRepository ar = new AlarmRepository(MyApplication.getContext());
        alarms = ar.getAllAlarmsReSched();

        // Iterate through all alarms and reEnable where getAlarmEnabled() returns true
        AlarmEntity ae;
        for (int i = 0; i < alarms.size(); i++) {
            ae = alarms.get(i);
            if (ae.getAlarmEnabled()) {
                //Set old alarmId
                ah.oldAlarmId = ae.getAlarmId();
                ah.reEnableAlarm(ae);
                Log.i(TAG, "onHandleWork: AlarmEnabled(OldId): " + ae.getAlarmId());
            }
        }
    }
}
