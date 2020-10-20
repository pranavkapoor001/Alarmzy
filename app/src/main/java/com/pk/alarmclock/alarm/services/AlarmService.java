package com.pk.alarmclock.alarm.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.pk.alarmclock.alarm.helper.NotificationHelper;

// Note: Define service in AndroidManifest.xml

public class AlarmService extends Service {
    private static final String TAG = "AlarmService";
    Vibrator v;
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

        /* This can produce npe
         * Check if key exists then fetch value
         */
        int alarmId = -1;
        if (intent.hasExtra("alarmIdKey"))
            alarmId = intent.getIntExtra("alarmIdKey", -1);

        Log.i(TAG, "Got alarmIdKey: " + alarmId);


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
        vibrateAlarm();
    }

    public void vibrateAlarm() {
        final String KEY_VIBRATE = "vibrateEnabled";

        // Check if vibration is enabled
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean vibrationEnabled = sharedPref.getBoolean(KEY_VIBRATE, true);

        // Get vibrator service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrationEnabled) {
            // Start vibration with pattern
            long[] vibratePattern = new long[]{0, 500, 1000};
            VibrationEffect effect = VibrationEffect.createWaveform(vibratePattern, 0);
            v.vibrate(effect);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
            // Stop vibration
            v.cancel();
        }
    }

    // Return null here as bound service is not used
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
