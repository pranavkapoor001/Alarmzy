package com.pk.alarmzy.weather;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private static Retrofit instance;

    public static Retrofit getInstance() {

        if (instance != null)
            return instance;

        return new Retrofit.Builder()
                .baseUrl(WeatherResponse.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
