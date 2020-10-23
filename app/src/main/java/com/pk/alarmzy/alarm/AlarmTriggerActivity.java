package com.pk.alarmzy.alarm;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.ncorti.slidetoact.SlideToActView;
import com.pk.alarmzy.R;
import com.pk.alarmzy.alarm.db.AlarmEntity;
import com.pk.alarmzy.alarm.db.AlarmRepository;
import com.pk.alarmzy.alarm.helper.AlarmHelper;
import com.pk.alarmzy.alarm.helper.NotificationHelper;
import com.pk.alarmzy.alarm.services.AlarmService;
import com.pk.alarmzy.misc.DaysOfWeek;
import com.pk.alarmzy.misc.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmTriggerActivity extends AppCompatActivity {

    private static final String TAG = "AlarmTriggerActivity";
    private SlideToActView btnDismissAlarm, btnSnoozeAlarm;
    private TextView tvAlarmTime, tvAlarmTitle;
    private Handler handler;
    private Runnable silenceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_trigger);

        tvAlarmTime = findViewById(R.id.trigger_alarm_time);
        tvAlarmTitle = findViewById(R.id.trigger_alarm_title);

        Intent intent = getIntent();

        /* This can produce npe
         * Check if key exists then fetch value
         */
        int alarmId = -1;
        if (intent.hasExtra("alarmIdKey"))
            alarmId = intent.getIntExtra("alarmIdKey", -1);

        Log.i(TAG, "onCreate: Got alarmIdKey: " + alarmId);

        btnDismissAlarm = findViewById(R.id.btn_dismiss_alarm);
        btnSnoozeAlarm = findViewById(R.id.btn_snooze_alarm);

        Application app = AlarmTriggerActivity.this.getApplication();
        final AlarmRepository ar = new AlarmRepository(app);

        // Temp var for inner class
        final int finalAlarmId = alarmId;
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final AlarmEntity currentEntity = ar.getAlarm(finalAlarmId);
                /* If alarmId is not matched in db and daysOfRepeat is null
                 * Then it is a child alarm
                 */
                try {
                    Boolean[] daysOfRepeat = currentEntity.getDaysOfRepeatArr();
                    // Disable toggle if alarm is not recurring type
                    if (!daysOfRepeat[DaysOfWeek.IsRECURRING])
                        ar.updateAlarmStatus(finalAlarmId, false);

                    displayInfo(currentEntity);

                } catch (NullPointerException e) {
                    Log.e(TAG, "run: Array is null, This is a child alarm");

                    try {
                        // Get today's day of week
                        Calendar todayCal = Calendar.getInstance();
                        int dayToday = todayCal.get(Calendar.DAY_OF_WEEK);


                        /* Since child alarms are scheduled
                         * with ID as parent id + dayToday(Calendar.DAY_OF_WEEK)
                         * sub dayToday from received id to get parent alarm id
                         * which is stored in db
                         */
                        int parentAlarmId = finalAlarmId - dayToday;

                        AlarmEntity parentEntity = ar.getAlarm(parentAlarmId);

                        /* This is a repeating alarm
                         * Now set this alarm for next week
                         */
                        AlarmHelper ah = new AlarmHelper();
                        ah.repeatingAlarm(parentEntity, dayToday);

                        displayInfo(parentEntity);
                    } catch (NullPointerException e1) {
                        Log.e(TAG, "run: This is a snoozed alarm");

                        // Simply show current time and "Snoozed Alarm"

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                                Locale.getDefault());
                        String formattedTime = sdf.format(System.currentTimeMillis());
                        tvAlarmTime.setText(formattedTime);

                        tvAlarmTitle.setText(R.string.snoozed_alarm);

                        // Cancel notification
                        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotifyManager.cancelAll();
                    }
                }
            }
        });

        // Dismiss Alarm
        btnDismissAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                // Stop service and finish this activity
                stopAlarmService();
            }
        });

        // Snooze Alarm
        btnSnoozeAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                // Create new snooze alarm
                AlarmHelper ah = new AlarmHelper();
                ah.snoozeAlarm();

                stopAlarmService();
            }
        });

        // Check silenceTimeout
        silenceTimeout(alarmId);
    }

    /* Check if silence timeout is greater than 0
     * and deliver missed alarm notification if timeout is exceeded
     */
    public void silenceTimeout(final int alarmId) {
        final String KEY_SILENCE_TIMEOUT = "silenceTimeout";

        // Get silence timeout
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String silenceTimeStr = sharedPref.getString(KEY_SILENCE_TIMEOUT, "0");

        /* Set default and add null check
         * to avoid warning and npe later
         */
        int silenceTimeoutInt = 0;
        if (silenceTimeStr != null)
            silenceTimeoutInt = Integer.parseInt(silenceTimeStr);

        /* If silenceTimeout is set to Never(0)
         * Return from here
         */
        if (silenceTimeoutInt == 0)
            return;

        /* If not dismissed under x minutes
         * Stop alarm
         * Post missed alarm notification
         */
        handler = new Handler(getMainLooper());
        // Create temp final var for inner class
        final int finalSilenceTimeoutInt = silenceTimeoutInt;
        silenceRunnable = new Runnable() {
            @Override
            public void run() {
                // Deliver notification using id
                NotificationHelper nh = new NotificationHelper(getApplicationContext(), alarmId);
                nh.deliverMissedNotification(finalSilenceTimeoutInt);

                stopAlarmService();
            }
        };
        handler.postDelayed(silenceRunnable, silenceTimeoutInt * 60000); // x Minutes * millis
    }

    // Display alarmTime and alarmTitle
    public void displayInfo(final AlarmEntity alarmEntity) {
        // Fetch from db (running on bg thread)
        final long alarmTimeInMillis = alarmEntity.getAlarmTime();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Get alarm time
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                        Locale.getDefault());
                String formattedTime = sdf.format(alarmTimeInMillis);
                tvAlarmTime.setText(formattedTime);

                /* Get alarm title
                 * If alarmTitle is "Alarm Title" (User didn't set any custom title)
                 * then show title as "Alarm"
                 */
                if (alarmEntity.getAlarmTitle().trim().equals(getString(R.string.alarm_title)))
                    tvAlarmTitle.setText(R.string.alarm);
                else
                    tvAlarmTitle.setText(alarmEntity.getAlarmTitle());
            }
        });
    }

    // Stop service and finish activity
    public void stopAlarmService() {
        Intent intent = new Intent(AlarmTriggerActivity.this, AlarmService.class);
        stopService(intent);

        /* Runnable has not yet executed
         * and alarm has been dismissed by user
         * no need to post work now
         */
        if (handler != null && silenceRunnable != null)
            handler.removeCallbacks(silenceRunnable);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
    }
}
