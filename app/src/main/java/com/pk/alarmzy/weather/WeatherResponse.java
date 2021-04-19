package com.pk.alarmzy.weather;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    public static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    // 'Weather' is an array in open weather json
    @SerializedName("weather")
    public List<Weather> weatherList;
    public Main main;

    // manually created from json api response
    public static class Main {
        @SerializedName("temp")
        public float temp;
        @SerializedName("temp_min")
        public float tempMin;
        @SerializedName("temp_max")
        public float tempMax;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }
}
