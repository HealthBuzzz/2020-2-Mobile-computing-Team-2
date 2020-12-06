package com.healthbuzz.healthbuzz

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.getSystemService
import com.healthbuzz.healthbuzz.UserInfo.userName
import com.healthbuzz.healthbuzz.data.LoginDataSource

class StretchBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action
        Log.d(TAG, "Action:$action")
        if ("ACTION_STRETCH" == action) {
            val notiManager: NotificationManager? = context.getSystemService()
            notiManager?.cancel(SensorService.STRETCHING_NOTIFICATION_ID)
            val willStretchNow = intent.getBooleanExtra("stretched", false)
            if (willStretchNow) {
                showYoutubeSearch(context, SearchKeywords.office)
                RealtimeModel.stretching_count.postValue(
                    (RealtimeModel.stretching_count.value?.toLong() ?: 0) + 1
                ) // add 1
                if (!userName.getValue().equals("")) {
                    LoginDataSource.postTodayStretching()
                }
            }
        }
    }
}