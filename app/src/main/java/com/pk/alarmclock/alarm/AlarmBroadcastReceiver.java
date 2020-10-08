package com.pk.alarmclock.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.e("AlarmBroadcastReceiver", "TRIGGERED");
            Intent jobServiceIntent = new Intent(context, ReSchedAlarmService.class);
            ReSchedAlarmService.enqueueWork(context, jobServiceIntent);
        }
    }
}
