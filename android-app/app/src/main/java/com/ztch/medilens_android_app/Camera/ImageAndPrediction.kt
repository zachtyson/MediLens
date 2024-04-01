package com.ztch.medilens_android_app.Camera

import android.graphics.Bitmap
import com.ztch.medilens_android_app.ApiUtils.PredictionResponse

data class ImageAndPrediction(var bitmap: Bitmap? = null, var prediction: PredictionResponse? = null, var displayPrediction: Boolean = false, var base64String: String? = null)

