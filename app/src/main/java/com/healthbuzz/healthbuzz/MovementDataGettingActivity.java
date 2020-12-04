package com.healthbuzz.healthbuzz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MovementDataGettingActivity extends AppCompatActivity {

    Button talkbutton;
    TextView textview;
    protected Handler myHandler;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;

    @Override
    protected void onPause() {
        super.onPause();
        new MovementDataGettingActivity.NewThread("/my_path", "_stop").start();
        BlService.output.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_data_getting);
        talkbutton = findViewById(R.id.talkButton);
        textview = findViewById(R.id.textView);

        //Create a message handler//

        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                messageText(stuff.getString("messageText"));
                return true;
            }
        });

//Register to receive local broadcasts, which we'll be creating in the next step//

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MovementDataGettingActivity.Receiver messageReceiver = new MovementDataGettingActivity.Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

    }

    public void messageText(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            textview.append("\n" + newinfo);
        }

    }

//Define a nested class that extends BroadcastReceiver//

    public class Receiver extends BroadcastReceiver {
        @Override

        public void onReceive(Context context, Intent intent) {

//Upon receiving each message from the wearable, display the following text//

            String message = "I just received a message from the wearable " + receivedMessageNumber++;
            message = intent.getStringExtra("message");

            textview.setText(message);

        }
    }

    EditText et;

    public void talkClick(View v) {
        et = (EditText) findViewById(R.id.editText);
        String message = et.getText().toString(); //"Sending message.... ";
        textview.setText(message);

//Sending a message can block the main UI thread, so use a new thread//

        new MovementDataGettingActivity.NewThread("/my_path", message).start();

    }

    public void sendMessageOnclick(View v) {
        switch (v.getId()) {
            case R.id.btn_ondesk:
                new MovementDataGettingActivity.NewThread("/my_path", "_ondesk").start();
                break;
            case R.id.btn_walking:
                new MovementDataGettingActivity.NewThread("/my_path", "_walking").start();
                break;
            case R.id.btn_running:
                new MovementDataGettingActivity.NewThread("/my_path", "_running").start();
                break;
            case R.id.btn_stop:
                new MovementDataGettingActivity.NewThread("/my_path", "_stop").start();
                break;
            case R.id.btn_save:
                new MovementDataGettingActivity.NewThread("/my_path", "_stop").start();
                //save BlService.output to file
                saveToFile(BlService.output);
                break;
            case R.id.btn_rest:
                new MovementDataGettingActivity.NewThread("/my_path", "_stop").start();
                //clear all from BlService.output
                BlService.output.clear();
                break;
        }


    }

    public void saveToFile(LinkedList<String[]> output) {

        String filename = new SimpleDateFormat("'SensorData'yyyyMMddHHmm'_W.csv'", Locale.KOREA).format(new Date());

        File root = new File(Environment.getExternalStorageDirectory(), filename);
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(root));
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

//Use a Bundle to encapsulate our message//

    public void sendmessage(String messageText) {
        Bundle bundle = new Bundle();
        bundle.putString("messageText", messageText);
        Message msg = myHandler.obtainMessage();
        msg.setData(bundle);
        myHandler.sendMessage(msg);

    }

    class NewThread extends Thread {
        String path;
        String message;

//Constructor for sending information to the Data Layer//

        NewThread(String p, String m) {
            path = p;
            message = m;
        }

        public void run() {

//Retrieve the connected devices, known as nodes//

            Task<List<Node>> wearableList =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =

//Send the message//

                            Wearable.getMessageClient(MovementDataGettingActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {

//Block on a task and get the result synchronously//

                        Integer result = Tasks.await(sendMessageTask);
                        //sendmessage("I just sent the wearable a message " + sentMessageNumber++);

                        //if the Task fails, then…..//

                    } catch (ExecutionException exception) {

                        //TO DO: Handle the exception//

                    } catch (InterruptedException exception) {

                        //TO DO: Handle the exception//

                    }

                }

            } catch (ExecutionException exception) {

                //TO DO: Handle the exception//

            } catch (InterruptedException exception) {

                //TO DO: Handle the exception//
            }

        }
    }
}
