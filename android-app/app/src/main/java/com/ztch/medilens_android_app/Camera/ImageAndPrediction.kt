package com.ztch.medilens_android_app.Camera

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.ztch.medilens_android_app.ApiUtils.PredictionResponse

data class ImageAndPrediction(
    var bitmap: Bitmap? = null,
     //MutableState prediction response
    var prediction: MutableState<PredictionResponse?> = mutableStateOf(null),
    var displayPrediction: MutableState<Boolean> = mutableStateOf(false),
    var base64String: String? = null,
)

