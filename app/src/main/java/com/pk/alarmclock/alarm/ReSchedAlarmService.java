package com.pk.alarmclock.alarm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.pk.alarmclock.alarm.db.AlarmEntity;
import com.pk.alarmclock.alarm.db.AlarmRepository;

import java.util.List;

public class ReSchedAlarmService extends JobIntentService {

    public static final int JOB_ID = 1;
    List<AlarmEntity> alarms;

    public static void enqueueWork(Context context, Intent jobServiceIntent) {
        enqueueWork(context, ReSchedAlarmService.class, JOB_ID, jobServiceIntent);
    }

    // JobIntentService already holds a wakelock for us: Simply run the job
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.e("PK: ReSchedAlarmService", "onHandleWorkTriggered");

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
                ah.reEnableAlarm(ae.getAlarmTime());
                Log.e("ReSchedAlarmService", "AlarmEnabled(OldId): " + ae.getAlarmId());
            }
        }
    }
}
