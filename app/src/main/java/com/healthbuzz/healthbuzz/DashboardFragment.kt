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


        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        var time_interval_stretch: String =
            prefs.getString("time_interval_stretch", "20") ?: "20"
        if (time_interval_stretch.isEmpty())
            time_interval_stretch = "20"

        var time_interval_water: String =
            prefs.getString("time_interval_water", "20") ?: "20"
        if (time_interval_water.isEmpty())
            time_interval_water = "20"


        val stretch_interval = Integer.parseInt(time_interval_stretch)
        val water_interval = Integer.parseInt(time_interval_water)

        RealtimeModel.stretching_time_left.value = stretch_interval.toLong()
        RealtimeModel.water_time_left.value = water_interval.toLong()


        RealtimeModel.stretching_time_left.observe(viewLifecycleOwner) { value ->
            val intValue = value?.toInt() ?: stretch_interval
            Log.e(TAG, "update value $value")
            rootView.cardview_layout_stretching.findViewById<TextView>(R.id.tvCardContent).text =
                if (intValue >= 0) {
                    getString(R.string.dashboard_minutes_left, intValue)
                } else {
                    "You need to stretch now"
                }
        }
        RealtimeModel.water_time_left.observe(viewLifecycleOwner) { value ->
            val intValue = value?.toInt() ?: water_interval
            Log.e(TAG, "update value2 $value")
            rootView.cardview_layout_water.findViewById<TextView>(R.id.tvCardContent).text =
                if (intValue >= 0) {
                    getString(R.string.dashboard_minutes_left, intValue)
                } else {
                    "You need to drink water now"
                }
        }

//        SingleObject.getInstance().stretching_time_left.registerObserver { value ->
//            Log.e(TAG, "update value ${value}")
//            rootView.cardview_layout_stretching.findViewById<TextView>(R.id.tvCardContent).text =
//                getString(R.string.dashboard_minutes_left, value)
//        }
//
//        SingleObject.getInstance().water_time_left.registerObserver { value ->
//            Log.e(TAG, "update value2 ${value}")
//            rootView.cardview_layout_water.findViewById<TextView>(R.id.tvCardContent).text =
//                getString(R.string.dashboard_minutes_left, value)
//        }


        with(rootView) {
            cardview_layout_stretching.findViewById<ImageView>(R.id.ivCardImage)
                .setImageDrawable(stretchingDrawable)
            cardview_layout_water.findViewById<ImageView>(R.id.ivCardImage)
                .setImageDrawable(waterDrawable)
            cardview_layout_stretching.findViewById<TextView>(R.id.tvCardTitle)
                .setText(R.string.dashboard_stretching_title_default)
            cardview_layout_water.findViewById<TextView>(R.id.tvCardTitle)
                .setText(R.string.dashboard_water_title_default)
            cardview_layout_stretching.findViewById<TextView>(R.id.tvCardContent).text =
                (RealtimeModel.stretching_time_left.value?.toInt() ?: stretch_interval).let {
                    val intVal = it
                    getString(
                        R.string.dashboard_minutes_left,
                        intVal
                    )
                }
            cardview_layout_water.findViewById<TextView>(R.id.tvCardContent).text =
                (RealtimeModel.water_time_left.value?.toInt() ?: water_interval).let {
                    val intVal = it
                    getString(
                        R.string.dashboard_minutes_left,
                        intVal
                    )
                }

            cardview_layout_stretching.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, StretchingDetailActivity::class.java))
                }
            cardview_layout_water.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, BluetoothActivity::class.java))
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
    }
}