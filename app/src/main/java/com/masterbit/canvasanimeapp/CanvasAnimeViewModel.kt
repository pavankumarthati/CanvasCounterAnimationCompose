package com.masterbit.canvasanimeapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CanvasAnimeViewModel @Inject constructor(): ViewModel() {

    private var job: Job? = null

    val timeState = MutableLiveData<Long>(0)
    val alertState = MutableLiveData<Boolean>(false)
    val alertMessage = MutableLiveData<Boolean>(false)

    fun setupAndInitTime(time: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            timeState.postValue(time)
            alertState.postValue(false)
            alertMessage.postValue(false)
        }
    }

    fun startTimer() {
        stop()
        job = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (timeState.value!! <= 0L) {
                    job?.cancel()
                    return@launch
                }

                alertState.postValue(timeState.value!! - 1 <= 5)
                alertMessage.postValue(timeState.value!! - 1 <= 0)
                timeState.postValue((timeState.value!! - 1).coerceAtLeast(0L))
                delay(1_000)
            }
        }
    }

    fun stop(time: Long = 0) {
        job?.cancel()
        if (time > 0) {
            setupAndInitTime(time)
        }
    }

    fun addTime(time: Long = 10) {
        viewModelScope.launch(Dispatchers.IO) {
            timeState.postValue(timeState.value!! + time)
        }
    }
}