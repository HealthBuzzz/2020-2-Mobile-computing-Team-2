package com.healthbuzz.healthbuzz;

import android.content.Context;
import android.util.Log;

public class SensorThread {
    private static final String TAG = "SensorThread";

    // It should finish working if it encounters InterruptedException.
    public static int run(Context context) {
        while (true) {

            try {
                Thread.sleep(10000);
                Log.d(TAG, "I am working");
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return 0;
    }
}
