package com.healthbuzz.healthbuzz

import android.content.Context
import android.content.Intent
import android.os.Build


// Generates log TAG constant automatically
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun startSensorService(context: Context) {
    val intent = Intent(context, SensorService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}