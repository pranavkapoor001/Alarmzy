package com.pk.alarmzy.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherResponse> getWeather(@Query("lat") String lat,
                                     @Query("lon") String lon,
                                     @Query("units") String units,
                                     @Query("appid") String apiKey);
}
