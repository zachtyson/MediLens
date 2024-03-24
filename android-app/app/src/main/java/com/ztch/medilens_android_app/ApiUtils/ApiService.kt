package com.ztch.medilens_android_app.ApiUtils
import android.annotation.SuppressLint
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.http.*
import java.io.ByteArrayOutputStream
import java.io.IOException

interface ApiService {
    @FormUrlEncoded
    @POST("login/e/")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginTokenResponse> // TokenResponse is a data class you'll define to match the JSON response
    @POST("users/")
    fun createUser(@Body credentials: UserRegistrationCredentials): Call<RegisterResponse>

    @Multipart
    @POST("predict")
    fun uploadImageAndGetPrediction(
        @Part image: MultipartBody.Part
    ): Call<PredictionResponse>
}

data class UserRegistrationCredentials(
    val email: String,
    val password: String,
)

data class Prediction(
    val x1: Double,
    val y1: Double,
    val x2: Double,
    val y2: Double,
    val confidence: Double,
    val classId: Int,
    val name: String,
    val color: String?,
    val shape: String?,
    val ocr: List<List<Any>>?
)

data class PredictionResponse(
    val predictions: List<Prediction>
)
