package com.healthbuzz.healthbuzz

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SensorService : Service() {

    override fun onBind(intent: Intent): IBinder? {
//        TODO("Return the communication channel to the service.")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}