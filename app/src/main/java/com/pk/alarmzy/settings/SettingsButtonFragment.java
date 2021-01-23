package com.pk.alarmzy.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.pk.alarmzy.R;
import com.pk.alarmzy.misc.Constants;

public class SettingsButtonFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.buttonpreference, rootKey);

        /* Volume button dynamic summary */
        final ListPreference volumeBtnPref = findPreference("volume_btn_action");
        if (volumeBtnPref != null) {
            volumeBtnPref.setSummaryProvider(new Preference.SummaryProvider<ListPreference>() {
                private String volBtnText = "";

                @Override
                public CharSequence provideSummary(ListPreference preference) {
                    switch (preference.getValue()) {
                        case Constants.ACTION_DO_NOTHING:
                            volBtnText = "Do nothing";
                            break;
                        case Constants.ACTION_MUTE:
                            volBtnText = "Mute";
                            break;
                        case Constants.ACTION_DISMISS:
                            volBtnText = "Dismiss";
                            break;
                        case Constants.ACTION_SNOOZE:
                            volBtnText = "Snooze";
                            break;
                    }
                    volBtnText = volBtnText + " when volume button is pressed";
                    return volBtnText;
                }
            });
        }

        /* Power button dynamic summary */
        final ListPreference powerBtnPref = findPreference("power_btn_action");
        if (powerBtnPref != null) {
            powerBtnPref.setSummaryProvider(new Preference.SummaryProvider<ListPreference>() {
                private String powerBtnText = "";

                @Override
                public CharSequence provideSummary(ListPreference preference) {
                    switch (preference.getValue()) {
                        case Constants.ACTION_DO_NOTHING:
                            powerBtnText = "Do nothing";
                            break;
                        case Constants.ACTION_MUTE:
                            powerBtnText = "Mute";
                            break;
                        case Constants.ACTION_DISMISS:
                            powerBtnText = "Dismiss";
                            break;
                        case Constants.ACTION_SNOOZE:
                            powerBtnText = "Snooze";
                            break;
                    }
                    powerBtnText = powerBtnText + " when volume button is pressed";
                    return powerBtnText;
                }
            });
        }
    }
}
