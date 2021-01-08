package com.pk.alarmzy.misc;

public class Utils {

    public static String getFormattedNextAlarmTime(long time) {

        final long mills = time - System.currentTimeMillis();
        final int hour = (int) mills / (1000 * 60 * 60);
        final int min = (int) (mills / (1000 * 60)) % 60;

        if (hour == 0 && min < 1)
            return "less than a minute from now";
        else if (hour == 0)
            return min + " minutes from now";

        return hour + " hours and " + min + " minutes from now";
    }
}
