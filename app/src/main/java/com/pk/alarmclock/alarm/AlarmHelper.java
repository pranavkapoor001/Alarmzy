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
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AlarmHelper {

    private static String TAG = "AlarmHelper";
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
        if (daysOfRepeatArr == null) {
            Log.e(TAG, "cancelAlarm, array is null");
        }

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
                // Cancel dummy alarm
                dummyAlarm(parentAlarmId, false);
                int childAlarmId;
                for (int i = 1; i < daysOfRepeatArr.length; i++) {
                    if (daysOfRepeatArr[i]) {
                        // This child alarm is enabled
                        Log.e(TAG, "cancelAlarm, Child Alarm was Enabled: " + i);
                        childAlarmId = parentAlarmId + i;
                        pendingIntent = PendingIntent.getForegroundService(context,
                                childAlarmId, intent, 0);
                        alarmManager.cancel(pendingIntent);
                        // daysOfRepeatArr[i] = false;
                    }
                }
            }
        } else {
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
             * also delete the dummy alarm
             */
            if (!flag && alarmEntity.getAlarmTime() < System.currentTimeMillis()) {
                Log.e(TAG, "ParentTime: "+alarmEntity.getAlarmTime());
                Log.e(TAG, "CurrentTime: "+System.currentTimeMillis());
                daysOfRepeatArr[DaysOfWeek.IsRECURRING] = false;
                ar.updateAlarmStatus(alarmEntity.getAlarmId(), false);
                dummyAlarm(alarmEntity.getAlarmId(), false);
            }
        }
    }

    /* Create new alarm with Calendar data
     * Return newParentAlarmId to Schedule its child alarms
     *      (Only for ReEnableAlarm())
     */
    public int createAlarm(Calendar c) {

        context = MyApplication.getContext();
        app = new Application();
        ar = new AlarmRepository(app);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        intent.putExtra("alarmIdKey", alarmId);
        Log.e("AlarmHelper", "Putting alarmIdKey: " + alarmId);
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

        Log.e(TAG, "createAlarm: AlarmHour" + c.get(Calendar.HOUR_OF_DAY));
        Log.e(TAG, "createAlarm: AlarmHour" + c.get(Calendar.MINUTE));
        Log.e(TAG, "createAlarm: AlarmID: " + alarmId);
        Log.e(TAG, "createAlarm: isNew: " + isNew);
        Log.e(TAG, "createAlarm: Time: " + c.getTime());

        // Update alarmId of alarm enabled by Toggle
        if (!isNew) {
            Log.e(TAG, "createAlarm: Not New");
            ar.updateAlarmId(oldAlarmId, alarmId);
        } else {
            Boolean[] daysOfRepeatArr = new Boolean[8];
            // Populate array with all elements set to false (New alarm, not recurring)
            Arrays.fill(daysOfRepeatArr, false);
            AlarmEntity alarm = new AlarmEntity(alarmTime, alarmId, true,
                    daysOfRepeatArr, context.getString(R.string.alarm_title));
            ar.insert(alarm);
        }
        return alarmId;
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
         * Using parentAlarmsId Schedule its child alarms
         */
        int newParentAlarmId = createAlarm(cal);
        reEnableAlarmChild(newParentAlarmId);
    }

    public void reEnableAlarmChild(final int newParentAlarmId) {

        /* Since we need to fetch AlarmEntity using Id from Db
         * Wait till parent toggle value is updated (createAlarm()-updateAlarmStatus()): Fixed timeout
         * Then run it on bg thread
         */
        final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(1);
        databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // wait for updateAlarmStatus to finish
                    databaseWriteExecutor.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                AlarmEntity currentEntity = ar.getAlarm(newParentAlarmId);
                if (currentEntity == null)
                    return;

                Log.e(TAG, "PARENT ALARM SAYS: " + currentEntity.getAlarmEnabled()
                        + " WITH ID: " + currentEntity.getAlarmId());

                Boolean[] daysOfRepeatArr = currentEntity.getDaysOfRepeatArr();
                if (daysOfRepeatArr[DaysOfWeek.IsRECURRING]) {
                    for (int i = 1; i < daysOfRepeatArr.length; i++) {
                        if (daysOfRepeatArr[i]) {
                            // This child alarm toggle is enabled
                            Log.e(TAG, "reEnableAlarm: Going to reEnable Child alarm at: " + i
                                    + " With ParentId: " + newParentAlarmId);
                            repeatingAlarm(currentEntity, i);
                        }
                    }
                }
            }
        });
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
            Log.e(TAG, "repeatingAlarm: ParentAlarmDisabled, Skipping");
            Log.e(TAG, "repeatingAlarm: ParentAlarmId" + alarmEntity.getAlarmId());
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
        Log.e(TAG, "repeatingAlarm: Putting childAlarmIdKey: " + childAlarmId);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context,
                childAlarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        // childAlarm Time is same as parent
        cal.setTimeInMillis(alarmEntity.getAlarmTime());

        /* If day of repeat is same as day today
         * Increment it to next week
         */
        Calendar todayCal = Calendar.getInstance();
        int dayToday = todayCal.get(Calendar.DAY_OF_WEEK);

        if (dayOfRepeat == dayToday)
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

        Log.e(TAG, "repeatingAlarm: NewTime: : " + cal.getTime() + " In millis: " + cal.getTimeInMillis());

        // Set childAlarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7, pendingIntent);

        /* this will update only daysOfRepeatArr
         * all other values will be same
         * since alarmId of child does not need to be saved
         * keep parent alarmId and update all entries
         */
        alarmEntity = new AlarmEntity(alarmEntity.getAlarmTime(), parentAlarmId,
                alarmEntity.getAlarmEnabled(), daysOfRepeatArr, alarmEntity.getAlarmTitle());
        ar.update(alarmEntity);

        // Create new alarm far in future.. just to show alarm icon to user
        dummyAlarm(parentAlarmId, true);
    }

    // dummyAlarmId = parentAlarmId + 20;
    public void dummyAlarm(int dummyAlarmId, boolean create) {
        context = MyApplication.getContext();

        Log.e(TAG, "dummyAlarm Called with id: " + dummyAlarmId);

        dummyAlarmId = dummyAlarmId + 20;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);

        // Cancel if dummy Alarm
        if (!create) {
            if (PendingIntent.getForegroundService(context, dummyAlarmId,
                    intent, PendingIntent.FLAG_NO_CREATE) != null)
                alarmManager.cancel(PendingIntent.getForegroundService(context, dummyAlarmId,
                        intent, PendingIntent.FLAG_NO_CREATE));

            return;
        }

        // Now we want to create a dummy alarm
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context,
                dummyAlarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calDummy = Calendar.getInstance();
        calDummy.set(Calendar.YEAR, 3020);

        AlarmManager.AlarmClockInfo alarmClockInfo =
                new AlarmManager.AlarmClockInfo(calDummy.getTimeInMillis(), null);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
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
        Log.e("AlarmHelper", "Putting alarmIdKey: " + alarmId);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context,
                alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get Snooze Length
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int snoozeLength = Integer.parseInt(Objects.requireNonNull(
                sharedPref.getString(KEY_SNOOZE_LENGTH, "10")));

        // Add snooze length
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, snoozeLength);

        AlarmManager.AlarmClockInfo alarmClockInfo =
                new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), null);

        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        NotificationHelper notificationHelper = new NotificationHelper(context, alarmId);
        notificationHelper.deliverPersistentNotification();
    }
}
