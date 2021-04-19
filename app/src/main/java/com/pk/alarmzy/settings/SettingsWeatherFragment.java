package com.pk.alarmzy.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.pk.alarmzy.R;
import com.pk.alarmzy.Utils.Constants.PreferenceKeys;
import com.pk.alarmzy.Utils.LocationUtils;

public class SettingsWeatherFragment extends PreferenceFragmentCompat {

    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.weatherpreference, rootKey);

        final Preference weatherLocationPref = findPreference(PreferenceKeys.KEY_WEATHER_LOCATION);
        if (weatherLocationPref == null || activity == null)
            return;

        weatherLocationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LocationUtils(getActivity()).getLocation();
                return false;
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Get parent activity to be used with LocationUtils
        if (context instanceof Activity)
            activity = (Activity) context;
    }
}
