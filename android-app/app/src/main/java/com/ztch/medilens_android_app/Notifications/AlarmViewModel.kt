package com.ztch.medilens_android_app.Notifications

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
     val _alarms = mutableStateListOf<AlarmItem>()
    private val scheduler: AlarmScheduler = AlarmScheduler(application.applicationContext)

    fun addAlarm(alarm: AlarmItem) {
        _alarms.add(alarm)
    }

    fun removeAlarm(alarm: AlarmItem) {
        _alarms.remove(alarm)
        alarm.let(scheduler::cancel)
    }
}