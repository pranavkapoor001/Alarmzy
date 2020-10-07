package com.pk.alarmclock.alarm;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.db.AlarmRepository;

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

        btnDismissAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Directly disable alarm toggle of fired alarm
                Application app = AlarmTriggerActivity.this.getApplication();
                AlarmRepository ar = new AlarmRepository(app);
                ar.updateAlarmStatus(alarmId, false);

                Intent intent = new Intent(AlarmTriggerActivity.this, AlarmService.class);
                stopService(intent);
                finish();
            }
        });
    }
}