package com.pk.alarmclock.alarm.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarm_table")
public class AlarmEntity {

    // ID used to disable / enable / delete alarms
    @PrimaryKey
    private int mAlarmId;

    // Trigger time for alarm
    private long mAlarmTime;
    private boolean mAlarmEnabled;

    public AlarmEntity(long alarmTime, int alarmId, boolean alarmEnabled) {
        this.mAlarmTime = alarmTime;
        this.mAlarmId = alarmId;
        this.mAlarmEnabled = alarmEnabled;
    }

    public int getAlarmId() {
        return mAlarmId;
    }

    public long getAlarmTime() {
        return mAlarmTime;
    }

    public boolean getAlarmEnabled() {
        return mAlarmEnabled;
    }
}
