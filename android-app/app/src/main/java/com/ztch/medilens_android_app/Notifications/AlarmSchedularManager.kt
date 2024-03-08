package com.ztch.medilens_android_app.Notifications




interface AlarmSchedulerManager {
    fun schedule(item: AlarmItem)
    fun cancel(item: AlarmItem)
}