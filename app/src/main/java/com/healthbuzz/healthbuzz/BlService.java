

//############################################################
package com.healthbuzz.healthbuzz;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
//

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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

//Extend WearableListenerService//

public class BlService extends WearableListenerService {

    public static LinkedList<String[]> output=new LinkedList<>();
    public static List<String[]> output2;
    private final Attribute Attr0 = new Attribute("0");
    private final Attribute Attr1 = new Attribute("1");
    private final Attribute Attr2 = new Attribute("2");
    private final ArrayList<Attribute> attributes = new ArrayList<>(Arrays.asList(Attr0, Attr1, Attr2));
    private Instances output_segment = new Instances("output", attributes, 100);
    private Classifier asset_classifier = null;

    private int state_count = 0;
    private int not_state_count = 0;
    private int state = 0;
    // drink state = 1 means drink detected, when 10 second go after drink detect(drink time), drink state revert to 0
    private int drink_state = 0;
    private int drink_time = 0;


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

        if(asset_classifier == null){
            loadModel("drink_newrf.model");
            Log.d("drink_rf", "Load model finished");
        }

        //If the message’s path equals "/my_path"...//
        if (messageEvent.getPath().equals("/my_path")) {

            output_segment.clear();
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

            //Log.e("CYT_LOG" , message);
            Log.d("print_message", message.split(",")[3]);
            String[] split_message = message.split(",");
            for(int i=0; i<50; i++)
            {
                Instance sample = new DenseInstance(3);
                for(int j=0; j<3; j++)
                {
                    sample.setValue(j, Double.parseDouble(split_message[6*i+j]));
                }
                output_segment.add(sample);
                //output.add(Arrays.copyOfRange(split_message, 6*i, 6*i + 6));
            }

            //Log.d("output_size", String.valueOf(output.size()));
            Log.d("output_segment", output_segment.get(0).toString());

            Instances features = extractFeatures(output_segment, "normal");
            features.setClassIndex(9);
            //Log.d("feature", features.classAttribute().toString());
            //Log.d("feature", features.toSummaryString());
            Instance feature = features.get(0);

            try{
                double pred = asset_classifier.classifyInstance(feature);
                String pred_result = features.classAttribute().value((int)pred);
                //Log.d("prediction", String.valueOf(pred));
                Log.d("prediction", pred_result);

                if(drink_state == 1)
                {
                    drink_time = drink_time + 1;
                }
                if(drink_time > 10)
                {
                    drink_state = 0;
                    drink_time = 0;
                }

                //state update
                if(state == 0 & pred == 1)
                {
                    state = 1;
                    state_count = 0;
                    not_state_count = 0;
                    Log.d("prediction_state","up");
                }
                else if(state == 1 & pred == 2)
                {
                    state = 2;
                    state_count = 0;
                    not_state_count = 0;
                    Log.d("prediction_state","drink");
                }
                else if(state == 2 & pred == 3)
                {
                    state = 3;
                    state_count = 0;
                    not_state_count = 0;
                    Log.d("prediction_state","down");
                }
                else if(state == 3 & pred == 0)
                {
                    drink_state = 1;
                    drink_time = 0;
                    state = 0;
                    state_count = 0;
                    not_state_count = 0;
                    Log.d("drink_detect", "drink_detect");
                }

                //state count update
                if(state != 0)
                {
                    if(state == pred)
                    {
                        state_count = state_count + 1;
                    }
                    else
                    {
                        not_state_count = not_state_count + 1;
                    }
                }


                if(state_count < not_state_count & not_state_count > 0)
                {
                    Log.d("prediction_state","state revert");
                    state = 0;
                }

            }
            catch (Exception e) {
                Log.d("inference_error", e.toString());
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
            //output.add(message.split(","));
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void loadModel(String model_name){
        Log.d("load_asset_model", "loading the model from asset folder");
        AssetManager assetManager = getAssets();
        try{
            asset_classifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open(model_name));
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Toast.makeText(this, "Model loaded", Toast.LENGTH_SHORT).show();
    }

    private ArrayList<Attribute> getAttributes(String[] attributes) {
        ArrayList<Attribute> attInfo = new ArrayList<>();
        for (String attribute : attributes) {
            attInfo.add(new Attribute(attribute));
        }
        return attInfo;
    }

    private Instance mean(Instances window) {
        int numAxis = window.numAttributes();
        Instance mean = new DenseInstance(3);
        for (int i = 0; i < 3; i++) {
            mean.setValue(i, window.meanOrMode(i));
        }
        return mean;
    }

    private Instance var(Instances window) {
        int numAxis = window.numAttributes();
        Instance var = new DenseInstance(3);
        for (int i = 0; i < 3; i++) {
            var.setValue(i, window.variance(i));
        }
        return var;
    }

    private Instance diffsum(Instances window){
        int numAxis = window.numAttributes();
        Instance diffsum = new DenseInstance(3);
        double diffsum_x=0;
        double diffsum_y=0;
        double diffsum_z=0;
        for(int j=0; j < window.numInstances()-1; j++)
        {
            double diff_sign = (window.get(j+1).value(0) - window.get(j).value(0)) > 0 ? 1 : -1;
            diffsum_x+=diff_sign;
        }
        for(int j=0; j < window.numInstances()-1; j++)
        {
            double diff_sign = (window.get(j+1).value(1) - window.get(j).value(1)) > 0 ? 1 : -1;
            diffsum_y+=diff_sign;
        }
        for(int j=0; j < window.numInstances()-1; j++)
        {
            double diff_sign = (window.get(j+1).value(2) - window.get(j).value(2)) > 0 ? 1 : -1;
            diffsum_z+=diff_sign;
        }
        diffsum.setValue(0, diffsum_x);
        diffsum.setValue(1, diffsum_y);
        diffsum.setValue(2, diffsum_z);
        return diffsum;
    }

    Instances extractFeatures(Instances data, String label) {
        Instances varFeatures = new Instances("var", getAttributes(new String[]{"3", "4", "5"}), 100);
        Instances meanFeatures = new Instances("mean", getAttributes(new String[]{"0", "1", "2"}), 100);
        Instances diffFeatures = new Instances("diff", getAttributes(new String[]{"6", "7", "8"}), 100);

        List<String> label_list = new ArrayList(4);
        label_list.add("normal");
        label_list.add("up");
        label_list.add("drinking");
        label_list.add("down");
        Attribute label_att = new Attribute("9", label_list);
        ArrayList<Attribute> label_arr = new ArrayList<Attribute>();
        label_arr.add(label_att);
//        Instances labels = new Instances("label", getAttributes(new String[]{"9"}), 100);
        Instances labels = new Instances("label", label_arr, 100);


        meanFeatures.add(mean(data));
        varFeatures.add(var(data));
        diffFeatures.add(diffsum(data));
        //Instance labelInstance = new DenseInstance(1);
        Instance labelInstance = new DenseInstance(1);
        labelInstance.setDataset(labels);
        labelInstance.setValue(0, label);
        labels.add(labelInstance);

        //Log.d("mean", mean(data).toString());
        //Log.d("var", var(data).toString());
        //Log.d("diffsum", diffsum(data).toString());

        Instances featureData = Instances.mergeInstances(varFeatures, meanFeatures);
        featureData = Instances.mergeInstances(featureData, diffFeatures);
        featureData = Instances.mergeInstances(featureData, labels);
        return featureData;
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