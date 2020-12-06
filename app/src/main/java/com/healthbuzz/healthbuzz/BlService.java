package com.healthbuzz.healthbuzz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

//Extend WearableListenerService//

public class BlService extends WearableListenerService {

    public static LinkedList<String[]> output = new LinkedList<>();


    @Override
    public void onDestroy() {
        Log.e("CYT_LOG" , "destroyed...");
        SharedPreferences sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);

        boolean continueService = sharedPrefs.getBoolean("sync2", false);
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

            if(message.equals("_WR")){
                /*Intent messageIntent = new Intent();
                messageIntent.setAction(Intent.ACTION_SEND);
                messageIntent.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);*/
                SharedPreferences sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
                sharedPrefs.edit().putBoolean("watchDetected" , true).apply();
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
            }

            //Broadcast the received Data Layer messages locally//
            //output.add(message.split(","));
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else {
            super.onMessageReceived(messageEvent);
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
                        //TO DO//
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