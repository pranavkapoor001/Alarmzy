package com.pk.alarmzy.alarm.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.pk.alarmzy.alarm.db.AlarmEntity;
import com.pk.alarmzy.alarm.db.AlarmRepository;
import com.pk.alarmzy.alarm.helper.AlarmHelper;
import com.pk.alarmzy.misc.Constants;
import com.pk.alarmzy.misc.MyApplication;

import java.util.List;

public class ReSchedAlarmService extends JobIntentService {

    public static final int JOB_ID = 1;
    private static final String TAG = "ReSchedAlarmService";

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
        List<AlarmEntity> alarms = ar.getAllAlarmsReSched();

        // Iterate through all alarms and reEnable where getAlarmEnabled() returns true
        AlarmEntity ae;
        for (int i = 0; i < alarms.size(); i++) {
            ae = alarms.get(i);
            if (ae.getAlarmEnabled()) {

                /* Parent alarm time has passed and no child alarms are enabled
                 * Cancel this alarm toggle and skip to next iteration
                 */
                if (ae.getAlarmTime() < System.currentTimeMillis() &&
                        !ae.getDaysOfRepeatArr()[Constants.IsRECURRING]) {

                    Log.e(TAG, "onHandleWork: ParentTime passed, no child alarms");
                    ah.cancelAlarm(ae, false, true, -1);
                    continue;
                }

                /* Parent alarm time has passed but child alarms are enabled
                 * Skip setting parent alarm
                 * But Schedule child alarms then skip to next iteration
                 */
                else if (ae.getAlarmTime() < System.currentTimeMillis()) {

                    Log.e(TAG, "onHandleWork: ParentTime passed, but child alarms enabled");
                    ar.reEnableAlarmChild(ae.getAlarmId());
                    continue;
                }

                // Parent alarm time has not passed
                // Goto Enable both parent and child alarms

                //Set old alarmId
                ah.oldAlarmId = ae.getAlarmId();
                ah.reEnableAlarm(ae);
                Log.i(TAG, "onHandleWork: AlarmEnabled(OldId): " + ae.getAlarmId());
            }
        }
    }
}
