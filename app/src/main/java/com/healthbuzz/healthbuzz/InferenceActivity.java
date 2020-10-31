package com.healthbuzz.healthbuzz;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class InferenceActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = InferenceActivity.class.getSimpleName();
    private Button loadButton;
    private Button inferenceButton;
    private TextView inferenceResultView;
    private static String sitting, walking, running;

    private Sensor accelerometer;
    private Sensor gyroscope;
    private SensorManager sm;
    private final int samplingRate = SensorManager.SENSOR_DELAY_GAME;

    private final int windowSize = 100;
    private final int strideSize = 20;
    private final Processor processor = new Processor(windowSize, strideSize);
    private int stop_count = 0;
    private int not_stop_count = 0;

    private final Attribute xAttr = new Attribute("x");
    private final Attribute yAttr = new Attribute("y");
    private final Attribute zAttr = new Attribute("z");
    private final ArrayList<Attribute> attributes = new ArrayList<>(Arrays.asList(xAttr, yAttr, zAttr));
    private static ArrayList<String> labelList;
    public static Attribute labelAttr;
    public static final int initialInstancesSize = 2000;
    private Instances inferenceSegment = new Instances("inference", attributes, initialInstancesSize);

    private boolean isInference = false;

    private Classifier asset_classifier = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_inference);
        loadButton = findViewById(R.id.load_model);
        inferenceButton = findViewById(R.id.inference);
        inferenceResultView = findViewById(R.id.result);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sitting = "sitting";
        walking = "walking";
        running = "running";

        labelList = new ArrayList<>(Arrays.asList(sitting, walking, running));
        labelAttr = new Attribute("label", labelList);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            Instance sample = new DenseInstance(attributes.size());
            for (int i = 0; i < event.values.length; i++) {
                sample.setValue(i, event.values[i]);
            }
            if (isInference) {
                handleInference(sample);
            }
        } else if (event.sensor == gyroscope) {
            Instance sample = new DenseInstance(attributes.size());
            for (int i = 0; i < event.values.length; i++) {
                sample.setValue(i, event.values[i]);
            }
            if (isInference) {
                handleInference(sample);
            }
        }
    }

    private void loadModel(String model_name) {
        Log.d("load_asset_model", "loading the model from asset folder");
        AssetManager assetManager = getAssets();
        try {
            asset_classifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open(model_name));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Model loaded", Toast.LENGTH_SHORT).show();
    }

    public void onLoadmodelClick(View view) {
        loadModel("rf.model");
    }

    private void handleInference(Instance sample) {
        inferenceSegment.add(sample);
        if (inferenceSegment.size() >= windowSize) {
            Instances features = processor.extractFeaturesAndAddLabels(inferenceSegment, -1);
            inferenceSegment.clear();
            Instance feature = features.get(0);
            if (asset_classifier == null) {
                loadModel("rf.model");
            }
            try {

                //int prediction = (int)classifier.classifyInstance(feature);
                int prediction = (int) asset_classifier.classifyInstance(feature);
                //inferenceResultView.setText(labelList.get(prediction));

                Log.d("stop_count", String.valueOf(stop_count));
                Log.d("prediction", String.valueOf(prediction));

                if (prediction == 0) {
                    stop_count += 1;
                    not_stop_count = 0;
                    if (stop_count > 50) {
                        inferenceResultView.setText("you need to move");
                    } else {
                        inferenceResultView.setText(labelList.get(prediction));
                    }
                } else {
                    not_stop_count += 1;

                    if (not_stop_count >= 5) {
                        stop_count = 0;
                    }
                    inferenceResultView.setText(labelList.get(prediction));
                }
            } catch (Exception e) {
                Log.d(TAG, e.toString());
                inferenceResultView.setText(getString(R.string.inference_failed));
                Toast.makeText(getApplicationContext(), "Inference failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onInferenceClick(View view) {
        if (asset_classifier == null) {
            Toast.makeText(getApplicationContext(), "Model need to be Trained!", Toast.LENGTH_SHORT).show();
            loadModel("rf.model");
        } else {
            isInference = !isInference;
            onStopClick(view);
            inferenceSegment.clear();
            if (isInference) {
                sm.registerListener(this, accelerometer, samplingRate);
                sm.registerListener(this, gyroscope, samplingRate);
                inferenceResultView.setText(getString(R.string.processing));
            } else {
                inferenceResultView.setText(getString(R.string.not_inferencing));
            }
        }
    }

    public void onStopClick(View view) {
        if (!isInference) {
            sm.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}