package com.ztch.medilens_android_app.Notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmBroadcaster: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        println("Alarm triggered: $message")
    }
}