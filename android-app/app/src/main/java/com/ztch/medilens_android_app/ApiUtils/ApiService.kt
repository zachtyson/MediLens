package com.ztch.medilens_android_app.ApiUtils
import com.google.gson.Gson
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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

    @POST("doctor/modify_doctor")
    fun modifyDoctor(
        @Header("token") token: String,
        @Body doctor: Doctor
    ): Call<Map<String, String>>

    @POST("doctor/add_doctor")
    fun addDoctor(
        @Header("token") token: String,
        @Body doctor: DoctorCreate
    ): Call<Map<String, String>>

    @POST("doctor/delete_doctor")
    fun deleteDoctor(
        @Header("token") token: String,
        @Body doctor: DoctorDelete
    ): Call<Map<String, String>>

    @GET("doctor/get_user_doctors")
    fun getUserDoctors(
        @Header("token") token: String
    ): Call<List<Doctor>>

    @POST("medication/add_medication")
    fun addMedication(
        @Header("token") token: String,
        @Body medication: MedicationCreate
    ): Call<Map<String, String>>

    @POST("medication/modify_medication")
    fun modifyMedication(
        @Header("token") token: String,
        @Body medication: MedicationModify
    ): Call<Map<String, String>>

    @GET("medication/get_medications")
    fun getMedications(
        @Header("token") token: String
    ): Call<List<Medication>>

    @GET("medication/get_scheduled_medications")
    fun getScheduledMedications(
        @Header("token") token: String
    ): Call<List<Medication>>

    @POST("medication/remove_medication_schedule")
    fun removeMedicationSchedule(
        @Header("token") token: String,
        @Query("medication_id") medicationId: Int,
        @Query("user_id") userId: Int
    ): Call<Map<String, String>>

    @GET("/medication/interactions")
    fun getMedicationInteractions(
        @Query("drug_a") drugA: String,
        @Query("drug_b") drugB: String
    ): Call<MedicationInteractionResponse>

    @POST("medication/get_all_user_interactions/")
    fun getAllInteractions(
        @Header("token") token: String,
        // Body contains user drugs and new drug
        @Body userDrugs: UserDrugs
    ): Call<List<MedicationInteractionResponse>>

    @FormUrlEncoded
    @POST("users/email/")
    fun updateEmail(
        @Header("token") token: String,
        @Field("old_email") oldEmail: String,
        @Field("new_email") newEmail: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("users/password_change")
    fun updatePassword(
        @Header("token") token: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("users/delete_account")
    fun deleteUser(
        @Header("token") token: String,
        @Field("password") password: String
        // Returns basic JSON response
    ): Call<Map<String, String>>

    @GET("medicard/user_info")
    fun getUserInfo(
        @Header("token") token: String,
    ): Call<UserInfoResponse>
}

data class UserInfoResponse(
    val name: String,
    val email: String
)

data class Doctor(
    val doctor_id: Int,
    val doctor_name: String?,
    val specialty: String?,
    val office_number: String?,
    val emergency_number: String?,
    val office_address: String?,
    val email: String,
    val owner_id: Int
)

data class DoctorCreateResponse(
    val doctor_id: Int,
    val doctor_name: String?,
    val specialty: String?,
    val office_number: String?,
    val emergency_number: String?,
    val office_address: String?,
    val email: String,
    val owner_id: Int
)
data class DoctorCreate(
    val doctor_name: String?,
    val specialty: String?,
    val office_number: String?,
    val emergency_number: String?,
    val office_address: String?,
    val email: String?
)

data class DoctorDelete(
    val doctor_id: Int
)

data class UserDrugs(
    var drugs: List<String> = listOf(),
)

data class MedicationInteractionResponse(
    val id: Int,
    val drug_a: String,
    val drug_b: String,
    val severity: String,
    val description: String,
    val extended_description: String
)
data class Medication(
    val id: Int,
    val created_date: Date?,
    val owner_id: Int,
    var name: String,
    var description: String?,
    var color: String?,
    var imprint: String?,
    var shape: String?,
    var dosage: String?,
    var intake_method: String?,
    var schedule_start: Date?,
    var interval_milliseconds: Long?,
    val init_vector: String,
)

data class MedicationCreate(
    val name: String,
    val description: String?,
    val color: String?,
    val imprint: String?,
    val shape: String?,
    val dosage: String?,
    val intake_method: String?,
    val init_vector: String
)

// class for modifying medication, this might be the same as the Medication class but I made it separate for clarity
data class MedicationModify(
    val id: Int,
    val owner_id: Int,
    val name: String,
    val description: String?,
    val color: String?,
    val imprint: String?,
    val shape: String?,
    val dosage: String?,
    val intake_method: String?,
    val schedule_start: String?,
    val interval_milliseconds: Long?,
    val init_vector: String
)

fun convertToLocalDateTime(date: Date): LocalDateTime {
    val instant = date.toInstant()
    return instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun formatDateTime(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy, 'at' hh:mm a", Locale.US)
    return formatter.format(localDateTime)
}

fun convertMillisecondsToHumanReadableTime(milliseconds: Long): String {
    val years = milliseconds / 31536000000
    val months = (milliseconds % 31536000000) / 2628000000
    val days = ((milliseconds % 31536000000) % 2628000000) / 86400000
    val hours = (((milliseconds % 31536000000) % 2628000000) % 86400000) / 3600000
    val minutes = ((((milliseconds % 31536000000) % 2628000000) % 86400000) % 3600000) / 60000
    val seconds = (((((milliseconds % 31536000000) % 2628000000) % 86400000) % 3600000) % 60000) / 1000
    var ret = ""
    if (years > 0) {
        ret += "$years year"
        if (years > 1) {
            ret += "s"
        }
    }
    if (months > 0) {
        if (ret.isNotEmpty()) {
            ret += ", "
        }
        ret += "$months month"
        if (months > 1) {
            ret += "s"
        }
    }
    if (days > 0) {
        if (ret.isNotEmpty()) {
            ret += ", "
        }
        ret += "$days day"
        if (days > 1) {
            ret += "s"
        }
    }
    if (hours > 0) {
        if (ret.isNotEmpty()) {
            ret += ", "
        }
        ret += "$hours hour"
        if (hours > 1) {
            ret += "s"
        }
    }
    if (minutes > 0) {
        if (ret.isNotEmpty()) {
            ret += ", "
        }
        ret += "$minutes minute"
        if (minutes > 1) {
            ret += "s"
        }
    }
    if (seconds > 0) {
        if (ret.isNotEmpty()) {
            ret += ", "
        }
        ret += "$seconds second"
        if (seconds > 1) {
            ret += "s"
        }
    }
    return ret
}

data class UserRegistrationCredentials(
    val name: String,
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
    var ocrParsed: List<OcrDetection>? = parseOCRResult(ocr.toString())
) {
    fun getOCRParsed(): List<OcrDetection>? {
        return parseOCRResult(ocr.toString())
    }
}

fun parseOCRResult(input: String): List<OcrDetection>? {
    val detections = mutableListOf<OcrDetection>()
    val inputAsList = Gson().fromJson(input, List::class.java)
    for(item in inputAsList) {
        val parsedItem = Gson().fromJson(item.toString(), List::class.java)
        //List<List<Int>>, text: Any, confidence: Double
        val ocrDetection = parsedItem[1]?.let {
            OcrDetection(
                boundingBoxAsList = parsedItem[0] as List<List<Int>>,
                text = it,
                confidence = parsedItem[2] as Double
            )
        }
        if(ocrDetection != null) {
            detections.add(ocrDetection)
        }

    }
    if(detections.isEmpty()) {
        return null
    }
    return detections

}

data class BoundingBox(
    val topLeft: Pair<Int,Int>,
    val topRight: Pair<Int,Int>,
    val bottomRight: Pair<Int,Int>,
    val bottomLeft: Pair<Int,Int>
) {

}

data class OcrDetection(
    val boundingBox: BoundingBox,
    // text is a generic type, it can be a string or a number
    val text: Any,
    val confidence: Double
) {
    constructor(boundingBoxAsList: List<List<Int>>, text: Any, confidence: Double) : this(
        boundingBox = BoundingBox(
            topLeft = Pair(boundingBoxAsList[0][0], boundingBoxAsList[0][1]),
            topRight = Pair(boundingBoxAsList[1][0], boundingBoxAsList[1][1]),
            bottomRight = Pair(boundingBoxAsList[2][0], boundingBoxAsList[2][1]),
            bottomLeft = Pair(boundingBoxAsList[3][0], boundingBoxAsList[3][1])
        ),
        text = text,
        confidence = confidence
    ) {}
    //toString() method to print the object
    override fun toString(): String {
        //OcrDetection
        //{
        //boundingBox {
        //  topLeft=[48, 86]
        //  topRight=[208, 86]
        //  bottomRight=[208, 166]
        //  bottomLeft=[48, 166]
        //  }
        //text=512.0
        //confidence=0.7369789298534946
        //}
        val topLeft = boundingBox.topLeft.first.toString() + ", " + boundingBox.topLeft.second.toString()
        val topRight = boundingBox.topRight.first.toString() + ", " + boundingBox.topRight.second.toString()
        val bottomRight = boundingBox.bottomRight.first.toString() + ", " + boundingBox.bottomRight.second.toString()
        val bottomLeft = boundingBox.bottomLeft.first.toString() + ", " + boundingBox.bottomLeft.second.toString()
        return "OcrDetection\n{\nboundingBox {\n  topLeft=$topLeft\n  topRight=$topRight\n  bottomRight=$bottomRight\n  bottomLeft=$bottomLeft\n  }\ntext=$text \nconfidence=$confidence\n}"
    }

    fun getText(): String {
        val formattedText = when (text) {
            is Double -> {
                val intValue = text.toInt()
                if (text == intValue.toDouble()) {
                    intValue.toString()
                } else {
                    text.toString()
                }
            }
            else -> text.toString()
        }
        return formattedText
    }
}

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
