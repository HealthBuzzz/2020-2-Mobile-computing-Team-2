package com.healthbuzz.healthbuzz.rundetector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.healthbuzz.healthbuzz.TAG


class GpsRunDetector @Throws(IllegalStateException::class) constructor(
    val context: Context,
    val listener: RunningStateListener
) :
    LocationListener {

    enum class RunState(val threshold: Float) {
        STOPPED(0.1f),
        WALKING(1.2f),
        RUNNING(7f),
        TRANSPORT(22f),
        HIGHWAY(28f),
        TELEPORT(Float.POSITIVE_INFINITY);

        companion object {
            fun fromFloat(value: Float): RunState {
                return values().find {
                    value <= it.threshold
                }!!
            }
        }
    }

    var prevRunState: RunState = RunState.STOPPED

    private val manager: LocationManager = context.getSystemService()
        ?: throw IllegalStateException("No location manager found")

    @Throws(IllegalStateException::class)
    fun startDetection() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw IllegalStateException("Permission not granted")
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0.0f, this)
    }


    override fun onLocationChanged(location: Location) {
        if (location.hasSpeed()) {
            val speed = location.speed
            Log.d(TAG, "Yay has speed $speed !!!!")

            val state = RunState.fromFloat(speed)
            listener.onStateMayUpdate(state)
            when (prevRunState) {
                RunState.STOPPED -> when (state) {
                    RunState.STOPPED -> listener.onStateMayUpdate(state)
                    RunState.WALKING -> listener.onStartWalking()
                    RunState.RUNNING -> listener.onStartRunning()
                    else -> return
                }
                RunState.WALKING -> when (state) {
                    RunState.STOPPED -> listener.onStopWalking(state)
                    RunState.WALKING -> listener.onStateMayUpdate(state)
                    RunState.RUNNING -> listener.onStartRunning()
                    else -> return
                }
                RunState.RUNNING -> when (state) {
                    RunState.STOPPED -> listener.onStopRunning(state)
                    RunState.WALKING -> listener.onStopRunning(state)
                    RunState.RUNNING -> listener.onStateMayUpdate(state)
                    else -> return
                }
                else -> return
            }
        } else {
            Log.d(TAG, "Nooooooo Pleaseeeeeeee")
        }
    }
}
