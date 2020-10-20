package com.pk.alarmclock.alarm.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.pk.alarmclock.BuildConfig;
import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.AlarmTriggerActivity;
import com.pk.alarmclock.misc.AlarmBroadcastReceiver;
import com.pk.alarmclock.misc.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationHelper {

    static final String PRIMARY_CHANNEL_ID = "primary_channel_id";
    private static final String TAG = "NotificationHelper";
    public int mAlarmId;
    NotificationManager mNotifyManager;
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

        Log.i(TAG, "createNotificationChannel: Channel Created");
    }

    public Notification deliverNotification() {


        Intent fullScreenIntent = new Intent(mContext, AlarmTriggerActivity.class);
        Log.i(TAG, "deliverNotification: Putting alarmIdKey: " + mAlarmId);
        fullScreenIntent.putExtra("alarmIdKey", mAlarmId);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(mContext,
                0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Display Alarm Time in notification
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        String formattedTime = sdf.format(System.currentTimeMillis());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext, PRIMARY_CHANNEL_ID)
                .setContentTitle("Alarm")
                .setContentText(formattedTime)
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
        return builder.build();
    }

    public void deliverPersistentNotification() {
        String ACTION_DISMISS = BuildConfig.APPLICATION_ID + ".ACTION_DISMISS";
        final String KEY_SNOOZE_LENGTH = "snoozeLength";

        mNotifyManager = (NotificationManager) MyApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent dismissIntent = new Intent(mContext, AlarmBroadcastReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        // AlarmId received from constructor
        dismissIntent.putExtra("alarmIdKey", mAlarmId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(mContext,
                mAlarmId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Display Alarm Time in notification
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());

        // Get snoozeLength
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String snoozeLengthStr = sharedPref.getString(KEY_SNOOZE_LENGTH, "10");

        /* Set default value to 10
         * Add null check to avoid warning
         * and npe later
         */
        int snoozeLengthInt = 10;
        if (snoozeLengthStr != null)
            snoozeLengthInt = Integer.parseInt(snoozeLengthStr);


        // Add current time to snoozeLength(Min)* millis
        long snoozeTargetTime = System.currentTimeMillis() + snoozeLengthInt * 60000;
        String formattedTime = sdf.format(snoozeTargetTime);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarm")
                .setContentText("Snoozing... " + formattedTime)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_alarm_dismiss, mContext.getResources().getString(R.string.dismiss),
                        dismissPendingIntent);

        /* Build notification with id as alarmID
         * So it can be cancelled by Receiver using alarmId
         *
         */
        mNotifyManager.notify(mAlarmId, builder.build());
    }

    // Use silenceTimeout to show actual alarmId instead of current time
    public void deliverMissedNotification(int silenceTimeout) {
        mNotifyManager = (NotificationManager) MyApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Display Alarm Time in notification
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        // Set formattedTime to currentTime - silenceTimeout(minutes) * millis
        String formattedTime = sdf.format(System.currentTimeMillis() - silenceTimeout * 60000);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarm")
                .setContentText("Missed Alarm: " + formattedTime)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        // Use alarmId just to create a unique notification
        mNotifyManager.notify(mAlarmId, builder.build());
    }
}