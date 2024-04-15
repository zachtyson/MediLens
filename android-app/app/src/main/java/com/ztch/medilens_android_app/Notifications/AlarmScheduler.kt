package com.ztch.medilens_android_app.Notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.ztch.medilens_android_app.R
import okhttp3.internal.notify

import java.util.*


data class AlarmScheduler(
    val context: Context,
    val db: AlarmDatabase
) {

    val receiver = ComponentName(context, AlarmBroadcaster::class.java)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(item: AlarmItem) {
        val message = item.message
        val alarmId = item.id
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcaster::class.java).apply {
            putExtra("EXTRA_MESSAGE", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            requestExactAlarmPermission()
            return
        }

        val timeInMillis = item.startTimeMillis

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )

        saveAlarmDetails(item)
    }

    private fun saveAlarmDetails(item: AlarmItem) {
        db.alarmDao().insertAll(item)
    }

    fun cancelAllAlarms() {
        db.alarmDao().getAll().forEach(this::cancel)
        // clear all alarms from database
        db.alarmDao().deleteAll(*db.alarmDao().getAll().toTypedArray())
    }

    fun cancel(item: AlarmItem) {
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        val alarmId = item.id
        val intent = Intent(context, AlarmBroadcaster::class.java).apply {
            putExtra("EXTRA_MESSAGE", item.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(
            pendingIntent
        )
        // Remove alarm from database
        db.alarmDao().delete(item)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    }


}
