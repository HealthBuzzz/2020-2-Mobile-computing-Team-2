package com.healthbuzz.healthbuzz;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
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
                   // BluetoothAdapter BlAdapter=BluetoothAdapter.getDefaultAdapter();
                    if(((CheckBoxPreference)preference).isChecked()){

                        // check if device supports bluetooth
                        if(BlAdapter==null){
                            Toast.makeText(preference.getContext() , "this device Does NOT support Bluetooth!!!" , Toast.LENGTH_LONG).show();
                            return false;
                        }

                        // try to enable blurtotth if it is not enabled
                        if(!BlAdapter.isEnabled()){
                            Intent eintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(eintent, 103);
                        }



                    }else{


                    }

                    return false;
                }
            });

            bindSummaryValue(findPreference("time_interval_water"));
            bindSummaryValue(findPreference("time_interval_stretch"));
        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == 103){
                if(resultCode==0){
                    Toast.makeText(getContext() , "you need to enable bluetoth to use this functionality" , Toast.LENGTH_LONG).show();
                    ((CheckBoxPreference)findPreference("smartwatch")).setChecked(false);
                }else{
                    BluetoothAdapter BlAdapter=BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> pairedDevices = BlAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                        }
                    }
                    // go for device selection
                    Intent i=new Intent(getContext() , SelectBlDeviceActivity.class);
                    startActivity(i);

                }
                Log.e("Y_LOG" , resultCode+"");
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