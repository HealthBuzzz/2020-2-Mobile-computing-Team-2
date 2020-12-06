package com.healthbuzz.healthbuzz


import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import com.healthbuzz.healthbuzz.RealtimeModel.ranking_stretch
import com.healthbuzz.healthbuzz.RealtimeModel.ranking_water
import com.healthbuzz.healthbuzz.UserInfo.userName
import com.healthbuzz.healthbuzz.data.CommunityActivity
import com.healthbuzz.healthbuzz.data.LoginDataSource
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*


/**
 * A fragment representing a single Item detail screen.
 * This fragment is contained in a [MainActivity]
 * on handsets.
 */
class DashboardFragment : Fragment() {
    /** Defines callbacks for service binding, passed to bindService()  */
    var mBound = false
    var mService: SensorService? = null
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SensorService.SensorBinder
            mService = binder.getService()
            mBound = true
            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            val binder = service as SensorService.SensorBinder
//            mService = binder.getService()
//            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            mService = null
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
        val stretchingDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.stretchicon, null)
        val waterDrawable = ResourcesCompat.getDrawable(resources, R.drawable.drinkicon, null)
        val rankingDrawable = ResourcesCompat.getDrawable(resources, R.drawable.users, null)


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

        val stretchIntervalSec = resetStretchTime(prefs)
        val waterIntervalSec = resetWaterTime(prefs)

        RealtimeModel.stretching_time_left.value = stretchIntervalSec.toLong()
        RealtimeModel.water_time_left.value = waterIntervalSec.toLong()


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
        /*
        RealtimeModel.run_time_left.observe(viewLifecycleOwner) { value ->
            val intValue = value?.toInt() ?: runIntervalSec
            Log.e(TAG, "update value3 $value")
            rootView.cardview_layout_running.findViewById<TextView>(R.id.tvCardContent).text =
                if (intValue > 0) {
                    formatTime(requireContext(), intValue)
                } else {
                    getString(R.string.you_need_run)
                }
            rootView.cardview_layout_running.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
                .let {
                    it.secondaryProgress = 0F
                    it.max = waterIntervalSec.toFloat()
                    it.progress = value.toFloat()
                }
        }*/

        with(rootView) {
            /*cardview_layout_stretching.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorStretchingLight,
                        null
                    )
                )*/

            /*cardview_layout_water.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorWaterLight,
                        null
                    )
                )*/

            /*cardview_layout_community.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorRunLight,
                        null
                    )
                )*/

            cardview_layout_stretching.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
                .apply {
                    progressBackgroundColor =
                        ResourcesCompat.getColor(
                            resources,
                            R.color.progressBarBack,
                            null
                        )
                    progressColor =
                        ResourcesCompat.getColor(
                            resources,
                            R.color.progressBarFront,
                            null
                        )
                }

            cardview_layout_water.findViewById<RoundCornerProgressBar>(R.id.pbCardProgress)
                .apply {
                    progressBackgroundColor =
                        ResourcesCompat.getColor(
                            resources,
                            R.color.progressBarBack,
                            null
                        )
                    progressColor = ResourcesCompat.getColor(
                        resources,
                        R.color.progressBarFront,
                        null
                    )
                }


            cardview_layout_stretching.findViewById<ImageView>(R.id.ivCardImage)
                .setImageDrawable(stretchingDrawable)
            cardview_layout_water.findViewById<ImageView>(R.id.ivCardImage)
                .setImageDrawable(waterDrawable)
            cardview_layout_community.findViewById<ImageView>(R.id.ivCardImage)
                .setImageDrawable(rankingDrawable)

            cardview_layout_stretching.findViewById<TextView>(R.id.tvCardTitle)
                .setText(R.string.dashboard_stretching_title_default)
            cardview_layout_water.findViewById<TextView>(R.id.tvCardTitle)
                .setText(R.string.dashboard_water_title_default)
            cardview_layout_community.findViewById<TextView>(R.id.tvCardTitle)
                .setText(R.string.dashboard_community_title)

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

            cardview_layout_stretching.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, StretchingDetailActivity::class.java))
                }
            cardview_layout_water.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    startActivity(Intent(context, WaterDetailActivity::class.java))
                }
            cardview_layout_community.findViewById<ConstraintLayout>(R.id.cardview_root)
                .setOnClickListener {
                    if (!userName.value.equals("")) {
                        startActivity(Intent(context, CommunityActivity::class.java))
                    }
                }


            cardview_layout_stretching.findViewById<SwitchCompat>(R.id.swCardEnable)
                .apply {
                    isChecked = prefs.getBoolean("sync", false)
                }
                .setOnCheckedChangeListener { buttonView, checked ->
                    disableEnableControls(checked, cardview_layout_stretching as ViewGroup)
                    buttonView.isEnabled = true
                    val editor = prefs.edit()
                    editor.putBoolean("sync", checked)
                    editor.apply()
                    resetStretchTime(prefs)
                }
            cardview_layout_water.findViewById<SwitchCompat>(R.id.swCardEnable)
                .apply {
                    isChecked = prefs.getBoolean("sync2", false)
                }
                .setOnCheckedChangeListener { buttonView, checked ->
                    disableEnableControls(checked, cardview_layout_water as ViewGroup)
                    buttonView.isEnabled = true
                    val editor = prefs.edit()
                    editor.putBoolean("sync2", checked)
                    editor.apply()
                    resetWaterTime(prefs)
                }
        }

        return rootView
    }

    private fun resetWaterTime(prefs: SharedPreferences): Int {
        var timeIntervalWaterMin: String =
            prefs.getString("time_interval_water", "20") ?: "20"
        if (timeIntervalWaterMin.isEmpty())
            timeIntervalWaterMin = "20"

        val waterIntervalSec = Integer.parseInt(timeIntervalWaterMin) * 60
        RealtimeModel.water_time_left.value = waterIntervalSec.toLong()
        return waterIntervalSec
    }

    private fun resetStretchTime(prefs: SharedPreferences): Int {
        var timeIntervalStretchMin: String =
            prefs.getString("time_interval_stretch", "20") ?: "20"
        if (timeIntervalStretchMin.isEmpty())
            timeIntervalStretchMin = "20"

        val stretchIntervalSec = Integer.parseInt(timeIntervalStretchMin) * 60
        if (mBound) {
            mService?.resetStretchTime()
        }
        RealtimeModel.stretching_time_left.value = stretchIntervalSec.toLong()

        return stretchIntervalSec
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ranking_stretch.observe(viewLifecycleOwner, { aLong: Long? ->
            val rankingView = requireView().findViewById<TextView>(R.id.ranking)
            if (aLong == null || aLong.toInt() == (-1)) {
                rankingView.setText("Login First!")
            } else {
                rankingView.setText("TOP $aLong% Stretcher")
            }
        })
        ranking_water.observe(viewLifecycleOwner, { aLong: Long? ->
            val rankingView = requireView().findViewById<TextView>(R.id.ranking2)
            if (aLong == null || aLong.toInt() == (-1)) {
                rankingView.setText("")
            } else {
                rankingView.setText("TOP $aLong% WaterDrinker")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

        cardview_layout_water.findViewById<SwitchCompat>(R.id.swCardEnable)
            .apply {
                isChecked = prefs.getBoolean("sync2", false)
            }
        cardview_layout_stretching.findViewById<SwitchCompat>(R.id.swCardEnable)
            .apply {
                isChecked = prefs.getBoolean("sync", false)
            }
        if (!userName.getValue().equals("")) {
            LoginDataSource.getTodayData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBound)
            context?.unbindService(connection)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.bindService(
            Intent(requireActivity(), SensorService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }
}