package com.ztch.medilens_android_app.Refill

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import com.ztch.medilens_android_app.ApiUtils.Medication
import com.ztch.medilens_android_app.ApiUtils.MedicationInteractionResponse

class UserMedicationViewModel {
    var unencryptedMedications = mutableStateListOf<Medication>()
    var user_id: Int = 0
    var interactions = mutableStateListOf<MedicationInteractionResponse>()
}
