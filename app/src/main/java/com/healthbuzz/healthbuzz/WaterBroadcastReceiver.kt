package com.healthbuzz.healthbuzz

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.getSystemService
import com.healthbuzz.healthbuzz.data.LoginDataSource


class WaterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action
        Log.d(TAG, "Action:$action")
        if ("ACTION_DRINK" == action) {
            val notiManager: NotificationManager? = context.getSystemService()
            notiManager?.cancel(SensorService.WATER_NOTIFICATION_ID)

            val willDrinkNow = intent.getBooleanExtra("water", false)
            if (willDrinkNow) {
                val drankNow = intent.getBooleanExtra("RealWater", false)
                notiManager?.cancel(SensorService.WATER_ASK_NOTIFICATION_ID)
                if (drankNow) {
                    val theIntent = Intent(context, WaterAmountInputActivity::class.java)
                    context.startActivity(theIntent)
                    if (!UserInfo.userName.value.equals("")) {
                        LoginDataSource.postTodayWater()
                    }
                }
            } else {
                // show danger of not drinking
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=9iMGFqMmUFs&ab_channel=TED-Ed")
                    )
                )
            }
        }
    }
}