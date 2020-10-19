package com.healthbuzz.healthbuzz

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder

class SensorService : Service() {

    private var thread: Thread? = null

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1
        private const val CHANNEL_DEFAULT_IMPORTANCE = "healthbuzz.channel"
    }

    override fun onBind(intent: Intent): IBinder? {
//        TODO("Return the communication channel to the service.")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                    .setContentTitle(getText(R.string.app_name))
                    .setContentText(getText(R.string.ticker_text))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.ticker_text))
                    .build()
            } else {
                Notification()
            }

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification)

        thread = Thread {
            SensorThread.run(this)
        }
        thread?.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        thread?.interrupt()
    }
}