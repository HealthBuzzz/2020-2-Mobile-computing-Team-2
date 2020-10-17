package com.healthbuzz.healthbuzz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_dashboard.view.*

/**
 * A fragment representing a single Item detail screen.
 * This fragment is contained in a [MainActivity]
 * on handsets.
 */
class DashboardFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val stretchingDrawable = ResourcesCompat.getDrawable(resources, R.drawable.stretching, null)
        val waterDrawable = ResourcesCompat.getDrawable(resources, R.drawable.drink_water, null)
        rootView.cardview_layout_stretching.findViewById<ImageView>(R.id.ivCardImage)
            .setImageDrawable(stretchingDrawable)
        rootView.cardview_layout_water.findViewById<ImageView>(R.id.ivCardImage)
            .setImageDrawable(waterDrawable)


        return rootView
    }
}