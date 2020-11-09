package com.healthbuzz.healthbuzz;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

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

            //BL start

            CheckBoxPreference BlActivityCheckBox=findPreference("goToBlActivity");
            BlActivityCheckBox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent i=new Intent(getContext() , BlActivity.class);
                    startActivity(i);

                    return false;
                }
            });


            CheckBoxPreference BlCheckBox=findPreference("smartwatch");
            BluetoothAdapter BlAdapter=BluetoothAdapter.getDefaultAdapter();
            if(BlAdapter.isEnabled()){
                BlCheckBox.setChecked(true);
                //go for device selection
            }else{
                BlCheckBox.setChecked(false);
            }
            BlCheckBox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if(((CheckBoxPreference)preference).isChecked()){

                        // check if device supports bluetooth
                        if(BlAdapter==null){
                            Toast.makeText(preference.getContext() , "this device Does NOT support Bluetooth!!!" , Toast.LENGTH_LONG).show();
                            return false;
                        }

                        // try to enable blurtooth if it is not enabled
                        if(!BlAdapter.isEnabled()){
                            Intent eintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(eintent, 103);
                        }



                    }else{


                    }

                    return false;
                }
            });

            //BL end

            bindSummaryValue(findPreference("time_interval_water"));
            bindSummaryValue(findPreference("time_interval_stretch"));
        }

        // BL2
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == 103){
                if(resultCode==0){
                    Toast.makeText(getContext() , "you need to enable bluetooth to use this functionality" , Toast.LENGTH_LONG).show();
                    ((CheckBoxPreference)findPreference("smartwatch")).setChecked(false);
                }else{
                    // go for device selection
                    Intent i=new Intent(getContext() , SelectBlDeviceActivity.class);
                    startActivity(i);
                }

            }

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
            }
            return false;
        }
    };
}