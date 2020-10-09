package com.pk.alarmclock.alarm;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.db.AlarmEntity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AlarmRecViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static String TAG = "PK:AlarmRecViewAdapter";
    TextView tvAlarmTime;
    EditText etAlarmTitle;
    SwitchCompat switchAlarmEnabled;
    ImageButton ibAlarmDelete;
    AlarmEntity currentItem;
    String formattedTime;
    long alarmTimeInMillis;
    AlarmHelper ah;

    public AlarmRecViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAlarmTime = itemView.findViewById(R.id.item_alarm_time);
        etAlarmTitle = itemView.findViewById(R.id.item_alarm_title);
        switchAlarmEnabled = itemView.findViewById(R.id.item_alarm_enabled);
        ibAlarmDelete = itemView.findViewById(R.id.item_alarm_delete);

        // Set onClickListeners
        switchAlarmEnabled.setOnClickListener(this);
        ibAlarmDelete.setOnClickListener(this);
    }

    public void bindTo(AlarmEntity currentItem) {
        this.currentItem = currentItem;
        ah = new AlarmHelper();

        // Get time from milliSeconds to format: 08:30 PM
        alarmTimeInMillis = currentItem.getAlarmTime();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        formattedTime = sdf.format(alarmTimeInMillis);

        tvAlarmTime.setText(formattedTime);
        switchAlarmEnabled.setChecked(currentItem.getAlarmEnabled());
        Log.e(TAG, "bindTo Called");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_alarm_enabled:
                if (!switchAlarmEnabled.isChecked()) {
                    ah.cancelAlarm(currentItem.getAlarmId(), false);
                    Snackbar.make(v, "Alarm for " + formattedTime + " disabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    ah.oldAlarmId = currentItem.getAlarmId();
                    ah.reEnableAlarm(alarmTimeInMillis);
                    Snackbar.make(v, "Alarm Set for " + formattedTime, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case R.id.item_alarm_delete:
                int alarmIdOnPos = currentItem.getAlarmId();
                // pass alarm id and true to delete: if false then just disable alarm
                ah.cancelAlarm(alarmIdOnPos, true);
                Snackbar.make(v, "Alarm Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }
    }
}
