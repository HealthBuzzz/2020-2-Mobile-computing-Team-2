package com.healthbuzz.healthbuzz.rundetector

interface RunningStateListener {
    fun onStartWalking()
    fun onStopWalking(newState: GpsRunDetector.RunState)
    fun onStartRunning()
    fun onStopRunning(newState: GpsRunDetector.RunState)
    fun onRequirePermission()
    fun onStateContinued(state : GpsRunDetector.RunState)
}