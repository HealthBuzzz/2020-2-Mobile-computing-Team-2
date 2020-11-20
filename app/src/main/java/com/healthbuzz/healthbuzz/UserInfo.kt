package com.healthbuzz.healthbuzz

import androidx.lifecycle.MutableLiveData

object UserInfo {
    val userName = MutableLiveData<String>().also {
        it.value = ""
    }
}