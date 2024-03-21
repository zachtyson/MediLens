package com.ztch.medilens_android_app.ApiUtils

data class RegisterResponse(
    val email: String,
    val id: Int,
    val created_date: String //It's actually a datetime but it's not used in the app so it doesn't matter
)
