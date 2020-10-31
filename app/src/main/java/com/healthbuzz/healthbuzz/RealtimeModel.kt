package com.healthbuzz.healthbuzz

import androidx.lifecycle.MutableLiveData

object RealtimeModel {
    val stretching_time_left = MutableLiveData<Long>()
    val water_time_left = MutableLiveData<Long>()
    val stretching_count = MutableLiveData<Long>()
    val water_count = MutableLiveData<Long>()
}