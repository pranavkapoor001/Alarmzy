package com.pk.alarmclock.alarm.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlarmEntity alarmEntity);

    @Update
    void update(AlarmEntity alarmEntity);

    // Fetch all alarms in List<> wrapped in liveData to observe
    @Query("SELECT * FROM alarm_table ORDER BY mAlarmTime ASC")
    LiveData<List<AlarmEntity>> getAllAlarms();

    // Fetch all alarms in List<> to ReSchedule()
    @Query(("SELECT * FROM alarm_table"))
    List<AlarmEntity> getAllAlarmsReSched();

    // Pass alarm id to delete from db
    @Query("DELETE FROM alarm_table WHERE mAlarmId = :id")
    void deleteAlarm(int id);

    // Pass alarm id and toggle value (Use to enable / disable toggle when alarm is dismissed)
    @Query("UPDATE alarm_table SET mAlarmEnabled = :isAlarmEnabled WHERE mAlarmId = :id")
    void updateAlarmStatus(int id, boolean isAlarmEnabled);

    // ReEnable Alarm: Update Id and avoid reInsertion
    @Query("UPDATE alarm_table SET mAlarmId=:newAlarmId WHERE mAlarmId=:oldAlarmId")
    void updateAlarmId(int oldAlarmId, int newAlarmId);

    @Query("SELECT * FROM alarm_table WHERE mAlarmId=:id")
    AlarmEntity getAlarm(int id);

    @Query("UPDATE alarm_table SET mAlarmTitle = :alarmTitle WHERE mAlarmId = :alarmId")
    void setAlarmTitle(String alarmTitle, long alarmId);

}
