package com.healthbuzz.healthbuzz;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;  

class Motions {
    public static final int ONDESK = 0;
    public static final int STANDING = 1;
    public static final int WALKING = 2;
}

class SensorData {
    public float[] data = new float[3];
    public SensorType sensorType;

    SensorData(float x, float y, float z, SensorType sensorType) {
        data[0] = x;
        data[1] = y;
        data[2] = z;
        this.sensorType = sensorType;
    }
}

public class DataGettingActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = DataGettingActivity.class.getSimpleName();
    private static String ondesk, standing, walking;
    private static ArrayList<String> labelList;

    private SensorManager sm;
    private Sensor accelerometer, gyroscope;
    private final int samplingRate = SensorManager.SENSOR_DELAY_GAME;

    private LinkedList<SensorData> ondeskSegment, standingSegment, walkingSegment;

    private Button standingButton, walkingButton, runningButton;
    private String currentMotion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_getting);

        ondesk = getString(R.string.ondesk);
        standing = getString(R.string.standing);
        walking = getString(R.string.walking);

        standingButton = findViewById(R.id.standing);
        walkingButton = findViewById(R.id.walking);
        runningButton = findViewById(R.id.running);

        labelList = new ArrayList<>(Arrays.asList(ondesk, standing, walking));

        ondeskSegment = new LinkedList<>();
        standingSegment = new LinkedList<>();
        walkingSegment = new LinkedList<>();

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            SensorData sample = new SensorData(event.values[0], event.values[1], event.values[2], SensorType.ACCELEROMETER);
            if (currentMotion.equals(ondesk)) {
                ondeskSegment.add(sample);
            } else if (currentMotion.equals(standing)) {
                standingSegment.add(sample);
            } else if (currentMotion.equals(walking)) {
                walkingSegment.add(sample);
            }
            setSampleNumbers();
        } else if (event.sensor == gyroscope) {
            SensorData sample = new SensorData(event.values[0], event.values[1], event.values[2], SensorType.GYROSCOPE);
            if (currentMotion.equals(ondesk)) {
                ondeskSegment.add(sample);
            } else if (currentMotion.equals(standing)) {
                standingSegment.add(sample);
            } else if (currentMotion.equals(walking)) {
                walkingSegment.add(sample);
            }
            setSampleNumbers();
        }
    }

    public void onMotionClick(View view) {
        if (currentMotion == null) {
            sm.registerListener(this, accelerometer, samplingRate);
            sm.registerListener(this, gyroscope, samplingRate);
        }
        String inputMotion = view.getTag().toString();
        currentMotion = inputMotion;
    }

    public void onStopClick(View view) {
        sm.unregisterListener(this);
        currentMotion = null;
    }

    public void onSaveClick(View view) {
        String filename = new SimpleDateFormat("'SensorData'yyyyMMddHHmm'.csv'", Locale.KOREA).format(new Date());

        File root = new File(Environment.getExternalStorageDirectory(), filename);
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(root));
        } catch (Exception e) {
            Log.e(TAG, "ERROR in making CSVWriter", e);
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Label {0: ondeskAcc, 1: standingAcc, 2: walkingAcc,
        //      3: ondeskGyro, 4: standingGyro, 5: walkingGyro}

        LinkedList<LinkedList<SensorData>> segments
                = new LinkedList<>(Arrays.asList(ondeskSegment, standingSegment, walkingSegment));
        LinkedList<String[]> data = new LinkedList<>();

        for (int i = 0; i < segments.size(); i++) {
            LinkedList<SensorData> targetSegment = segments.get(i);
            for (int j = 0; j < targetSegment.size(); j++) {
                data.add(new String[]{"" + targetSegment.get(j).data[0],
                        "" + targetSegment.get(j).data[1],
                        "" + targetSegment.get(j).data[2],
                        "" + ((targetSegment.get(j).sensorType == SensorType.ACCELEROMETER) ? 0 : 3)});
            }
        }

        writer.writeAll(data);

        try {
            writer.close();
            Toast.makeText(this, "Successfully saved to file:" + root.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "ERROR in closing CSVWriter", e);
        }
    }

    public void onResetClick(View view) {
        onStopClick(view);
        ondeskSegment.clear();
        standingSegment.clear();
        walkingSegment.clear();
        setSampleNumbers();
    }

    private void setSampleNumbers() {
        runningButton.setText(ondesk + " " + ondeskSegment.size());
        standingButton.setText(standing + " " + standingSegment.size());
        walkingButton.setText(walking + " " + walkingSegment.size());
    }

    @Override
    public void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentMotion != null) {
            sm.registerListener(this, accelerometer, samplingRate);
            sm.registerListener(this, gyroscope, samplingRate);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
