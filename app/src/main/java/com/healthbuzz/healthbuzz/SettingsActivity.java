package com.healthbuzz.healthbuzz;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.CheckBoxPreference;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

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

        mainContext=getApplicationContext();
        actContext=SettingsActivity.this;

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        SettingsActivity.Receiver messageReceiver = new SettingsActivity.Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);



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

            /*bindSummaryValue(findPreference("time_interval_water"));
            bindSummaryValue(findPreference("time_interval_stretch"));

            bindSummaryValue(findPreference("sound"));*/
            findPreference("activate_drinking").setOnPreferenceChangeListener(listener);

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

            }else if (preference instanceof SwitchPreferenceCompat) {
                if(preference.getKey().equals("activate_drinking")){
                    if((boolean)newValue){

                        /*SendMessage t1=new SendMessage("/my_path" , "_stop");
                        SendMessage t2 = new SendMessage("/my_path" , "_hellowatch");
                        t2.setPredecessor(t1);
                        t1.start();
                        t2.start();*/



                        filename = new SimpleDateFormat("'SensorData'yyyyMMddHHmm'_W_realtime.csv'", Locale.KOREA).format(new Date());
                        SendMessage t1=new SendMessage("/my_path" , "_stop");
                        SendMessage t2 = new SendMessage("/my_path" , "_startrealtime");
                        t2.setPredecessor(t1);
                        t1.start();
                        t2.start();

                    }else{
                        new SendMessage("/my_path" , "_stop").start();
                    }
                }

            }
            return true;
        }
    };



    public class Receiver extends BroadcastReceiver {
        @Override

        public void onReceive(Context context, Intent intent) {

            //String message = "I just received a message from the wearable " + receivedMessageNumber++;
            String message=intent.getStringExtra("message" );

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes
                            // button clicked
                            Log.e("CYT_LOG" , "yes clicked....");
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            Log.e("CYT_LOG" , "no clicked....");
                            break;
                    }
                }
            };

           /* AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();*/

        }
    }


}