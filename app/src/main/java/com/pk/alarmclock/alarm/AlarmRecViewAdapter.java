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

import com.pk.alarmclock.R;

public class AlarmRecViewAdapter extends RecyclerView.Adapter<AlarmRecViewAdapter.AlarmViewHolder> {

    private static String TAG = "PK:AlarmRecViewAdapter";
    private Context context;

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
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
