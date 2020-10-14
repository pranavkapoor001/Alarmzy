package com.pk.alarmclock.alarm;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ncorti.slidetoact.SlideToActView;
import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.db.AlarmEntity;
import com.pk.alarmclock.alarm.db.AlarmRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmTriggerActivity extends AppCompatActivity {

    SlideToActView btnDismissAlarm, btnSnoozeAlarm;
    TextView alarmTime, alarmTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_trigger);

        alarmTime = findViewById(R.id.trigger_alarm_time);
        alarmTitle = findViewById(R.id.trigger_alarm_title);

        Intent intent = getIntent();
        final int alarmId = intent.getExtras().getInt("alarmIdKey", -1);
        Log.e("AlarmTriggerActivity", "Got alarmIdKey: " + alarmId);

        btnDismissAlarm = findViewById(R.id.btn_dismiss_alarm);
        btnSnoozeAlarm = findViewById(R.id.btn_snooze_alarm);

        Application app = AlarmTriggerActivity.this.getApplication();
        final AlarmRepository ar = new AlarmRepository(app);

        final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(1);
        databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                AlarmEntity currentEntity = ar.getAlarm(alarmId);
                /* If alarmId is not matched in db and daysOfRepeat is null
                 * Then it is a child alarm
                 */
                try {
                    Boolean[] daysOfRepeat = currentEntity.getDaysOfRepeatArr();
                    // Disable toggle if alarm is not recurring type
                    if (!daysOfRepeat[DaysOfWeek.IsRECURRING])
                        ar.updateAlarmStatus(alarmId, false);

                    displayInfo(currentEntity);

                } catch (NullPointerException e) {
                    Log.e("AlarmTrig: ", "Array is null, This is a child alarm");

                    try {
                        // Get today's day of week
                        Calendar todayCal = Calendar.getInstance();
                        int dayToday = todayCal.get(Calendar.DAY_OF_WEEK);


                        /* Since child alarms are scheduled
                         * with ID as parent id + dayToday(Calendar.DAY_OF_WEEK)
                         * sub dayToday from received id to get parent alarm id
                         * which is stored in db
                         */
                        int parentAlarmId = alarmId - dayToday;

                        AlarmEntity parentEntity = ar.getAlarm(parentAlarmId);
                        displayInfo(parentEntity);
                    } catch (NullPointerException e1) {
                        Log.e("AlarmTrig: ", "This is a snoozed alarm");

                        // Simply show current time and "Snoozed Alarm"

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                                Locale.getDefault());
                        String formattedTime = sdf.format(System.currentTimeMillis());
                        alarmTime.setText(formattedTime);

                        alarmTitle.setText(R.string.snoozed_alarm);

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
            public void onSlideComplete(SlideToActView slideToActView) {
                // Stop service and finish this activity
                Intent intent = new Intent(AlarmTriggerActivity.this, AlarmService.class);
                stopService(intent);
                finish();
            }
        });

        // Snooze Alarm
        btnSnoozeAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                // Create new snooze alarm
                AlarmHelper ah = new AlarmHelper();
                ah.snoozeAlarm();

                // Stop service and finish this activity
                Intent intent = new Intent(AlarmTriggerActivity.this, AlarmService.class);
                stopService(intent);
                finish();
            }
        });
    }

    // Display alarmTime and alarmTitle
    public void displayInfo(AlarmEntity alarmEntity) {
        // Get alarm time
        long alarmTimeInMillis = alarmEntity.getAlarmTime();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        String formattedTime = sdf.format(alarmTimeInMillis);
        alarmTime.setText(formattedTime);

        /* Get alarm title
         * If alarmTitle is "Alarm Title" (User didn't set any custom title)
         * then show title as "Alarm"
         */
        if (alarmEntity.getAlarmTitle().trim().equals(this.getResources().getString(R.string.alarm_title)))
            alarmTitle.setText(R.string.alarm);
        else
            alarmTitle.setText(alarmEntity.getAlarmTitle());
    }
}
