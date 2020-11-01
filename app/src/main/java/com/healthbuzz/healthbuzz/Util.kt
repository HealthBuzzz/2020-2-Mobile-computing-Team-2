package com.healthbuzz.healthbuzz

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


// Generates log TAG constant automatically
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun startSensorService(context: Context) {
    if (!isServiceRunning(context, "com.healthbuzz.healthbuzz.SensorService"))
        startSensorServiceSub(context)
}

fun startSensorServiceSub(context: Context) {
    val intent = Intent(context, SensorService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

// Though deprecated but will work as expected
fun isServiceRunning(context: Context, className: String): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    val info = activityManager!!.getRunningServices(Int.MAX_VALUE)
    if (info == null || info.size == 0) return false
    for (aInfo in info) {
        Log.d("IsServiceRunnig", "classname: ${aInfo.service.className}")
        if (className == aInfo.service.className) return true
    }
    return false
}
