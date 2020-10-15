package com.pk.alarmclock.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.pk.alarmclock.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}