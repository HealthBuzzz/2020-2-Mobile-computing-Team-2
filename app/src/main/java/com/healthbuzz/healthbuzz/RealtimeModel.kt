package com.healthbuzz.healthbuzz

import androidx.lifecycle.MutableLiveData
import com.healthbuzz.healthbuzz.data.model.LoggedInUser

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
}

