package com.ztch.medilens_android_app.Notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ztch.medilens_android_app.R


class AlarmBroadcaster: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        var alarmRepository: AlarmRepository? = null
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        val requestCode = intent?.getIntExtra("REQUEST_CODE", -1) ?: return
        Log.d("AlarmBroadcaster", "onReceive: $message")

        // Get an instance of the database and add the alarm to the database
        if (context == null) {
            return
        }
        alarmRepository = AlarmRepository(context)
        // get instance of alarm from futureAlarm
        val alarm = alarmRepository?.getFutureAlarmByRequestCode(requestCode) ?: return
        // convert futureAlarm to pendingAlarm
        alarmRepository.convertFutureAlarmToPendingAlarm(alarm)

        context?.let { ctx ->
            val notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(ctx, "alarm_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Pill Reminder")
                .setContentText(
                    if(message != null) {
                        "Time to take your $message"
                    } else {
                        "Time to take your pills!"
                    }
                )
                .build()

            notificationManager.notify(1, notification)
        }

    }
}
