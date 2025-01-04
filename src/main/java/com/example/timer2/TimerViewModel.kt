package com.example.timer2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class TimerData(
    val date:String,
    val elapsedTime: String,
    val selectedTime: String,
    val speed: String,
    val memo: String
)

class TimerViewModel : ViewModel() {
    private val _timerData = MutableLiveData<TimerData>()
    val timerData: LiveData<TimerData> get() = _timerData

    // TimerData 설정 메서드
    fun setTimerData(date: String, elapsedTime: String, selectedTime: String, speed: String, memo: String) {
        _timerData.value = TimerData(date, elapsedTime, selectedTime, speed, memo)
    }
}
