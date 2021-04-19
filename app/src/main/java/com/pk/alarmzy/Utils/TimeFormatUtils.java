package com.pk.alarmzy.Utils;

import android.content.Context;

import com.pk.alarmzy.R;
import com.pk.alarmzy.misc.MyApplication;

public class TimeFormatUtils {

    public static String getFormattedNextAlarmTime(long time) {

        Context context = MyApplication.getContext();
        final long mills = time - System.currentTimeMillis();
        final int hour = (int) mills / (1000 * 60 * 60);
        final int min = (int) (mills / (1000 * 60)) % 60;

        if (hour == 0 && min < 1)
            return context.getString(R.string.less_than_a_minute_from_now);
        else if (hour == 0)
            return min + " " + context.getString(R.string.minutes_from_now);

        return hour + " " + context.getString(R.string.hours_and) + " " + min
                + " " + context.getString(R.string.minutes_from_now);
    }
}
