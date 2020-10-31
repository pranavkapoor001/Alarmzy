package com.pk.alarmzy.alarm.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.pk.alarmzy.R;
import com.pk.alarmzy.alarm.db.AlarmEntity;


public class AlarmRecViewAdapter extends ListAdapter<AlarmEntity, AlarmRecViewHolder> {

    private static final DiffUtil.ItemCallback<AlarmEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<AlarmEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull AlarmEntity oldItem, @NonNull AlarmEntity newItem) {

            return oldItem.getAlarmId() == newItem.getAlarmId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AlarmEntity oldItem, @NonNull AlarmEntity newItem) {

            /* No need to redraw the view item (Required when data is changed in background
             * and needs to be updated in the UI)
             * Here we update the UI then call any db ops
             *
             * EXCEPT: alarm switch toggle
             *         since it can be disabled in db
             *         when alarm is dismissed from AlarmTriggerActivity
             */
            return oldItem.getAlarmEnabled() == newItem.getAlarmEnabled();
        }
    };
    private static String TAG = "PK:AlarmRecViewAdapter";

    public AlarmRecViewAdapter() {
        super(DIFF_CALLBACK);
    }

    // Gets current position for ItemTouchHelper (Drag to del)
    public AlarmEntity getAlarmRecView(int position) {
        // Return alarmId for selected alarm
        final AlarmEntity currentItem = getItem(position);
        return new AlarmEntity(currentItem.getAlarmTime(),
                currentItem.getAlarmId(), currentItem.getAlarmEnabled(),
                currentItem.getDaysOfRepeatArr(), currentItem.getAlarmTitle());
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
        final AlarmEntity currentItem = getItem(position);
        holder.bindTo(currentItem);
    }
}