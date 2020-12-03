package com.healthbuzz.healthbuzz

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.healthbuzz.healthbuzz.rundetector.GpsRunDetector
import com.healthbuzz.healthbuzz.rundetector.RunningStateListener
import weka.classifiers.Classifier
import weka.core.*
import java.io.IOException
import java.util.*


class SensorService : Service(), SensorEventListener, RunningStateListener {
    private val CHANNEL_ID = "HealthBuzzSensorService"
    private val CHANNEL_NAME = "HealthBuzz sensor service"

    private val binder = SensorBinder()

    private val samplingRate = SensorManager.SENSOR_DELAY_GAME

    private val sitting: String = "sitting"
    private val walking: String = "walking"
    private val running: String = "running"

    private lateinit var gyroscope: Sensor
    private lateinit var accelerometer: Sensor
    private var sensorManager: SensorManager? = null

    private lateinit var runDetector: GpsRunDetector

    private val windowSize = 100
    private val strideSize = 20
    private val processor = Processor(windowSize, strideSize)
    private var stop_count = 0
    private var not_stop_count = 0
    private var lastTimeMoveSec = System.currentTimeMillis()

    private var lastTimeWalkSec = System.currentTimeMillis()
    private var lastTimeRunSec = System.currentTimeMillis()
    private var isWalking = false
    private var isRunning = false
    private var currentRunState: GpsRunDetector.RunState = GpsRunDetector.RunState.STOPPED

    private val xAttr = Attribute("x")
    private val yAttr = Attribute("y")
    private val zAttr = Attribute("z")
    private val attributes = ArrayList(listOf(xAttr, yAttr, zAttr))

    private val labelList: ArrayList<String> = ArrayList(
        listOf(
            sitting,
            walking,
            running
        )
    )

    var labelAttr: Attribute? = null
    val initialInstancesSize = 2000
    private val inferenceSegment = Instances("inference", attributes, initialInstancesSize)

    private lateinit var assetClassifier: Classifier

    private lateinit var notiBuilder: Notification.Builder

    private lateinit var notiManager: NotificationManager

    private var isNotifying = false

    companion object {
        // We need to make this false when user not allow vibrate initially

        var soundSetting = "Buzz"

        @JvmStatic
        fun setSound(setting: String) {
            soundSetting = setting
        }

        private const val ONGOING_NOTIFICATION_ID = 1
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class SensorBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): SensorService = this@SensorService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    // https://stackoverflow.com/a/47533338/8614565
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notiManager = getSystemService()!!
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notification: Notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notiBuilder = Notification.Builder(this, channelId)
                    .setContentTitle(getText(R.string.ticker_text))
                    .setContentText(getText(R.string.ticker_text))
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.ticker_text))
                notiBuilder.build()
            } else {
                notiBuilder = Notification.Builder(this)
                notiBuilder.build()
            }

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        RealtimeModel.stretching_count.observeForever {
            isNotifying = false
            lastTimeMoveSec = System.currentTimeMillis()
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        loadModel("rf.model")

        sensorManager!!.registerListener(this, accelerometer, samplingRate)
        sensorManager!!.registerListener(this, gyroscope, samplingRate)

        runDetector = GpsRunDetector(this, this)
        runDetector.startDetection()
        lastTimeMoveSec = System.currentTimeMillis()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
//        thread?.interrupt()
        sensorManager?.unregisterListener(this, accelerometer)
        sensorManager?.unregisterListener(this, gyroscope)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == accelerometer) {
            val sample: Instance = DenseInstance(attributes.size)
            for (i in event.values.indices) {
                sample.setValue(i, event.values[i].toDouble())
            }
            handleInference(sample)
        } else if (event?.sensor == gyroscope) {
            val sample: Instance = DenseInstance(attributes.size)
            for (i in event.values.indices) {
                sample.setValue(i, event.values[i].toDouble())
            }
            //handleInference(sample)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun handleInference(sample: Instance) {
        inferenceSegment.add(sample)
        if (inferenceSegment.size >= windowSize) {
            val features: Instances = processor.extractFeaturesAndAddLabels(inferenceSegment, -1)
            inferenceSegment.clear()
            val feature = features[0]
            try {
                val prediction = assetClassifier.classifyInstance(feature).toInt()
                //inferenceResultView.setText(labelList.get(prediction));
                Log.d("stop_count", stop_count.toString())
                Log.d("prediction", prediction.toString())
                if (prediction == 0) {
                    stop_count += 1
                    not_stop_count = 0
                    val currentTimeSec = System.currentTimeMillis()
                    val timeDiffSec = (currentTimeSec - lastTimeMoveSec) / 1000
                    Log.d(TAG, "time_diff$timeDiffSec")
                    val leftSeconds = getLeftSeconds(timeDiffSec)

                    RealtimeModel.stretching_time_left.postValue(leftSeconds)
                    if (leftSeconds <= 0) {
                        if (!isNotifying) {
                            alarmToStretch()
                        }
                        Log.d(TAG, "You need to move $timeDiffSec")
                    } else {
                        isNotifying = false
                        Log.d(TAG, "val:${labelList[prediction]}")
                        showDebugToNoti(prediction)
                    }
                } else { // moving!
                    not_stop_count += 1
                    if (not_stop_count >= 3) {
                        stop_count = 0
                        lastTimeMoveSec = System.currentTimeMillis()
                    }
                    showDebugToNoti(prediction)
                    Log.d(TAG, "val:${labelList[prediction]}")
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString(), e)
                Toast.makeText(applicationContext, "Inference failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLeftSeconds(timeDiffSec: Long): Long {
        //bad practice which always read the value
        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        var timeIntervalStretch: String =
            prefs.getString("time_interval_stretch", "20") ?: "20"
        if (timeIntervalStretch.isEmpty())
            timeIntervalStretch = "20"

        val leftSeconds = Integer.parseInt(timeIntervalStretch) * 60 - timeDiffSec
        return leftSeconds
    }

    private fun showDebugToNoti(prediction: Int) {
//        notiManager.cancel(ONGOING_NOTIFICATION_ID)
        notiBuilder.setContentText("Current status: ${labelList[prediction]}, gps: $currentRunState")
        notiManager.notify(ONGOING_NOTIFICATION_ID, notiBuilder.build())
    }

    private fun alarmToStretch() {
        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        if (!prefs.getBoolean("n_bother", false)) {
            if (soundSetting == "Buzz") {
                val vibrator: Vibrator =
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            500,
                            DEFAULT_AMPLITUDE
                        )
                    ) // 0.5초간 진동
                } else {
                    vibrator.vibrate(500);
                }
            } else if (soundSetting == "Sound") {
                val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300)
            }
        }
        val stretchIntent =
            Intent(this, StretchBroadcastReceiver::class.java).apply {
                action = "ACTION_STRETCH"
                putExtra("stretched", true)
                //                                    putExtra(EXTRA_NOTIFICATION_ID, 0)
            }
        val notStretchIntent = Intent(
            this, StretchBroadcastReceiver::class.java
        ).apply {
            action = "ACTION_STRETCH"
            putExtra("stretched", false)
        }
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, stretchIntent, 0)
        val snoozePendingIntent2: PendingIntent =
            PendingIntent.getBroadcast(this, 0, notStretchIntent, 0)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.stretching)
            .setContentTitle("You need to stretch now!")
            .setContentText("Happy stretching")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(snoozePendingIntent)
            .addAction(
                R.drawable.stretching, "I will stretch now!",
                snoozePendingIntent
            ).addAction(
                R.drawable.icon, "later",
                snoozePendingIntent2
            )
            .setAutoCancel(true)
        //                            notiBuilder.setContentText("You need to move $time_diff")
        notiManager.notify(1, builder.build())
        isNotifying = true
    }

    private fun loadModel(model_name: String) {
        Log.d("load_asset_model", "loading the model from asset folder")
        val assetManager = assets
        try {
            assetClassifier = SerializationHelper.read(assetManager.open(model_name)) as Classifier
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load ", e)
        }
        Toast.makeText(this, "Model loaded", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Load model finished")
    }

    override fun onStartWalking() {
        Log.d(TAG, "Start Walking $isWalking")
        if (isWalking)
            return
        isWalking = true
        lastTimeWalkSec = System.currentTimeMillis() / 1000
    }

    override fun onStopWalking(newState: GpsRunDetector.RunState) {
        Log.d(TAG, "Stop Walking $isWalking")
        if (!isWalking)
            return
        isWalking = false
        val nowTimeSec = System.currentTimeMillis() / 1000
        val walkingTime = nowTimeSec - lastTimeWalkSec
        if (walkingTime >= 1800) {
            // recommend stretching for walking
        }
    }

    override fun onStartRunning() {
        Log.d(TAG, "Start Running $isRunning")
        if (isRunning)
            return
        isRunning = true
        lastTimeRunSec = System.currentTimeMillis() / 1000
    }

    override fun onStopRunning(newState: GpsRunDetector.RunState) {
        Log.d(TAG, "Stop Running $isRunning")
        if (!isRunning)
            return
        isRunning = false
        if (newState == GpsRunDetector.RunState.STOPPED)
            isWalking = false
        val nowTimeSec = System.currentTimeMillis() / 1000
        val runningTime = nowTimeSec - lastTimeRunSec
        if (runningTime >= 1800) {
            // recommend stretching after running
            recommendStretching(StretchingType.AFTER_RUN)
        }
    }

    override fun onRequirePermission() {
        // ignore
        Log.e(TAG, "Why is it called?")
    }

    override fun onStateMayUpdate(state: GpsRunDetector.RunState) {
        currentRunState = state
        // do nothing
        Log.v(TAG, "onStateContinued")
        // notify!
//        notiBuilder.setContentText("Current status: ${labelList[prediction]}")
//        notiManager.notify(1, notiBuilder.build())
    }

    fun recommendStretching(type: StretchingType? = null) {
        val keyword = when (type) {
            null -> "stretching"
            StretchingType.COOLING -> "stretching+for+cool+down"
            StretchingType.AFTER_WALK -> "stretching+after+walking"
            StretchingType.AFTER_RUN -> "stretching+after+running"
            StretchingType.AFTER_SITTING -> "stretching+after+sitting+all+day"
            StretchingType.AFTER_WAKEUP -> "wakeup+stretching"
        }
        val url = "https://www.youtube.com/results?search_query=$keyword"
        if (!isNotifying) {
            val stretchIntent =
                Intent(this, StretchBroadcastReceiver::class.java).apply {
                    action = "ACTION_STRETCH"
                    putExtra("stretched", true)
//                                    putExtra(EXTRA_NOTIFICATION_ID, 0)
                }
            val snoozePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, stretchIntent, 0)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.stretching)
                .setContentTitle("You need to stretch now!")
                .setContentText(url)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(snoozePendingIntent)
                .addAction(
                    R.drawable.stretching, "I stretched!",
                    snoozePendingIntent
                ).setAutoCancel(true)
//                            notiBuilder.setContentText("You need to move $time_diff")
            notiManager.notify(1, builder.build())
            isNotifying = true
        }

    }

    fun resetStretchTime() {
        lastTimeMoveSec = System.currentTimeMillis()
    }
}