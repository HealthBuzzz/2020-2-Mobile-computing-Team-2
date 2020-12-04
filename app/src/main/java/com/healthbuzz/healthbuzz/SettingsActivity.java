package com.healthbuzz.healthbuzz;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class SettingsActivity extends AppCompatActivity {

    public SwitchPreference noBotherPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home || item.getItemId() == R.id.homeAsUp) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            bindSummaryValue(findPreference("time_interval_water"));
            bindSummaryValue(findPreference("time_interval_stretch"));
            bindSummaryValue(findPreference("sound"));
        }
    }

    private static void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static final Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof EditTextPreference) {
                preference.setSummary(stringValue);
                ((EditTextPreference) preference).setText(stringValue);
                if (stringValue == "")
                    stringValue = "20";
                int intValue = Integer.parseInt(stringValue);
                String key = preference.getKey();
                switch (key) {
                    case "time_interval_water":
                        RealtimeModel.INSTANCE.getWater_time_left().postValue((long) intValue * 60);
                        break;
                    case "time_interval_stretch":
                        RealtimeModel.INSTANCE.getStretching_time_left().postValue((long) intValue * 60);
                        break;
                }
            } else if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                switch (index) {
                    case 0:
                        SensorService.setSound("No Sound");
                        break;
                    case 1:
                        SensorService.setSound("Buzz");
                        break;
                    case 2:
                        SensorService.setSound("Sound");
                        break;
                }
                preference
                        .setSummary(index >= 0 ? listPreference.getEntries()[index]
                                : null);
            }
            return true;
        }
    };
}