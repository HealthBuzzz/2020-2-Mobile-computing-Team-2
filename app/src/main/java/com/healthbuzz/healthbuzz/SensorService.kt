package com.healthbuzz.healthbuzz

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import weka.classifiers.Classifier
import weka.core.*
import java.io.IOException
import java.util.*

class SensorService : Service(), SensorEventListener {
    private val samplingRate = SensorManager.SENSOR_DELAY_GAME

    private val sitting: String = "sitting"
    private val walking: String = "walking"
    private val running: String = "running"

    private lateinit var gyroscope: Sensor
    private lateinit var accelerometer: Sensor
    private lateinit var sensorManager: SensorManager
    private var thread: Thread? = null

    private val windowSize = 100
    private val strideSize = 20
    private val processor = Processor(windowSize, strideSize)
    private var stop_count = 0
    private var not_stop_count = 0

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

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent): IBinder? {
//        TODO("Return the communication channel to the service.")
        return null
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
                createNotificationChannel("HealthBuzzSensorService", "HealthBuzz sensor service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notification: Notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notiBuilder = Notification.Builder(this, channelId)
                    .setContentTitle(getText(R.string.app_name))
                    .setContentText(getText(R.string.ticker_text))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.ticker_text))
                notiBuilder.build()
            } else {
                notiBuilder = Notification.Builder(this)
                notiBuilder.build()
            }

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification)
//        thread = Thread {
//            SensorThread.run(this)
//        }
//        thread?.start()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        loadModel("rf.model")
        Log.d(TAG, "Load model finished")

        sensorManager.registerListener(this, accelerometer, samplingRate)
        sensorManager.registerListener(this, gyroscope, samplingRate)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
//        thread?.interrupt()
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, gyroscope)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
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
            handleInference(sample)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO("Not yet implemented")
    }

    private fun handleInference(sample: Instance) {
        inferenceSegment.add(sample)
        if (inferenceSegment.size >= windowSize) {
            val features: Instances = processor.extractFeaturesAndAddLabels(inferenceSegment, -1)
            inferenceSegment.clear()
            val feature = features[0]

            try {

                //int prediction = (int)classifier.classifyInstance(feature);
                val prediction = assetClassifier.classifyInstance(feature).toInt()
                //inferenceResultView.setText(labelList.get(prediction));
                Log.d("stop_count", stop_count.toString())
                Log.d("prediction", prediction.toString())
                if (prediction == 0) {
                    stop_count += 1
                    not_stop_count = 0
                    if (stop_count > 50) {
//                        TODO("Show notification channel")
                        notiBuilder.setContentText("You need to move $stop_count")
                        notiManager.notify(1, notiBuilder.build())
                        // https://developer.android.com/training/notify-user/build-notification
                        Log.d(TAG, "You need to move $stop_count")
                        // inferenceResultView.setText("you need to move")
                    } else {
                        Log.d(TAG, "val:${labelList[prediction]}")
                        notiBuilder.setContentText("You need to move ${labelList[prediction]}")
                        notiManager.notify(1, notiBuilder.build())
//                        inferenceResultView.setText(labelList[prediction])
                    }
                } else {
                    not_stop_count += 1
                    if (not_stop_count >= 5) {
                        stop_count = 0
                    }
                    notiBuilder.setContentText("You need to move ${labelList[prediction]}")
                    notiManager.notify(1, notiBuilder.build())

                    Log.d(TAG, "val:${labelList[prediction]}")
//                    inferenceResultView.setText(labelList[prediction])
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                Toast.makeText(applicationContext, "Inference failed!", Toast.LENGTH_SHORT).show()
            }
        }
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
    }

}