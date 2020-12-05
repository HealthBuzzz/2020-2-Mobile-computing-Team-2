package com.healthbuzz.healthbuzz

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi


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

fun disableEnableControls(enable: Boolean, vg: ViewGroup) {
    for (i in 0 until vg.childCount) {
        val child: View = vg.getChildAt(i)
        child.isEnabled = enable
        if (child is ViewGroup) {
            disableEnableControls(enable, child as ViewGroup)
        }
    }
}

fun formatTime(context: Context, seconds: Int): String {
    return if (seconds >= 60) {
        val minutes = seconds / 60
        context.getString(R.string.dashboard_minutes_left, minutes)
    } else {
        context.getString(R.string.dashboard_seconds_left, seconds)
    }
}

fun showYoutubeSearch(context: Context, query: String) {
    val intent = Intent(Intent.ACTION_SEARCH)
    intent.setPackage("com.google.android.youtube")
    intent.putExtra("query", query)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.createNotificationChannel(channelId: String, channelName: String): String {
    val chan = NotificationChannel(
        channelId,
        channelName, NotificationManager.IMPORTANCE_HIGH
    )
    chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
}