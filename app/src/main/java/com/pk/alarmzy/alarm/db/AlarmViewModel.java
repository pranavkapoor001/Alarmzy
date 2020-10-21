package com.pk.alarmzy.alarm.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {

    private AlarmRepository alarmRepository;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        alarmRepository = new AlarmRepository(application);
    }

    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return alarmRepository.getAllAlarms();
    }
}
