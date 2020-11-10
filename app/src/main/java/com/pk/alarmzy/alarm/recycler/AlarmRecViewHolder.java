package com.pk.alarmzy.alarm.recycler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.pk.alarmzy.R;
import com.pk.alarmzy.alarm.db.AlarmEntity;
import com.pk.alarmzy.alarm.db.AlarmRepository;
import com.pk.alarmzy.alarm.helper.AlarmHelper;
import com.pk.alarmzy.misc.DaysOfWeek;
import com.pk.alarmzy.misc.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class AlarmRecViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    // vars
    private static final String TAG = "PK:AlarmRecViewAdapter";
    private AlarmHelper ah;
    private AlarmEntity currentEntity;
    private String formattedTime;

    // UI Components
    private TextView tvAlarmTime;
    private EditText etAlarmTitle;
    private ImageView ivRepeatIcon;
    private LinearLayout repeatDaysLayout;
    private SwitchCompat switchAlarmEnabled;
    private ImageButton ibShowRepeat;
    private ImageButton ibHideRepeat;
    private MaterialCheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;


    public AlarmRecViewHolder(@NonNull View itemView) {
        super(itemView);

        // Find views
        tvAlarmTime = itemView.findViewById(R.id.item_alarm_time);
        etAlarmTitle = itemView.findViewById(R.id.item_alarm_title);
        switchAlarmEnabled = itemView.findViewById(R.id.item_alarm_enabled);
        ImageButton ibAlarmDelete = itemView.findViewById(R.id.item_alarm_delete);
        ibShowRepeat = itemView.findViewById(R.id.item_alarm_show_repeat);
        ibHideRepeat = itemView.findViewById(R.id.item_alarm_hide_repeat);
        ivRepeatIcon = itemView.findViewById(R.id.item_repeat_icon);
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
        etAlarmTitle.setOnClickListener(this);

        repeatDaysLayout.setVisibility(View.GONE);
        ah = new AlarmHelper();
    }

    public void bindTo(AlarmEntity currentItem) {
        this.currentEntity = currentItem;

        // Get time from milliSeconds to format: 08:30 PM
        long alarmTimeInMillis = currentEntity.getAlarmTime();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        formattedTime = sdf.format(alarmTimeInMillis);
        Boolean[] daysOfRepeatArr = currentEntity.getDaysOfRepeatArr();
        Log.i(TAG, "Array: " + Arrays.toString(daysOfRepeatArr));
        // Tick checkbox if child alarm is enabled
        if (daysOfRepeatArr[DaysOfWeek.IsRECURRING]) {
            ivRepeatIcon.setVisibility(View.VISIBLE);
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
            ivRepeatIcon.setVisibility(View.GONE);
            cbSun.setChecked(false);
            cbMon.setChecked(false);
            cbTue.setChecked(false);
            cbWed.setChecked(false);
            cbThu.setChecked(false);
            cbFri.setChecked(false);
            cbSat.setChecked(false);
        }

        tvAlarmTime.setText(formattedTime);
        switchAlarmEnabled.setChecked(currentEntity.getAlarmEnabled());
        etAlarmTitle.setText(currentEntity.getAlarmTitle());
        Log.e(TAG, "bindTo Called");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.item_alarm_enabled:
                if (!switchAlarmEnabled.isChecked()) {
                    ah.cancelAlarm(currentEntity, false, true, -1);
                    Snackbar.make(v, "Alarm for " + formattedTime + " disabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    ah.oldAlarmId = currentEntity.getAlarmId();
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
                etAlarmTitle.setVisibility(View.VISIBLE);
                break;
            case R.id.item_alarm_hide_repeat:
                repeatDaysLayout.setVisibility(View.GONE);
                ibShowRepeat.setVisibility(View.VISIBLE);
                ibHideRepeat.setVisibility(View.GONE);
                etAlarmTitle.setVisibility(View.GONE);
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
            case R.id.item_alarm_title:
                alarmTitleBuilder();
                break;
        }
    }

    public void alarmTitleBuilder() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(itemView.getContext());
        alertBuilder.setTitle("Alarm Title");

        final EditText titleInput = new EditText(MyApplication.getContext());
        titleInput.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        titleInput.setLayoutParams(lp);
        alertBuilder.setView(titleInput);

        // Show soft Keyboard
        titleInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) MyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlarmRepository ar = new AlarmRepository(MyApplication.getContext());

                if (!TextUtils.isEmpty(titleInput.getText())) {
                    etAlarmTitle.setText(titleInput.getText().toString());
                    ar.setAlarmTitle(titleInput.getText().toString(), currentEntity.getAlarmId());
                }
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Nothing
            }
        });

        alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                titleInput.clearFocus();
                // Hide soft Keyboard
                InputMethodManager imm = (InputMethodManager) MyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });
        alertBuilder.show();
    }
}
