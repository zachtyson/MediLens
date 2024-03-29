package com.ztch.medilens_android_app.Camera

import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {
    var imageAndPrediction: ImageAndPrediction? = null

    var currentPillInfo: PillInfo? = null
}
