package com.healthbuzz.healthbuzz

import androidx.lifecycle.MutableLiveData
import com.healthbuzz.healthbuzz.data.model.Community
import com.healthbuzz.healthbuzz.data.model.YearData

// unit: seconds
object RealtimeModel {
    val stretching_time_left = MutableLiveData<Long>().also {
        it.value = 1200
    }
    val water_time_left = MutableLiveData<Long>().also {
        it.value = 1200
    }
    val stretching_count = MutableLiveData<Long>().also {
        it.value = 0
    }
    val water_count = MutableLiveData<Long>().also {
        it.value = 0
    }
    val ranking_stretch = MutableLiveData<Long?>().also {
        it.value = null
    }
    val ranking_water = MutableLiveData<Long?>().also {
        it.value = null
    }
    val year_data = MutableLiveData<List<YearData>?>().also {
        it.value = null
    }
    val community = MutableLiveData<Community?>().also {
        it.value = null
    }

    val waterDummy = MutableLiveData<String>().also {
        it.value = ""
    }
}

