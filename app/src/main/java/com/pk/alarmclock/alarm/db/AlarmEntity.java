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
    private String mAlarmTitle;

    @TypeConverters({Converter.class})
    private Boolean[] mDaysOfRepeatArr;

    public AlarmEntity(long alarmTime, int alarmId, boolean alarmEnabled,
                       Boolean[] daysOfRepeatArr, String alarmTitle) {
        this.mAlarmTime = alarmTime;
        this.mAlarmId = alarmId;
        this.mAlarmEnabled = alarmEnabled;
        this.mDaysOfRepeatArr = daysOfRepeatArr;
        this.mAlarmTitle = alarmTitle;
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

    public String getAlarmTitle() {
        return mAlarmTitle;
    }
}
