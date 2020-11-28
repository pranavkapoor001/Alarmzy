package com.pk.alarmzy.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.pk.alarmzy.R;
import com.pk.alarmzy.misc.MyApplication;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_ALARM_ALERT_RINGTONE = 10;
    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("ringtone")) {
            // Get current alarm ringtone to show as default
            Uri existingAlarmAlertUri = Uri.parse(prefs.getString(
                    "ringtone", Settings.System.DEFAULT_ALARM_ALERT_URI.toString()));

            // Launch ringtone picker
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingAlarmAlertUri);
            startActivityForResult(intent, REQUEST_CODE_ALARM_ALERT_RINGTONE);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_CODE_ALARM_ALERT_RINGTONE &&
                data != null) {
            // Write selected alarm ringtone to shared preferences
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone != null)
                prefs.edit().putString("ringtone", ringtone.toString()).apply();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}