package com.pk.alarmclock.alarm.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarm_table")
public class AlarmEntity {

    // mAlarmTime is used only for setting alarmTime
    @PrimaryKey
    private long mAlarmTime;

    // ID used to disable / enable / delete alarms
    private int mAlarmId;
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
