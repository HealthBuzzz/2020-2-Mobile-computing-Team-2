package com.healthbuzz.healthbuzz.rundetector

interface RunningStateListener {
    fun onStartRunning()
    fun onEndRunning()
    fun onSpeedDown()
    fun onSpeedUp()
    fun onRequirePermission()
}