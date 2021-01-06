package com.pk.alarmzy.alarm.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.pk.alarmzy.alarm.AlarmTriggerActivity;
import com.pk.alarmzy.alarm.helper.NotificationHelper;

// Note: Define service in AndroidManifest.xml

public class AlarmService extends Service {
    private static final String TAG = "AlarmService";
    private Vibrator v;
    private MediaPlayer player;
    private Handler handler;
    private Runnable crescendoRunnable;
    private SharedPreferences sharedPref;


    //----------------------------- Lifecycle methods --------------------------------------------//

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
        if (intent != null && intent.hasExtra("alarmIdKey"))
            alarmId = intent.getIntExtra("alarmIdKey", -1);

        Log.i(TAG, "Got alarmIdKey: " + alarmId);


        // deliver notification with alarmId to disable toggle when alarm is dismissed
        startForeground(1, new NotificationHelper(this, alarmId).deliverNotification());

        /* Start activity to dismiss / snooze alarm for API < 29(Q)
         * For API >= 29 full-screen pending intent defined in notification will launched
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Intent alarmActivityIntent = new Intent(this, AlarmTriggerActivity.class);
            alarmActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            alarmActivityIntent.putExtra("alarmIdKey", alarmId);
            startActivity(alarmActivityIntent);
        }

        // Get Settings shared preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        playAlarm();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop crescendoRunnable
        if (handler != null && crescendoRunnable != null)
            handler.removeCallbacks(crescendoRunnable);

        // Release player
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


    //----------------------------- AlarmPlay methods --------------------------------------------//

    public void playAlarm() {

        Log.e(TAG, "AlarmDeliver Called");

        // Get the default alarm sound
        Uri alarmDefaultSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        // Get user selected alarm ringtone
        Uri alarmUserSelectedSound = Uri.parse(sharedPref.getString("ringtone", alarmDefaultSound.toString()));
        // init media player
        player = new MediaPlayer();

        // setDataSource() and prepare() can throw an Exception
        try {
            player.setDataSource(this, alarmUserSelectedSound);

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
                startPlayerOnPrepared();
            }
        });
        vibrateAlarm();
    }

    public void startPlayerOnPrepared() {
        // Max volume of MediaPlayer
        final float MAX_VOLUME = 1.0f;
        final String KEY_CRESCENDO_TIME = "crescendoTime";

        // Get crescendo time
        String crescendoTimeStr = sharedPref.getString(KEY_CRESCENDO_TIME, "0");

        /* Set default value to 0
         * Add null check to avoid warning
         * and npe later
         */
        float crescendoTime = 0;
        if (crescendoTimeStr != null)
            crescendoTime = Integer.parseInt(crescendoTimeStr);


        /* Gradually increasing alarm volume is off
         * Simply start the player and return
         */
        if (crescendoTime == 0) {
            player.start();
            Log.i(TAG, "startPlayerOnPrepared: Not Gradual");
            return;
        }

        // Gradually increasing alarm vol is enabled
        Log.i(TAG, "startPlayerOnPrepared: Gradual");

        // Initially mute the alarm
        player.setVolume(0, 0);

        // Create new handler background thread
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        // Increase volume by this every second
        final float incrementPerSecond = 1 / crescendoTime;

        // Increase volume step every
        crescendoRunnable = new Runnable() {
            // Start with 0 volume
            float currentVol = 0.0f;

            @Override
            public void run() {

                /* Return if media player is not initialized
                 *
                 * This may happen when service is being destroyed but
                 * crescendoRunnable is still active.
                 * set volume could throw an NPE
                 */
                if (player == null) {
                    Log.i(TAG, "crescendoRunnable: Not playing, player uninitialized");
                    return;
                }

                /* Increase volume per second by incrementPerSecond value
                 * until we reach max value
                 */
                if (currentVol < MAX_VOLUME) {
                    currentVol = currentVol + incrementPerSecond;
                    player.setVolume(currentVol, currentVol);
                    handler.postDelayed(this, 1000); // 1 Second
                }
            }
        };

        // Start player then post runnable for the first time to increment volume in steps
        player.start();
        handler.post(crescendoRunnable);
    }

    public void vibrateAlarm() {
        final String KEY_VIBRATE = "vibrateEnabled";

        // Check if vibration is enabled
        boolean vibrationEnabled = sharedPref.getBoolean(KEY_VIBRATE, true);

        // Get vibrator service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrationEnabled) {
            // Start vibration with pattern
            long[] vibratePattern = new long[]{0, 500, 1000};
            VibrationEffect effect;

            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                effect = VibrationEffect.createWaveform(vibratePattern, 0);
                v.vibrate(effect);
            } else {
                v.vibrate(vibratePattern, 0);
            }
        }
    }
}
