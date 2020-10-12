package com.pk.alarmclock.alarm;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.db.AlarmEntity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class AlarmRecViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static String TAG = "PK:AlarmRecViewAdapter";
    TextView tvAlarmTime;
    EditText etAlarmTitle;
    SwitchCompat switchAlarmEnabled;
    ImageButton ibAlarmDelete, ibShowRepeat, ibHideRepeat;
    AlarmEntity currentItem;
    String formattedTime;
    long alarmTimeInMillis;
    AlarmHelper ah;
    MaterialCheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    LinearLayout repeatDaysLayout;

    public AlarmRecViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAlarmTime = itemView.findViewById(R.id.item_alarm_time);
        //etAlarmTitle = itemView.findViewById(R.id.item_alarm_title);
        switchAlarmEnabled = itemView.findViewById(R.id.item_alarm_enabled);
        ibAlarmDelete = itemView.findViewById(R.id.item_alarm_delete);
        ibShowRepeat = itemView.findViewById(R.id.item_alarm_show_repeat);
        ibHideRepeat = itemView.findViewById(R.id.item_alarm_hide_repeat);
        repeatDaysLayout = itemView.findViewById(R.id.repeat_days_layout);
        cbMon = itemView.findViewById(R.id.cb_monday);
        cbTue = itemView.findViewById(R.id.cb_tuesday);
        cbWed = itemView.findViewById(R.id.cb_wednesday);
        cbThu = itemView.findViewById(R.id.cb_thursday);
        cbFri = itemView.findViewById(R.id.cb_friday);
        cbSat = itemView.findViewById(R.id.cb_saturday);
        cbSun = itemView.findViewById(R.id.cb_sunday);

        // Set onClickListeners
        switchAlarmEnabled.setOnClickListener(this);
        ibAlarmDelete.setOnClickListener(this);
        cbMon.setOnClickListener(this);
        cbTue.setOnClickListener(this);
        cbWed.setOnClickListener(this);
        cbThu.setOnClickListener(this);
        cbFri.setOnClickListener(this);
        cbSat.setOnClickListener(this);
        cbSun.setOnClickListener(this);
        ibShowRepeat.setOnClickListener(this);
        ibHideRepeat.setOnClickListener(this);

        repeatDaysLayout.setVisibility(View.GONE);
    }

    public void bindTo(AlarmEntity currentItem) {
        this.currentItem = currentItem;

        // Get time from milliSeconds to format: 08:30 PM
        alarmTimeInMillis = currentItem.getAlarmTime();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        formattedTime = sdf.format(alarmTimeInMillis);
        Boolean[] daysOfRepeatArr = currentItem.getDaysOfRepeatArr();
        Log.e(TAG, "Array: " + Arrays.toString(daysOfRepeatArr));
        // Tick checkbox if child alarm for sunday is enabled
        if (daysOfRepeatArr[DaysOfWeek.IsRECURRING]) {
            for (int i = 1; i < daysOfRepeatArr.length; i++) {
                // this should be checked
                if (daysOfRepeatArr[i]) {
                    switch (i) {
                        case DaysOfWeek.SUNDAY:
                            cbSun.setChecked(true);
                            break;
                        case DaysOfWeek.MONDAY:
                            cbMon.setChecked(true);
                            break;
                        case DaysOfWeek.TUESDAY:
                            cbTue.setChecked(true);
                            break;
                        case DaysOfWeek.WEDNESDAY:
                            cbWed.setChecked(true);
                            break;
                        case DaysOfWeek.THURSDAY:
                            cbThu.setChecked(true);
                            break;
                        case DaysOfWeek.FRIDAY:
                            cbFri.setChecked(true);
                            break;
                        case DaysOfWeek.SATURDAY:
                            cbSat.setChecked(true);
                            break;
                        default:
                            // what now
                    }
                }
            }
        } else {
            cbSun.setChecked(false);
            cbMon.setChecked(false);
            cbTue.setChecked(false);
            cbWed.setChecked(false);
            cbThu.setChecked(false);
            cbFri.setChecked(false);
            cbSat.setChecked(false);
        }

        tvAlarmTime.setText(formattedTime);
        switchAlarmEnabled.setChecked(currentItem.getAlarmEnabled());
        Log.e(TAG, "bindTo Called");
    }

    @Override
    public void onClick(View v) {
        ah = new AlarmHelper();
        AlarmEntity currentEntity = new AlarmEntity(currentItem.getAlarmTime(),
                currentItem.getAlarmId(), currentItem.getAlarmEnabled(),
                currentItem.getDaysOfRepeatArr());
        switch (v.getId()) {
            case R.id.item_alarm_enabled:
                if (!switchAlarmEnabled.isChecked()) {
                    ah.cancelAlarm(currentEntity, false, true, -1);
                    Snackbar.make(v, "Alarm for " + formattedTime + " disabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    ah.oldAlarmId = currentItem.getAlarmId();
                    ah.reEnableAlarm(currentEntity);
                    Snackbar.make(v, "Alarm Set for " + formattedTime, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case R.id.item_alarm_delete:
                // pass alarm id and true to delete: if false then just disable alarm
                ah.cancelAlarm(currentEntity, true, true, -1);
                Snackbar.make(v, "Alarm Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case R.id.item_alarm_show_repeat:
                repeatDaysLayout.setVisibility(View.VISIBLE);
                ibShowRepeat.setVisibility(View.GONE);
                ibHideRepeat.setVisibility(View.VISIBLE);
                break;
            case R.id.item_alarm_hide_repeat:
                repeatDaysLayout.setVisibility(View.GONE);
                ibShowRepeat.setVisibility(View.VISIBLE);
                ibHideRepeat.setVisibility(View.GONE);
                break;
            case R.id.cb_sunday:
                if (cbSun.isChecked())
                    ah.repeatingAlarm(currentEntity, DaysOfWeek.SUNDAY);
                else
                    ah.cancelAlarm(currentEntity, false, false, DaysOfWeek.SUNDAY);
                break;
            case R.id.cb_monday:
                if (cbMon.isChecked())
                    ah.repeatingAlarm(currentEntity, DaysOfWeek.MONDAY);
                else
                    ah.cancelAlarm(currentEntity, false, false, DaysOfWeek.MONDAY);
                break;
            case R.id.cb_tuesday:
                if (cbTue.isChecked())
                    ah.repeatingAlarm(currentEntity, DaysOfWeek.TUESDAY);
                else
                    ah.cancelAlarm(currentEntity, false, false, DaysOfWeek.TUESDAY);
                break;
            case R.id.cb_wednesday:
                if (cbWed.isChecked())
                    ah.repeatingAlarm(currentEntity, DaysOfWeek.WEDNESDAY);
                else
                    ah.cancelAlarm(currentEntity, false, false, DaysOfWeek.WEDNESDAY);
                break;
            case R.id.cb_thursday:
                if (cbThu.isChecked())
                    ah.repeatingAlarm(currentEntity, DaysOfWeek.THURSDAY);
                else
                    ah.cancelAlarm(currentEntity, false, false, DaysOfWeek.THURSDAY);
                break;
            case R.id.cb_friday:
                if (cbFri.isChecked())
                    ah.repeatingAlarm(currentEntity, DaysOfWeek.FRIDAY);
                else
                    ah.cancelAlarm(currentEntity, false, false, DaysOfWeek.FRIDAY);
                break;
            case R.id.cb_saturday:
                if (cbSat.isChecked())
                    ah.repeatingAlarm(currentEntity, DaysOfWeek.SATURDAY);
                else
                    ah.cancelAlarm(currentEntity, false, false, DaysOfWeek.SATURDAY);
                break;
        }
        // TODO: Remove bloat code
        // TODO: Add cb to show repeating state, show these cb only if repeating is enabled
    }
}
