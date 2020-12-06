package com.healthbuzz.healthbuzz;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BlActivity extends WearableActivity {

    private TextView textView;
    Button talkButton;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bl);
        textView =  findViewById(R.id.text);
        talkButton =  findViewById(R.id.talkClick);




        //Create an OnClickListener//

        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String onClickMessage = "coyot in the house..." + sentMessageNumber++;
                textView.setText(onClickMessage);

                //Use the same path//

                String datapath = "/my_path";
                new SendMessage(datapath, onClickMessage).start();

            }
        });

        //Register to receive local broadcasts, which we'll be creating in the next step//

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

    }

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                //initialize();
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //checkPermissions();
        checkPermission("android.permission.WRITE_EXTERNAL_STORAGE" , 103);
        checkPermission("android.permission.BODY_SENSORS" , 104);

    }
    public void checkPermission(String permission, int requestCode)
    {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                BlActivity.this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            BlActivity.this,
                            new String[] { permission },
                            requestCode);
        }
        else {
            Toast
                    .makeText(BlActivity.this,
                            "Permission already granted " +requestCode,
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public static boolean realTimeFlag=false;
    public void enableCheckBox_onClick(View v){

        CheckBox chb=(CheckBox)v;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //realTimeFlag = preferences.getBoolean("smartwatch_realtime_sensor_reading" , false);



        if(chb.isChecked()){
            preferences.edit().putBoolean("smartwatch_realtime_sensor_reading" , true).apply();
            realTimeFlag=true;
        }else{
            preferences.edit().putBoolean("smartwatch_realtime_sensor_reading" , false).apply();
            realTimeFlag=false;
        }

    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        //Display the following when a new message is received//

            String onMessageReceived = intent.getStringExtra("command"); //"I just received a message from the handheld " + receivedMessageNumber++;
            textView.setText(onMessageReceived);

        }
    }

    class SendMessage extends Thread {
        String path;
        String message;

        //Constructor for sending information to the Data Layer//

        SendMessage(String p, String m) {
            path = p;
            message = m;
        }

        public void run() {
            //Retrieve the connected devices//
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                //Block on a task and get the result synchronously//
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {

                    //Send the message///
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(BlActivity.this).sendMessage(node.getId(), path, message.getBytes());
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
    }
}