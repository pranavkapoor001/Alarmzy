package com.pk.alarmzy.Utils.Constants;

/* All weather preference keys go here
 * Strings here must match with key value used in Settings preference xml or SharedPreferences
 */
public class PreferenceKeys {
    // Weather constants

    /**
     * KEY_WEATHER_ENABLED Settings preference key to check if weather is enabled
     */
    public static final String KEY_WEATHER_ENABLED = "weather_enabled";

    /**
     * KEY_WEATHER_UNIT Settings preference key to save temperature unit (Celsius or Fahrenheit)
     */
    public static final String KEY_WEATHER_UNIT = "weather_unit";

    /**
     * KEY_WEATHER_LOCATION Settings preference key to update location on preference click
     */
    public static final String KEY_WEATHER_LOCATION = "weather_location";

    /**
     * KEY_LAT SharedPreference key to save location latitude
     */
    public static final String KEY_LAT = "location_lat";

    /**
     * KEY_LONGI SharedPreference key to save location longitude
     */
    public static final String KEY_LONGI = "location_longi";


}
