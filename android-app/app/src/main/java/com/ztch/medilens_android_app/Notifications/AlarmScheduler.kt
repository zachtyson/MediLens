package com.ztch.medilens_android_app.Notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class AlarmScheduler(
    private val context: Context
) : AlarmSchedulerManager {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    @SuppressLint("ScheduleExactAlarm")
    override fun schedule(item: AlarmItem) {
        val intent = Intent(context, AlarmBroadcaster::class.java).apply {
            putExtra("EXTRA_MESSAGE", item.message)
            putExtra("EXTRA_REPETITION", item.repetition.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMillis = when (item.repetition) {
            Repetition.NONE -> item.time.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            Repetition.EVERY_DAY -> {
                val now = LocalDateTime.now()
                val nextTime = if (item.time.isBefore(now)) now.plusDays(1) else now
                nextTime.withHour(item.time.hour)
                    .withMinute(item.time.minute)
                    .withSecond(0)
                    .atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            }
            Repetition.ONCE -> item.time.toEpochSecond(ZoneOffset.UTC) * 1000
            Repetition.HOURLY -> {
                val now = LocalDateTime.now()
                val nextHour = if (item.time.isBefore(now)) now.hour + 1 else now.hour
                val nextTime = item.time.withHour(nextHour).withMinute(0).withSecond(0)
                nextTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            }
            Repetition.WEEKLY -> {
                val now = LocalDateTime.now()
                val daysDiff = ChronoUnit.DAYS.between(now, item.time)
                val triggerTimeMillis = System.currentTimeMillis() + daysDiff * 24 * 60 * 60 * 1000
                val nextTime = item.time.withSecond(0)
                triggerTimeMillis + nextTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            }
        }

        Log.d("AlarmTest", "Test alarm scheduled for: $triggerMillis")
        if (item.repetition == Repetition.HOURLY) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                AlarmManager.INTERVAL_HOUR,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    override fun cancel(item: AlarmItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                Intent(context, AlarmBroadcaster::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}
