package com.pk.alarmclock.alarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pk.alarmclock.BuildConfig;

import java.util.Objects;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmBroadcastReceiver";
    String ACTION_DISMISS = BuildConfig.APPLICATION_ID + ".ACTION_DISMISS";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: " + "TRIGGERED");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent jobServiceIntent = new Intent(context, ReSchedAlarmService.class);
            ReSchedAlarmService.enqueueWork(context, jobServiceIntent);

        } else if (Objects.equals(intent.getAction(), ACTION_DISMISS)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            int alarmId = intent.getIntExtra("alarmIdKey", -1);

            // Cancel alarm
            Intent alarmCancelIntent = new Intent(context, AlarmService.class);
            PendingIntent pendingIntent = PendingIntent.getForegroundService(context,
                    alarmId, alarmCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);

            // Dismiss notification using alarmId received from intent
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.cancel(alarmId);

        }
    }
}
