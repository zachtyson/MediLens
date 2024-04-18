package com.ztch.medilens_android_app.Notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ztch.medilens_android_app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class AlarmBroadcaster: BroadcastReceiver() {
    private val broadcasterJob = Job()
    private val broadcasterScope = CoroutineScope(Dispatchers.IO + broadcasterJob)

    override fun onReceive(context: Context?, intent: Intent?) {

        broadcasterScope.launch {
            // get instance of alarm from futureAlarm
            var alarmRepository = context?.let { AlarmRepository(it) }
            val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return@launch
            val requestCode = intent?.getIntExtra("EXTRA_REQUEST_CODE", 0) ?: return@launch
            Log.d("AlarmBroadcaster", "onReceive: $message")

            // Get an instance of the database and add the alarm to the database
            if (context == null) {
                return@launch
            }
            Log.d("AlarmBroadcaster", "onReceive: requestCode: $requestCode")
            val alarm = alarmRepository?.getFutureAlarmByRequestCode(requestCode) ?: return@launch
            // convert futureAlarm to pendingAlarm
            Log.d("AlarmBroadcaster", "onReceive: alarm: $alarm")
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
}
