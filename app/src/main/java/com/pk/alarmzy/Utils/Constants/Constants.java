package com.pk.alarmzy.Utils.Constants;

public class Constants {

    /* int id for all days to use anywhere across package
     * Sunday is 1 since Calendar.SUNDAY = 1
     * and Monday-Saturday(1-7)
     */
    public static final int IsRECURRING = 0;
    public static final int SUNDAY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 3;
    public static final int WEDNESDAY = 4;
    public static final int THURSDAY = 5;
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 7;

    /* Action button keys
     * Used to decide action on power or volume button press while alarm is ringing
     */
    public static final String ACTION_DO_NOTHING = "com.pk.alarmzy.DO_NOTHING";
    public static final String ACTION_MUTE = "com.pk.alarmzy.MUTE";
    public static final String ACTION_DISMISS = "com.pk.alarmzy.DISMISS";
    public static final String ACTION_SNOOZE = "com.pk.alarmzy.SNOOZE";

}
