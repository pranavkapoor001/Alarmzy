package com.pk.alarmclock.alarm;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pk.alarmclock.alarm.db.AlarmEntity;
import com.pk.alarmclock.alarm.db.AlarmRepository;

import java.util.Calendar;
import java.util.Random;

public class AlarmHelper {

    private static String TAG = "AlarmHelper";
    long alarmTime;
    Context context;
    Application app;
    AlarmRepository ar;
    // Indicates that alarm is freshly created. Not enabled again from toggle
    boolean isNew = true;
    // alarmId for alarm disabled by toggle
    int oldAlarmId;

    // Cancels alarm: Removes from db if delete is true
    public void cancelAlarm(int alarmId, boolean delete) {
        // Get instance of AlarmRepository
        context = MyApplication.getContext();
        app = new Application();
        ar = new AlarmRepository(app);

        // Cancel the registered alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context, alarmId, intent, 0);
        alarmManager.cancel(pendingIntent);

        // Set Alarm toggle to false
        ar.updateAlarmStatus(alarmId, false);

        //Delete from database(through repo)
        if (delete)
            ar.deleteAlarm(alarmId);
    }

    // Create new alarm with Calendar data from MainActivity
    public void createAlarm(Calendar c) {

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
        alarmTime = c.getTimeInMillis();

        // Format AlarmTime to format: 7:30 PM
        //DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        //alarmTime = df.format(c.getTime());
        Log.e(TAG, "createAlarm: AlarmHour" + c.get(Calendar.HOUR_OF_DAY));
        Log.e(TAG, "createAlarm: AlarmHour" + c.get(Calendar.MINUTE));
        Log.e(TAG, "createAlarm: AlarmID: " + alarmId);
        Log.e(TAG, "createAlarm: isNew: " + isNew);
        Log.e(TAG, "createAlarm: TimeInMs: " + c.getTimeInMillis());

        // Update alarmId of alarm enabled by Toggle
        if (!isNew) {
            Log.e(TAG, "createAlarm: Not New");
            ar.updateAlarmId(oldAlarmId, alarmId);
        } else {
            AlarmEntity alarm = new AlarmEntity(alarmTime, alarmId, true);
            ar.insert(alarm);
        }
    }

    // Create alarm when Toggle is enabled
    public void reEnableAlarm(long alarmTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(alarmTime);
        isNew = false;
        createAlarm(cal);
    }
}