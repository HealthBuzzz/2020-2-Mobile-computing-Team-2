package com.healthbuzz.healthbuzz;


import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;


import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

//Extend WearableListenerService//

public class BlService extends WearableListenerService {


    public static LinkedList<String[]> output=new LinkedList<>();
    public static List<String[]> output2;

    static int windowSize = 1000;

    @Override
    public void onDestroy() {
        Log.e("CYT_LOG" , "destroyed...");

        SharedPreferences sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        boolean continueService = sharedPrefs.getBoolean("activate_drinking", false);
        if(continueService) {
            String datapath = "/my_path";
            new SendMessage(datapath, "_startrealtime").start();
        }
        super.onDestroy();
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

//If the message’s path equals "/my_path"...//

        if (messageEvent.getPath().equals("/my_path")) {

//...retrieve the message//

            final String message = new String(messageEvent.getData());

            if(message.equals("_watchready")){
                Intent messageIntent = new Intent();
                messageIntent.setAction(Intent.ACTION_SEND);
                messageIntent.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                return;
            }

            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);


            Log.d("print_message", message.split(",")[3]);
            String[] split_message = message.split(",");
            for(int i=0; i<50; i++)
            {
                output.add(Arrays.copyOfRange(split_message, 6*i, 6*i + 6));
                String datapath = "/my_path";
                new SendMessage(datapath, "_startrealtime").start();
            }
//            if(output.size()>windowSize){
//
//                saveToFile(output , SettingsActivity.filename);
//                output.clear();
//
//                String datapath = "/my_path";
//                new SendMessage(datapath, "_startrealtime").start();
//            }
            //Broadcast the received Data Layer messages locally//
            output.add(message.split(","));
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

}