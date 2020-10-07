package com.pk.alarmclock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.pk.alarmclock.alarm.AlarmTriggerActivity;

public class NotificationHelper {

    static final String PRIMARY_CHANNEL_ID = "primary_channel_id";
    NotificationManager mNotifyManager;
    public int mAlarmId;
    Context mContext;

    public NotificationHelper(Context context, int alarmId) {
        this.mContext = context;
        this.mAlarmId = alarmId;
    }

    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new NotificationChannel(
                PRIMARY_CHANNEL_ID, "Alarm Clock1", NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("Notifications From Alarm1");
        mNotifyManager.createNotificationChannel(notificationChannel);

        Log.i("NotificationHelper ", "Channel Created");
    }

    public Notification deliverNotification() {


        Intent fullScreenIntent = new Intent(mContext, AlarmTriggerActivity.class);
        fullScreenIntent.putExtra("alarmIdKey", mAlarmId);
        Log.e("NotificationHelper", "obj saved alarmIdKey = " + mAlarmId);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(mContext,
                0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext, PRIMARY_CHANNEL_ID)
                .setContentTitle("Alarm Triggered")
                .setContentText("Notification From Alarm1")
                .setSmallIcon(R.drawable.ic_alarm)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                /* Since A10 activity cannot be started from service(Including foreground service)
                 * Use a High priority notification with FullScreenPendingIntent()
                 * Also requires USE_FULL_SCREEN_INTENT permission in manifest
                 */
                .setFullScreenIntent(fullScreenPendingIntent, true);

        // Return a Notification object to be used by startForeground()
        Notification notification = builder.build();
        return notification;
    }
}
