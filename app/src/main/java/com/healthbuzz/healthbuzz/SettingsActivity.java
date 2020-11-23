package com.healthbuzz.healthbuzz;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatActivity {

    public SwitchPreference noBotherPref;

    public static Context mainContext;
    public static Context actContext;



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
            findPreference("smartwatch_realtime_sensor_reading").setOnPreferenceChangeListener(listener);
        }
    }



    private static void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    public Context getContx(){
        return getApplicationContext();

    }
    public static String filename ;
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
            } else if (preference instanceof ListPreference){
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
            }else if (preference instanceof CheckBoxPreference) {
                if(preference.getKey().equals("smartwatch_realtime_sensor_reading")){
                    if((boolean)newValue){
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
        class SendMessage extends Thread {
            String path;
            String message;

            private Thread predecessor;

            //Constructor for sending information to the Data Layer//

            SendMessage(String p, String m) {
                path = p;
                message = m;
            }

            public void run() {

                if (predecessor != null) {
                    try {
                        predecessor.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                //Retrieve the connected devices//
                Task<List<Node>> nodeListTask =
                        Wearable.getNodeClient(mainContext).getConnectedNodes();
                try {
                    //Block on a task and get the result synchronously//
                    List<Node> nodes = Tasks.await(nodeListTask);
                    for (Node node : nodes) {

                        //Send the message///
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(actContext).sendMessage(node.getId(), path, message.getBytes());
                        try {
                            Integer result = Tasks.await(sendMessageTask);
                            //Handle the errors//
                        } catch (ExecutionException exception) {

                            //TO DO//
                        } catch (InterruptedException exception) {
                        }
                    }
                } catch (ExecutionException exception) {
                    //TO DO//
                } catch (InterruptedException exception) {
                    //TO DO//
                }
            }

            public void setPredecessor(Thread t) {
                this.predecessor = t;
            }




        }
    };



}