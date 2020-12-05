package com.healthbuzz.healthbuzz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.healthbuzz.healthbuzz.data.LoginDataSource


class WaterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action
        Log.d(TAG, "Action:$action")
        if ("ACTION_DRINK" == action) {
            val willDrinkNow = intent.getBooleanExtra("water", false)
            if (willDrinkNow) {
                val drankNow = intent.getBooleanExtra("RealWater", false)
                if (drankNow) {
                    RealtimeModel.water_count.postValue(
                        (RealtimeModel.water_count.value?.toLong() ?: 0) + 200
                    ) // add 1
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