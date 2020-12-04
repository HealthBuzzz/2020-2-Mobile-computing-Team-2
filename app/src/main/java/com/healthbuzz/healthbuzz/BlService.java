package com.healthbuzz.healthbuzz;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

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

            Log.e("CYT_LOG" , message);
            if(output.size()>windowSize){

                saveToFile(output , SettingsActivity.filename);
                output.clear();

                String datapath = "/my_path";
                new SendMessage(datapath, "_startrealtime").start();
            }
            //Broadcast the received Data Layer messages locally//
            output.add(message.split(","));
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    public void saveToFile(LinkedList<String[]> output , String filename){



        File root = new File(Environment.getExternalStorageDirectory(), filename);
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(root , true));
        } catch (Exception e) {
            Log.e("CYT_LOG", "ERROR in making CSVWriter", e);
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
            return;
        }

        writer.writeAll(output);

        try {
            writer.close();
            Toast.makeText(this, "Successfully saved to file:" + root.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("CYt_LOG", "ERROR in closing CSVWriter", e);
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
                            Wearable.getMessageClient(BlService.this).sendMessage(node.getId(), path, message.getBytes());
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