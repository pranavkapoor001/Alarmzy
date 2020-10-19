package com.pk.alarmclock.alarm;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.pk.alarmclock.NotificationHelper;
import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.db.AlarmEntity;
import com.pk.alarmclock.alarm.db.AlarmRepository;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

public class AlarmHelper {

    private static final String TAG = "AlarmHelper";
    Context context;
    Application app;
    AlarmRepository ar;
    // Indicates that alarm is freshly created. Not enabled again from toggle
    boolean isNew = true;
    // alarmId for alarm disabled by toggle
    int oldAlarmId;

    // Cancels alarm: Removes from db if delete is true
    public void cancelAlarm(AlarmEntity alarmEntity, boolean delete,
                            boolean cancelParent, int dayOfRepeat) {
        // Get instance of AlarmRepository
        context = MyApplication.getContext();
        app = new Application();
        ar = new AlarmRepository(app);

        // Cancel the registered alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);

        Boolean[] daysOfRepeatArr = alarmEntity.getDaysOfRepeatArr();

        /* daysOfRepeatArr is set through obj of ah class
         * It should not be null
         */
        if (daysOfRepeatArr == null)
            Log.e(TAG, "cancelAlarm: " + "array is null");

        if (cancelParent) {
            // Cancel Parent alarm start

            int parentAlarmId = alarmEntity.getAlarmId();
            // Finally cancel parent alarm
            PendingIntent pendingIntent = PendingIntent.getForegroundService(context, parentAlarmId, intent, 0);
            alarmManager.cancel(pendingIntent);

            // Set Alarm toggle to false
            ar.updateAlarmStatus(parentAlarmId, false);

            //Delete from database(through repo)
            if (delete)
                ar.deleteAlarm(parentAlarmId);

            // Cancel parent alarm end


            // Cancel child alarms
            if (daysOfRepeatArr != null && daysOfRepeatArr[DaysOfWeek.IsRECURRING]) {
                int childAlarmId;
                for (int i = 1; i < daysOfRepeatArr.length; i++) {
                    if (daysOfRepeatArr[i]) {
                        // This child alarm is enabled
                        Log.i(TAG, "cancelAlarm: Child Alarm was Enabled" + i);
                        childAlarmId = parentAlarmId + i;
                        pendingIntent = PendingIntent.getForegroundService(context,
                                childAlarmId, intent, 0);
                        alarmManager.cancel(pendingIntent);
                        // daysOfRepeatArr[i] = false;
                    }
                }
            }

        } else {
            // Get child alarmId to be cancelled
            int childAlarmId = alarmEntity.getAlarmId() + dayOfRepeat;
            PendingIntent pendingIntent = PendingIntent.getForegroundService(context, childAlarmId, intent, 0);
            alarmManager.cancel(pendingIntent);

            Log.i(TAG, "cancelAlarm: Cancelled child AlarmID: " + childAlarmId);
            // child alarm is disabled: reflect in toggle here
            assert daysOfRepeatArr != null;
            daysOfRepeatArr[dayOfRepeat] = false;
            alarmEntity = new AlarmEntity(alarmEntity.getAlarmTime(), alarmEntity.getAlarmId(),
                    alarmEntity.getAlarmEnabled(), daysOfRepeatArr, alarmEntity.getAlarmTitle());
            ar.update(alarmEntity);

            // Check if all child alarms are disabled
            boolean flag = false;
            for (int i = 1; i < daysOfRepeatArr.length; i++) {
                // Some child alarm is enabled
                // leave parent toggle enabled
                if (daysOfRepeatArr[i]) {
                    flag = true;
                    break;
                }
            }

            // Set recurring flag to false
            if (!flag)
                daysOfRepeatArr[DaysOfWeek.IsRECURRING] = false;

            /* Disable parent toggle if all child alarms are disabled
             * and parent alarm time has passed
             */
            if (!flag && alarmEntity.getAlarmTime() < System.currentTimeMillis()) {
                Log.i(TAG, "cancelAlarm: No child alarms / ParentTime passed");
                Log.i(TAG, "cancelAlarm: Going to disable parent toggle");
                daysOfRepeatArr[DaysOfWeek.IsRECURRING] = false;
                ar.updateAlarmStatus(alarmEntity.getAlarmId(), false);
            }
        }
    }

    // Create new alarm with Calendar data
    public void createAlarm(Calendar c) {

        context = MyApplication.getContext();
        app = new Application();
        ar = new AlarmRepository(app);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        intent.putExtra("alarmIdKey", alarmId);
        Log.i(TAG, "createAlarm: Putting alarmIdKey: " + alarmId);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context,
                alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // increment to next day if alarm time has passed
        if (c.before(Calendar.getInstance()))
            c.add(Calendar.DATE, 1);

        AlarmManager.AlarmClockInfo alarmClockInfo =
                new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), null);

        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

        // Get alarmTime in milliSeconds
        long alarmTime = c.getTimeInMillis();

        Log.i(TAG, "createAlarm: AlarmID" + alarmId);
        Log.i(TAG, "createAlarm: isNew: " + isNew);
        Log.i(TAG, "createAlarm: Time: " + c.getTime());

        // Update alarmId of alarm enabled by Toggle
        if (!isNew) {
            Log.i(TAG, "createAlarm: Not New");
            ar.updateAlarmIdTime(oldAlarmId, alarmId, alarmTime);
        } else {
            Boolean[] daysOfRepeatArr = new Boolean[8];
            // Populate array with all elements set to false (New alarm, not recurring)
            Arrays.fill(daysOfRepeatArr, false);
            AlarmEntity alarm = new AlarmEntity(alarmTime, alarmId, true,
                    daysOfRepeatArr, context.getString(R.string.alarm_title));
            ar.insert(alarm);
        }
    }

    /* Create alarm when Toggle is enabled
     * REQUIRED: Set old alarmId using AlarmHelpers obj
     */
    public void reEnableAlarm(AlarmEntity alarmEntity) {
        if (oldAlarmId == 0)
            Log.e(TAG, "reEnableAlarm: oldAlarmId NOT SET !");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(alarmEntity.getAlarmTime());
        isNew = false;
        /* Schedule parentAlarm
         * Using parentAlarmsTime Cal, Schedule its child alarms
         */
        createAlarm(cal);
    }


    public void repeatingAlarm(AlarmEntity alarmEntity, int dayOfRepeat) {
        context = MyApplication.getContext();
        app = new Application();
        ar = new AlarmRepository(app);

        // Update to show repeating Alarm on this day is enabled
        Boolean[] daysOfRepeatArr = alarmEntity.getDaysOfRepeatArr();
        daysOfRepeatArr[DaysOfWeek.IsRECURRING] = true;
        daysOfRepeatArr[dayOfRepeat] = true;

        /* No need to schedule child alarm if parent is disabled
         * Just update daysOfRepeatArr and return
         */
        if (!alarmEntity.getAlarmEnabled()) {
            Log.i(TAG, "repeatingAlarm: ParentAlarmDisabled, Skipping");
            Log.i(TAG, "repeatingAlarm: ParentAlarmId" + alarmEntity.getAlarmId());
            alarmEntity = new AlarmEntity(alarmEntity.getAlarmTime(), alarmEntity.getAlarmId(),
                    alarmEntity.getAlarmEnabled(), daysOfRepeatArr, alarmEntity.getAlarmTitle());
            ar.update(alarmEntity);
            return;
        }

        // We are here: Parent alarm is enabled

        Log.e(TAG, "repeatingAlarm, ParentAlarm is enabled");

        // Id
        int parentAlarmId = alarmEntity.getAlarmId();
        int childAlarmId = parentAlarmId + dayOfRepeat;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("alarmIdKey", childAlarmId);
        Log.i(TAG, "repeatingAlarm: Putting childAlarmIdKey: " + childAlarmId);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context,
                childAlarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        // childAlarm Time is same as parent
        cal.setTimeInMillis(alarmEntity.getAlarmTime());

        /* If day of repeat is same as day of parent alarm
         * Increment it to next week
         *
         * Get day of week of current parent alarm
         */
        Calendar parentTime = Calendar.getInstance();
        parentTime.setTimeInMillis(alarmEntity.getAlarmTime());
        int parentAlarmDay = parentTime.get(Calendar.DAY_OF_WEEK);

        if (dayOfRepeat == parentAlarmDay)
            cal.add(Calendar.WEEK_OF_MONTH, 1);

        // Set recurring alarms day to dayOfRepeat
        cal.set(Calendar.DAY_OF_WEEK, dayOfRepeat);

        /* If repeating alarm is set for
         * day < today then increment it to next week
         */
        if (cal.before(Calendar.getInstance()))
            cal.add(Calendar.WEEK_OF_MONTH, 1);

        // Set recurring alarms day to dayOfRepeat
        cal.set(Calendar.DAY_OF_WEEK, dayOfRepeat);

        Log.i(TAG, "repeatingAlarm: NewTime: : " + cal.getTime() + " In millis: " + cal.getTimeInMillis());

        // Set alarm
        AlarmManager.AlarmClockInfo alarmClockInfo =
                new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), null);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

        /* this will update only daysOfRepeatArr
         * all other values will be same
         * since alarmId of child does not need to be saved
         * keep parent alarmId and update all entries
         */
        alarmEntity = new AlarmEntity(alarmEntity.getAlarmTime(), parentAlarmId,
                alarmEntity.getAlarmEnabled(), daysOfRepeatArr, alarmEntity.getAlarmTitle());
        ar.update(alarmEntity);
    }

    public void snoozeAlarm() {
        final String KEY_SNOOZE_LENGTH = "snoozeLength";

        context = MyApplication.getContext();
        app = new Application();
        ar = new AlarmRepository(app);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        intent.putExtra("alarmIdKey", alarmId);
        Log.i("AlarmHelper", "Putting alarmIdKey: " + alarmId);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context,
                alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get Snooze Length
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String snoozeLengthStr = sharedPref.getString(KEY_SNOOZE_LENGTH, "10");

        /* Set default value to 10
         * Add null check to avoid warning
         * and npe later
         */
        int snoozeLengthInt = 10;
        if (snoozeLengthStr != null)
            snoozeLengthInt = Integer.parseInt(snoozeLengthStr);

        // Add snooze length
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, snoozeLengthInt);
        c.set(Calendar.SECOND, 0);

        AlarmManager.AlarmClockInfo alarmClockInfo =
                new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), null);

        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        NotificationHelper notificationHelper = new NotificationHelper(context, alarmId);
        notificationHelper.deliverPersistentNotification();
    }
}
