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

    @GET("pill_from_imprint-demo/")
    fun pillFromImprintDemo(
        @Query("imprint") imprint: String,
        @Query("color") color: Int,
        @Query("shape") shape: Int
    ): Call<List<PillInfoResponse>>
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
    val ocr: List<List<Any>>?,
    val ocrParsed: List<OcrDetection>? = parseOCRResult(ocr.toString())
)

fun parseOCRResult(input: String): List<OcrDetection>? {
    val detections = mutableListOf<OcrDetection>()
    val regex = Regex("""\[\(\[\[(\d+), (\d+)\], \[(\d+), (\d+)\], \[(\d+), (\d+)\], \[(\d+), (\d+)\]\], '(.+)', (\d+\.\d+)\)""")
    regex.findAll(input).forEach {
        val (x1, y1, x2, y2, x3, y3, x4, y4, text, confidence) = it.destructured
        val boundingBox = BoundingBox(
            topLeft = Pair(x1.toInt(), y1.toInt()),
            topRight = Pair(x2.toInt(), y2.toInt()),
            bottomRight = Pair(x3.toInt(), y3.toInt()),
            bottomLeft = Pair(x4.toInt(), y4.toInt())
        )
        detections.add(OcrDetection(boundingBox, text, confidence.toDouble()))
    }
    if(detections.isEmpty()) {
        return null
    }
    return detections

}

data class BoundingBox(
    val topLeft: Pair<Int, Int>,
    val topRight: Pair<Int, Int>,
    val bottomRight: Pair<Int, Int>,
    val bottomLeft: Pair<Int, Int>
)

data class OcrDetection(
    val boundingBox: BoundingBox,
    val text: String,
    val confidence: Double
)

data class PredictionResponse(
    val predictions: List<Prediction>
)

data class PillInfoResponse(
    val imageURL: String,
    val pillName: String,
    val strength: String,
    val imprint: String,
    val color: String,
    val shape: String
)
