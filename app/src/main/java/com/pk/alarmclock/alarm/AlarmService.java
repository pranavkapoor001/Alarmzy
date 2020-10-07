package com.pk.alarmclock.alarm;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pk.alarmclock.NotificationHelper;

// Note: Define service in AndroidManifest.xml

public class AlarmService extends Service {
    private static final String TAG = "AlarmService";
    private MediaPlayer player;


    public AlarmService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /* Start Foreground service with alarm notification
         * Also Associates ongoing notification with service
         */
        Log.i(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AlarmService Started");
        int alarmId = intent.getExtras().getInt("alarmIdKey", -1);
        Log.e(TAG, "Got alarmIdKey" + alarmId);


        // deliver notification with alarmId to disable toggle when alarm is dismissed
        startForeground(1, new NotificationHelper(this, alarmId).deliverNotification());

        playAlarm();
        return super.onStartCommand(intent, flags, startId);
    }

    public void playAlarm() {

        Log.e(TAG, "AlarmDeliver Called");

        // Get the default alarm sound
        Uri alarmSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        // init media player
        player = new MediaPlayer();

        // setDataSource() and prepare() can throw an Exception
        try {
            player.setDataSource(this, alarmSound);

            /* Set audio characteristics
             * Here we need to play the sound on alarm channel
             */
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build();

            /* Set attributes to MediaPlayer and prepare the stream
             * NOTE: All audio characteristics must be set before calling
             *       setAudioAttributes(), otherwise they will be ignored
             */
            player.setLooping(true);
            player.setAudioAttributes(audioAttributes);
            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the stream when its ready
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                player.start();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            Log.e(TAG, "MediaPlayer Released");
            player.release();
            player = null;
        }
    }

    // Return null here as bound service is not used
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
