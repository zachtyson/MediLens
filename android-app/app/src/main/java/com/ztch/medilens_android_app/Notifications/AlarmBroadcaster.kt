package com.ztch.medilens_android_app.Notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ztch.medilens_android_app.R


class AlarmBroadcaster: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

            val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
            println("Alarm triggered: $message")
            val channelId = "alarm_id"

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