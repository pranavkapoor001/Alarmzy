package com.pk.alarmzy.weather;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.pk.alarmzy.BuildConfig;
import com.pk.alarmzy.Utils.Constants.PreferenceKeys;
import com.pk.alarmzy.misc.MyApplication;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WeatherRepository {

    private static final String TAG = "WeatherRepository";
    public static String weatherUnit;
    private final WeatherApi weatherApi;
    private final MutableLiveData<WeatherResponse> weatherResponseMutableLiveData = new MutableLiveData<>();
    private final SharedPreferences sharedPref;

    public WeatherRepository() {
        sharedPref = PreferenceManager.
                getDefaultSharedPreferences(MyApplication.getContext());
        weatherUnit = sharedPref.getString(PreferenceKeys.KEY_WEATHER_UNIT, "metric");

        Retrofit retrofit = RetrofitService.getInstance();
        weatherApi = retrofit.create(WeatherApi.class);
        fetchWeather();
    }

    public LiveData<WeatherResponse> getWeather() {
        return weatherResponseMutableLiveData;
    }

    public void fetchWeather() {
        weatherApi.getWeather(getLatitude(), getLongitude(), weatherUnit, BuildConfig.WEATHER_APIKEY)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                        WeatherResponse weatherResponse = response.body();
                        if (weatherResponse != null) {
                            Log.i(TAG, "onResponse: Temperature: " + weatherResponse.main.temp);
                            weatherResponseMutableLiveData.postValue(weatherResponse);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    private String getLatitude() {
        return sharedPref.getString(PreferenceKeys.KEY_LAT, "");
    }

    private String getLongitude() {
        return sharedPref.getString(PreferenceKeys.KEY_LONGI, "");
    }
}
