package com.healthbuzz.healthbuzz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED ->
                startSensorService(context)
        }
        Log.i(TAG, "started")
    }
}