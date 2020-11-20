package com.healthbuzz.healthbuzz

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import com.healthbuzz.healthbuzz.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*


/**
 * A fragment representing a single Item detail screen.
 * This fragment is contained in a [MainActivity]
 * on handsets.
 */
class DashboardFragment : Fragment() {
    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            val binder = service as SensorService.SensorBinder
//            mService = binder.getService()
//            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
//            mBound = false
        }
    }

    //    override fun onStop() {
//        super.onStop()
//        context?.unbindService(connection)
//        mBound = false
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val stretchingDrawable = ResourcesCompat.getDrawable(resources, R.drawable.stretching, null)
        val waterDrawable = ResourcesCompat.getDrawable(resources, R.drawable.drink_water, null)
//        val runDrawable = ResourcesCompat.getDrawable(resources, R.drawable.run, null)

        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        var timeIntervalStretchMin: String =
            prefs.getString("time_interval_stretch", "20") ?: "20"
        if (timeIntervalStretchMin.isEmpty())
            timeIntervalStretchMin = "20"

        var timeIntervalWaterMin: String =
            prefs.getString("time_interval_water", "20") ?: "20"
        if (timeIntervalWaterMin.isEmpty())
            timeIntervalWaterMin = "20"

//        var timeIntervalRunMin: String =
//            prefs.getString("time_interval_run", "20") ?: "20"
//        if (timeIntervalRunMin.isEmpty())
//            timeIntervalRunMin = "20"


        val stretchIntervalSec = Integer.parseInt(timeIntervalStretchMin) * 60
        val waterIntervalSec = Integer.parseInt(timeIntervalWaterMin) * 60
//        val runIntervalSec = Integer.parseInt(timeIntervalRunMin) * 60


        RealtimeModel.stretching_time_left.value = stretchIntervalSec.toLong()
        RealtimeModel.water_time_left.value = waterIntervalSec.toLong()
//        RealtimeModel.run_time_left.value = runIntervalSec.toLong()


        RealtimeModel.stretching_time_left.observe(viewLifecycleOwner) { value ->
            val intValue = value?.toInt() ?: stretchIntervalSec
            Log.e(TAG, "update value $value")
            rootView.cardview_layout_stretching.findViewById<TextView>(R.id.tvCardContent).text =
                if (intValue > 0) {
                    formatTime(requireContext(), intValue)
                } else {
                    getString(R.string.you_need_stretch)
                }
            rootView.cardview_layout_stretching.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
                .let {
                    it.secondaryProgress = 0F
                    it.max = stretchIntervalSec.toFloat()
                    it.progress = value.toFloat()
                }
        }

        RealtimeModel.water_time_left.observe(viewLifecycleOwner) { value ->
            val intValue = value?.toInt() ?: waterIntervalSec
            Log.e(TAG, "update value2 $value")
            rootView.cardview_layout_water.findViewById<TextView>(R.id.tvCardContent).text =
                if (intValue > 0) {
                    formatTime(requireContext(), intValue)
                } else {
                    getString(R.string.you_need_drink)
                }
            rootView.cardview_layout_water.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
                .let {
                    it.secondaryProgress = 0F
                    it.max = waterIntervalSec.toFloat()
                    it.progress = value.toFloat()
                }
        }

//        RealtimeModel.run_time_left.observe(viewLifecycleOwner) { value ->
//            val intValue = value?.toInt() ?: runIntervalSec
//            Log.e(TAG, "update value3 $value")
//            rootView.cardview_layout_running.findViewById<TextView>(R.id.tvCardContent).text =
//                if (intValue > 0) {
//                    formatTime(requireContext(), intValue)
//                } else {
//                    getString(R.string.you_need_run)
//                }
//            rootView.cardview_layout_running.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
//                .let {
//                    it.secondaryProgress = 0F
//                    it.max = waterIntervalSec.toFloat()
//                    it.progress = value.toFloat()
//                }
//        }

        with(rootView) {
            cardview_layout_stretching.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorStretchingLight,
                        null
                    )
                )

            cardview_layout_water.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorWaterLight,
                        null
                    )
                )

//            cardview_layout_running.findViewById<ConstraintLayout>(R.id.cardview_root)
//                .setBackgroundColor(
//                    ResourcesCompat.getColor(
//                        resources,
//                        R.color.colorRunLight,
//                        null
//                    )
//                )

            cardview_layout_stretching.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
                .apply {
                    progressBackgroundColor =
                        ResourcesCompat.getColor(
                            resources,
                            R.color.colorStretching,
                            null
                        )
                    progressColor =
                        ResourcesCompat.getColor(
                            resources,
                            R.color.colorStretchingDark,
                            null
                        )
                }

            cardview_layout_water.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
                .apply {
                    progressBackgroundColor =
                        ResourcesCompat.getColor(
                            resources,
                            R.color.colorWater,
                            null
                        )
                    progressColor = ResourcesCompat.getColor(
                        resources,
                        R.color.colorWaterDark,
                        null
                    )
                }

//            cardview_layout_running.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
//                .apply {
//                    progressBackgroundColor =
//                        ResourcesCompat.getColor(
//                            resources,
//                            R.color.colorRun,
//                            null
//                        )
//                    progressColor = ResourcesCompat.getColor(
//                        resources,
//                        R.color.colorRunDark,
//                        null
//                    )
//                }

            cardview_layout_stretching.findViewById<ImageView>(R.id.ivCardImage)
                .setImageDrawable(stretchingDrawable)
            cardview_layout_water.findViewById<ImageView>(R.id.ivCardImage)
                .setImageDrawable(waterDrawable)
//            cardview_layout_running.findViewById<ImageView>(R.id.ivCardImage)
//                .setImageDrawable(runDrawable)

            cardview_layout_stretching.findViewById<TextView>(R.id.tvCardTitle)
                .setText(R.string.dashboard_stretching_title_default)
            cardview_layout_water.findViewById<TextView>(R.id.tvCardTitle)
                .setText(R.string.dashboard_water_title_default)
//            cardview_layout_running.findViewById<TextView>(R.id.tvCardTitle)
//                .setText(R.string.dashboard_run_title_default)

            cardview_layout_stretching.findViewById<TextView>(R.id.tvCardContent).text =
                (RealtimeModel.stretching_time_left.value?.toInt() ?: stretchIntervalSec).let {
                    val intVal = it
                    formatTime(context, intVal)
                }
            cardview_layout_water.findViewById<TextView>(R.id.tvCardContent).text =
                (RealtimeModel.water_time_left.value?.toInt() ?: waterIntervalSec).let {
                    val intVal = it
                    formatTime(context, intVal)
                }
//            cardview_layout_running.findViewById<TextView>(R.id.tvCardContent).text =
//                (RealtimeModel.run_time_left.value?.toInt() ?: runIntervalSec).let {
//                    val intVal = it
//                    formatTime(context, intVal)
//                }


            cardview_layout_stretching.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, StretchingDetailActivity::class.java))
                    //startActivity((Intent(context, LoginActivity::class.java)))
                }
            cardview_layout_water.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, WaterDetailActivity::class.java))
                }

            cardview_layout_stretching.findViewById<SwitchCompat>(R.id.swCardEnable)
                .apply {
                    isChecked = prefs.getBoolean("sync", true)
                }
                .setOnCheckedChangeListener { buttonView, checked ->
                    disableEnableControls(checked, cardview_layout_stretching as ViewGroup)
                    buttonView.isEnabled = true
                    val editor = prefs.edit()
                    editor.putBoolean("sync", checked)
                    editor.apply()
                }

            cardview_layout_water.findViewById<SwitchCompat>(R.id.swCardEnable)
                .apply {
                    isChecked = prefs.getBoolean("sync2", true)
                }
                .setOnCheckedChangeListener { buttonView, checked ->
                    disableEnableControls(checked, cardview_layout_water as ViewGroup)
                    buttonView.isEnabled = true
                    val editor = prefs.edit()
                    editor.putBoolean("sync2", checked)
                    editor.apply()
                }

//            cardview_layout_running.findViewById<SwitchCompat>(R.id.swCardEnable)
//                .apply {
//                    isChecked = prefs.getBoolean("sync3", true)
//                }
//                .setOnCheckedChangeListener { buttonView, checked ->
//                    disableEnableControls(checked, cardview_layout_running as ViewGroup)
//                    buttonView.isEnabled = true
//                    val editor = prefs.edit()
//                    editor.putBoolean("sync3", checked)
//                    editor.apply()
//                }
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

        cardview_layout_water.findViewById<SwitchCompat>(R.id.swCardEnable)
            .apply {
                isChecked = prefs.getBoolean("sync2", true)
            }
        cardview_layout_stretching.findViewById<SwitchCompat>(R.id.swCardEnable)
            .apply {
                isChecked = prefs.getBoolean("sync", true)
            }
//        cardview_layout_running.findViewById<SwitchCompat>(R.id.swCardEnable)
//            .apply {
//                isChecked = prefs.getBoolean("sync3", true)
//            }


    }
}