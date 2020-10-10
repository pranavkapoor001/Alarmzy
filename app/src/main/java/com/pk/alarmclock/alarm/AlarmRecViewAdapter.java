package com.pk.alarmclock.alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pk.alarmclock.R;
import com.pk.alarmclock.alarm.db.AlarmEntity;

import java.util.ArrayList;
import java.util.List;


public class AlarmRecViewAdapter extends RecyclerView.Adapter<AlarmRecViewHolder> {

    private static String TAG = "PK:AlarmRecViewAdapter";
    List<AlarmEntity> mAlarmDataList = new ArrayList<>();

    public void setAlarms(List<AlarmEntity> alarmDataList) {
        this.mAlarmDataList = alarmDataList;
    }

    // Gets current position for ItemTouchHelper (Drag to del)
    public AlarmEntity getAlarmRecView(int position) {
        // Return alarmId for selected alarm
        final AlarmEntity currentItem = mAlarmDataList.get(position);
        AlarmEntity currentEntity = new AlarmEntity(currentItem.getAlarmTime(),
                currentItem.getAlarmId(), currentItem.getAlarmEnabled(), currentItem.getDaysOfRepeatArr());
        return currentEntity;
    }

    @NonNull
    @Override
    public AlarmRecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.alarm_item, parent, false);
        return new AlarmRecViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AlarmRecViewHolder holder, final int position) {

        // pass currentItem to ViewHolder
        final AlarmEntity currentItem = mAlarmDataList.get(position);
        holder.bindTo(currentItem);
    }

    @Override
    public int getItemCount() {
        return mAlarmDataList.size();
    }

}