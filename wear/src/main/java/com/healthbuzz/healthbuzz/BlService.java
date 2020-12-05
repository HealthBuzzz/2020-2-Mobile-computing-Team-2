package com.healthbuzz.healthbuzz;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

class Motions {
    public static final int ONDESK = 0;
    public static final int STANDING = 1;
    public static final int WALKING = 2;
}

class SensorData {
    public float[] data = new float[3];
    public SensorType sensorType;
    public long mydate;
    public String motion;

    SensorData(float x, float y, float z, SensorType sensorType, long date, String motion_string) {
        data[0] = x;
        data[1] = y;
        data[2] = z;
        this.sensorType = sensorType;
        mydate = date;
        motion = motion_string;
    }

    @NonNull
    @Override
    public String toString() {
        return "" + data[0] + "," + data[1] + "," + data[2] + "," + sensorType + "," + mydate + "," + motion;
    }
}


public class BlService extends WearableListenerService implements SensorEventListener {

    static List<BlService> serviceList = new ArrayList<BlService>();
    private int sensor_data_count = 0;
    private ArrayList<SensorData> sensor_array = new ArrayList<SensorData>();

    @Override
    public void onCreate() {
        super.onCreate();

        serviceList.add(this);
        if (sm == null) {

            sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        HeartRate = sm.getDefaultSensor(Sensor.TYPE_HEART_RATE);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

//If the message’s path equals "/my_path"...//

        if (messageEvent.getPath().equals("/my_path")) {

//...retrieve the command//

            final String command = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("command", command);


            processTheCommand(command);

            //Broadcast the received Data Layer messages locally//
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private static String currentMotion = null;

    private static SensorManager sm;
    private static Sensor accelerometer, gyroscope, HeartRate;
    private final int samplingRate = SensorManager.SENSOR_DELAY_GAME;

    public void processTheCommand(String command) {
        if (command.equals("_normal")) {
            Log.e("CYT_LOG", "normal");
            if (currentMotion == null) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                sm.registerListener(this, HeartRate, samplingRate);
            }
            String inputMotion = "normal";
            currentMotion = inputMotion;
        } else if (command.equals("_up")) {
            Log.e("CYT_LOG", "up");
            if (currentMotion == null) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                sm.registerListener(this, HeartRate, samplingRate);
            }
            String inputMotion = "up";
            currentMotion = inputMotion;
        } else if (command.equals("_drinking")) {
            Log.e("CYT_LOG", "drinking");
            if (currentMotion == null) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                sm.registerListener(this, HeartRate, samplingRate);
            }
            String inputMotion = "drinking";
            currentMotion = inputMotion;
        } else if (command.equals("_down")) {
            Log.e("CYT_LOG", "down");
            if (currentMotion == null) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                sm.registerListener(this, HeartRate, samplingRate);
            }
            String inputMotion = "down";
            currentMotion = inputMotion;
        } else if (command.equals("_ondesk")) {
            Log.e("CYT_LOG", "ondesk");
            if (currentMotion == null) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                sm.registerListener(this, HeartRate, samplingRate);
            }
            String inputMotion = "ondesk";
            currentMotion = inputMotion;
        } else if (command.equals("_walking")) {
            Log.e("CYT_LOG", "walking");
            if (currentMotion == null) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                sm.registerListener(this, HeartRate, samplingRate);
            }
            String inputMotion = "walking";
            currentMotion = inputMotion;
        } else if (command.equals("_running")) {
            Log.e("CYT_LOG", "running");
            if (currentMotion == null) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                sm.registerListener(this, HeartRate, samplingRate);
            }
            String inputMotion = "running";
            currentMotion = inputMotion;
        } else if (command.equals("_stop")) {
            Log.e("CYT_LOG", "stop");

            for (BlService bls : serviceList) {
                sm.unregisterListener(bls);
            }

            String inputMotion = "stop";
            currentMotion = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        /*if(serviceList.size()>1){
            int i=0;
            for(BlService bls : serviceList){
                if(bls!=null){
                    Log.e("CYT_LOG" , i+" not null");
                    i++;
                }
            }
        }
        Log.e("CYT_LOG" , serviceList.size()+"");*/
        if(sensor_data_count == 0)
        {
            sensor_array.clear();
        }
        sensor_data_count++;
        if (event.sensor == accelerometer) {
            SensorData sample = new SensorData(event.values[0], event.values[1], event.values[2], SensorType.ACCELEROMETER, new Date().getTime(), currentMotion);
            sensor_array.add(sample);
        }
        if (event.sensor == HeartRate) {
            SensorData sample = new SensorData(event.values[0], 0, 0, SensorType.HEARTRATE, new Date().getTime(), currentMotion);
            sensor_array.add(sample);
        }
        if (event.sensor == gyroscope) {
            SensorData sample = new SensorData(event.values[0], event.values[1], event.values[2], SensorType.GYROSCOPE , new Date().getTime(), currentMotion);
            sensor_array.add(sample);
        }
        if(sensor_data_count == 50)
        {
            sensor_data_count = 0;
            String datapath = "/my_path";
            new SendMessage(datapath, sensor_array.toString()).start();
            Log.e("CYT_LOG" , sensor_array.toString());
            return;
        }




    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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