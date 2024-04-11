package com.ztch.medilens_android_app.Refill

import androidx.lifecycle.ViewModel
import com.ztch.medilens_android_app.ApiUtils.Medication

class SharedMedicationModel: ViewModel() {
    var medication: Medication? = null
    var userIsScheduling: Boolean = false
}
