package com.pk.alarmclock.alarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AlarmRecViewAdapter extends RecyclerView.Adapter<AlarmRecViewAdapter.AlarmViewHolder> {

    private static String TAG = "PK:AlarmRecViewAdapter";
    List<AlarmEntity> mAlarmDataList = new ArrayList<>();
    private Context context;

    public void setAlarms(List<AlarmEntity> alarmDataList) {
        this.mAlarmDataList = alarmDataList;
    }

    // Gets current position for ItemTouchHelper (Drag to del)
    public int getAlarmIdRecView(int position) {
        // Return alarmId for selected alarm
        final AlarmEntity currentItem = mAlarmDataList.get(position);
        return currentItem.getAlarmId();
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.alarm_item, parent, false);
        AlarmViewHolder vh = new AlarmViewHolder(itemView);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final AlarmViewHolder holder, final int position) {
        // Get hold of currentItem in ArrayData class
        final AlarmEntity currentItem = mAlarmDataList.get(position);
        final AlarmHelper ah = new AlarmHelper();

        // Gets time in format from milliSeconds to format: 08:30 PM
        // and its properly ordered in recyclerView!
        final long alarmTimeInMillis = currentItem.getAlarmTime();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        final String formatted = sdf.format(alarmTimeInMillis);

        holder.tvAlarmTime.setText(formatted);
        holder.switchAlarmEnabled.setChecked(currentItem.getAlarmEnabled());

        // Enable / Disable Alarm on switch toggle
        holder.switchAlarmEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.switchAlarmEnabled.isChecked()) {
                    ah.cancelAlarm(currentItem.getAlarmId(), false);
                    Snackbar.make(view, "Alarm for " + formatted + " disabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    ah.oldAlarmId = currentItem.getAlarmId();
                    ah.reEnableAlarm(alarmTimeInMillis);
                    Snackbar.make(view, "Alarm Set for " + formatted, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        // Delete Alarm on ImageButton Click
        holder.ibAlarmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int alarmIdOnPos = currentItem.getAlarmId();
                // pass alarm id and true to delete: if false then just disable alarm
                ah.cancelAlarm(alarmIdOnPos, true);

                Snackbar.make(view, "Alarm Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        /* TODO: Implement way to show actual AlarmTitle
         * TODO: Also add in AlarmEntity
         */
    }

    @Override
    public int getItemCount() {
        return mAlarmDataList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlarmTime;
        EditText etAlarmTitle;
        SwitchCompat switchAlarmEnabled;
        ImageButton ibAlarmDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlarmTime = itemView.findViewById(R.id.item_alarm_time);
            etAlarmTitle = itemView.findViewById(R.id.item_alarm_title);
            switchAlarmEnabled = itemView.findViewById(R.id.item_alarm_enabled);
            ibAlarmDelete = itemView.findViewById(R.id.item_alarm_delete);
        }
    }
}