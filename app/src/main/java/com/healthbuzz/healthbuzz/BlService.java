package com.healthbuzz.healthbuzz;

import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

//Extend WearableListenerService//

public class BlService extends WearableListenerService {

    public static LinkedList<String[]> output = new LinkedList<>();
    private final Attribute Attr0 = new Attribute("0");
    private final Attribute Attr1 = new Attribute("1");
    private final Attribute Attr2 = new Attribute("2");
    private final ArrayList<Attribute> attributes = new ArrayList<>(Arrays.asList(Attr0, Attr1, Attr2));
    private Instances output_segment = new Instances("output", attributes, 100);
    private Classifier asset_classifier = null;

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.d("BlService_create","BLservice");
//        new NewThread("/my_path", "_realtime").start();
//    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if(asset_classifier == null){
            loadModel("drink_rf.model");
            Log.d("drink_rf", "Load model finished");
        }

//If the message’s path equals "/my_path"...//

        if (messageEvent.getPath().equals("/my_path")) {

//...retrieve the message//
            output_segment.clear();
            final String message = new String(messageEvent.getData());

            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);

            Log.d("print_message", message.split(",")[5]);
            String[] split_message = message.split(",");
            //Log.d("message_size",String.valueOf(split_message.length));

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
            Log.d("output_size",String.valueOf(output.size()));
            Log.d("output_segment",output_segment.get(0).toString());


            Instances features = extractFeatures(output_segment, 1);
            Instance feature = features.get(0);
            Log.d("feature", feature.toString());
            try{
                double pred = asset_classifier.classifyInstance(feature);
                String pred_result = features.classAttribute().value((int)pred);
                Log.d("prediction",String.valueOf(pred));
            }
            catch (Exception e) {
                Log.d("inference_error", e.toString());
            }

            //Log.d("feature", feature.toString());

//Broadcast the received Data Layer messages locally//
//            output.add(message.split(","));
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else {
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

    Instances extractFeatures(Instances data, int label) {
        Instances meanFeatures = new Instances("mean", getAttributes(new String[]{"0", "1", "2"}), 100);
        Instances varFeatures = new Instances("var", getAttributes(new String[]{"3", "4", "5"}), 100);
        Instances diffFeatures = new Instances("diff", getAttributes(new String[]{"6", "7", "8"}), 100);
        Instances labels = new Instances("label", getAttributes(new String[]{"9"}), 100);

        meanFeatures.add(mean(data));
        varFeatures.add(var(data));
        diffFeatures.add(diffsum(data));
        Instance labelInstance = new DenseInstance(1);
        labelInstance.setValue(0, label);
        labels.add(labelInstance);

        Log.d("mean", mean(data).toString());
        Log.d("var", var(data).toString());
        Log.d("diffsum", diffsum(data).toString());

        Instances featureData = Instances.mergeInstances(meanFeatures, varFeatures);
        featureData = Instances.mergeInstances(featureData, diffFeatures);
        featureData = Instances.mergeInstances(featureData, labels);
        featureData.setClassIndex(9);
        return featureData;
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

                            Wearable.getMessageClient(BlService.this).sendMessage(node.getId(), path, message.getBytes());

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