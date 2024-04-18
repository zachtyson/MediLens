package com.ztch.medilens_android_app.Notifications

import android.content.Context

class AlarmRepository (context: Context) {
    val db = AlarmDatabase.getInstance(context)
    val alarmDao = db.alarmDao()
    val pastAlarmDao = db.pastAlarmDao()
    val futureAlarmDao = db.futureAlarmDao()
    val pendingAlarmDao = db.pendingAlarmDao()

    suspend fun getPastAlarmByRequestCode(requestCode: Int): PastAlarmItem? {
        return pastAlarmDao.getByRequestCode(requestCode)
    }

    fun getFutureAlarmByRequestCode(requestCode: Int): FutureAlarmItem? {
        return futureAlarmDao.getByRequestCode(requestCode)
    }

    fun getPendingAlarmByRequestCode(requestCode: Int): PendingAlarmItem? {
        return pendingAlarmDao.getByRequestCode(requestCode)
    }

    fun insertPastAlarm(pastAlarm: PastAlarmItem) {
        pastAlarmDao.insert(pastAlarm)
    }

    fun insertFutureAlarm(futureAlarm: FutureAlarmItem) {
        futureAlarmDao.insert(futureAlarm)
    }

    fun insertPendingAlarm(pendingAlarm: PendingAlarmItem) {
        pendingAlarmDao.insert(pendingAlarm)
    }

    fun deleteFutureAlarm(futureAlarm: FutureAlarmItem) {
        futureAlarmDao.delete(futureAlarm)
    }

    fun deletePendingAlarm(pendingAlarm: PendingAlarmItem) {
        pendingAlarmDao.delete(pendingAlarm)
    }

    fun deletePastAlarm(pastAlarm: PastAlarmItem) {
        pastAlarmDao.delete(pastAlarm)
    }

    // All three types of alarms have the exact same fields so this shouldn't be a problem
    fun convertFutureAlarmToPendingAlarm(futureAlarm: FutureAlarmItem) {
        val pendingAlarm = PendingAlarmItem(
            message = futureAlarm.message,
            timeMillis = futureAlarm.timeMillis,
            imageUri = futureAlarm.imageUri,
            response = futureAlarm.response,
            requestCode = futureAlarm.requestCode
        )
        insertPendingAlarm(pendingAlarm)
        deleteFutureAlarm(futureAlarm)
    }

    fun convertPendingAlarmToPastAlarm(pendingAlarm: PendingAlarmItem) {
        val pastAlarm = PastAlarmItem(
            message = pendingAlarm.message,
            timeMillis = pendingAlarm.timeMillis,
            imageUri = pendingAlarm.imageUri,
            response = pendingAlarm.response,
            requestCode = pendingAlarm.requestCode
        )
        insertPastAlarm(pastAlarm)
        deletePendingAlarm(pendingAlarm)
    }

    // No need to convert past alarms to anything else, since at that point they would probably just get deleted



}
