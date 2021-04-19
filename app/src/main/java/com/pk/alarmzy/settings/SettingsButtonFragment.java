package com.pk.alarmzy.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.pk.alarmzy.R;
import com.pk.alarmzy.Utils.Constants.Constants;

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
                            volBtnText = getString(R.string.do_nothing);
                            break;
                        case Constants.ACTION_MUTE:
                            volBtnText = getString(R.string.mute);
                            break;
                        case Constants.ACTION_DISMISS:
                            volBtnText = getString(R.string.dismiss);
                            break;
                        case Constants.ACTION_SNOOZE:
                            volBtnText = getString(R.string.snooze);
                            break;
                    }
                    volBtnText = volBtnText + " " + getString(R.string.when_volume_button_is_pressed);
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
                            powerBtnText = getString(R.string.do_nothing);
                            break;
                        case Constants.ACTION_MUTE:
                            powerBtnText = getString(R.string.mute);
                            break;
                        case Constants.ACTION_DISMISS:
                            powerBtnText = getString(R.string.dismiss);
                            break;
                        case Constants.ACTION_SNOOZE:
                            powerBtnText = getString(R.string.snooze);
                            break;
                    }
                    powerBtnText = powerBtnText + " " + getString(R.string.when_power_button_is_pressed);
                    return powerBtnText;
                }
            });
        }
    }
}
