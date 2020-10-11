package com.pk.alarmclock.alarm;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.db.AlarmEntity;
import com.pk.alarmclock.alarm.db.AlarmRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmTriggerActivity extends AppCompatActivity {

    Button btnDismissAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_trigger);

        Intent intent = getIntent();
        final int alarmId = intent.getExtras().getInt("alarmIdKey", -1);
        Log.e("AlarmTriggerActivity", "Got alarmIdKey: " + alarmId);

        btnDismissAlarm = findViewById(R.id.btn_dismiss_alarm);

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

                } catch (NullPointerException e) {
                    Log.e("AlarmTrig: ", "Array is null, This is a child alarm");
                }
            }
        });

        btnDismissAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Stop service and finish this activity
                Intent intent = new Intent(AlarmTriggerActivity.this, AlarmService.class);
                stopService(intent);
                finish();
            }
        });
    }
}
