package com.pk.alarmzy.alarm.db;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Converter {

    // Used by AlarmEntity Boolean[] mDaysOfWeek

    @TypeConverter
    public static Boolean[] fromString(String value) {
        Type listType = new TypeToken<Boolean[]>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromBoolean(Boolean[] list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

}
