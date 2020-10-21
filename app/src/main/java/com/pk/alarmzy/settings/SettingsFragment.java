package com.pk.alarmzy.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.pk.alarmzy.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}