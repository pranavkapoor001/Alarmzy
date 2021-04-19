package com.pk.alarmzy.weather;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class WeatherViewModel extends AndroidViewModel {

    private final WeatherRepository repo;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        repo = new WeatherRepository();
    }

    public LiveData<WeatherResponse> getWeather() {
        return repo.getWeather();
    }
}
