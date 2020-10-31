package com.healthbuzz.healthbuzz

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Intent(context, SensorService::class.java).also { intent ->
//            context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        }
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

        RealtimeModel.stretching_time_left.observe(viewLifecycleOwner) { value ->
            Log.e(TAG, "update value ${value}")
            rootView.cardview_layout_stretching.findViewById<TextView>(R.id.tvCardContent).text =
                getString(R.string.dashboard_minutes_left, value)
        }
        RealtimeModel.water_time_left.observe(viewLifecycleOwner) { value ->
            Log.e(TAG, "update value2 ${value}")
            rootView.cardview_layout_water.findViewById<TextView>(R.id.tvCardContent).text =
                getString(R.string.dashboard_minutes_left, value)
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
                getString(
                    R.string.dashboard_minutes_left,
                    RealtimeModel.stretching_time_left.value
                )
            cardview_layout_water.findViewById<TextView>(R.id.tvCardContent).text =
                getString(
                    R.string.dashboard_minutes_left,
                    RealtimeModel.water_time_left.value
                )

            cardview_layout_stretching.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, StretchingDetailActivity::class.java))
                }
            cardview_layout_water.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, WaterDetailActivity::class.java))
                }
        }

        return rootView
    }
}