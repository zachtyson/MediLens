package com.ztch.medilens_android_app.Notifications

import android.net.Uri
import java.time.LocalDateTime

data class AlarmItem(
    val time: LocalDateTime,
    val message: String, // medication name
    val dosage: String,
    val strength: String,
    val RX: String,
    val form: String,
    val repetition: Repetition,
    val imageUri: Uri?
)

enum class Repetition { EVERY_DAY, ONCE, HOURLY, WEEKLY }
