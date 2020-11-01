package com.healthbuzz.healthbuzz

import androidx.lifecycle.MutableLiveData

object RealtimeModel {
    val stretching_time_left = MutableLiveData<Long>().also {
        it.value = 20
    }
    val water_time_left = MutableLiveData<Long>().also {
        it.value = 20
    }
    val stretching_count = MutableLiveData<Long>().also {
        it.value = 0
    }
    val water_count = MutableLiveData<Long>().also {
        it.value = 0
    }
}