package com.pk.alarmclock.alarm.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "alarm_table")
public class AlarmEntity {

    // ID used to disable / enable / delete alarms
    @PrimaryKey
    private int mAlarmId;

    // Trigger time for alarm
    private long mAlarmTime;
    private boolean mAlarmEnabled;

    @TypeConverters({Converter.class})
    private Boolean[] mDaysOfRepeatArr;

    public AlarmEntity(long alarmTime, int alarmId, boolean alarmEnabled, Boolean[] daysOfRepeatArr) {
        this.mAlarmTime = alarmTime;
        this.mAlarmId = alarmId;
        this.mAlarmEnabled = alarmEnabled;
        this.mDaysOfRepeatArr = daysOfRepeatArr;
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

    public Boolean[] getDaysOfRepeatArr() {
        return mDaysOfRepeatArr;
    }
}
