package com.ztch.medilens_android_app.Notifications

import java.time.LocalDateTime

data class AlarmItem(
    val time: LocalDateTime,
    val message: String, // medication name
    val dosage: String,
    val strength: String,
    val RX: String,
    val form: String,
    val repetition: Repetition
)

enum class Repetition { EVERY_DAY, ONCE, HOURLY, WEEKLY }
