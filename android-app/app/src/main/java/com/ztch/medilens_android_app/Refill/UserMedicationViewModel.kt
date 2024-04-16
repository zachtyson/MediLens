package com.ztch.medilens_android_app.Refill

import com.ztch.medilens_android_app.ApiUtils.Medication

class UserMedicationViewModel {
    var unencryptedMedications = mutableListOf<Medication>()
    var user_id: Int = 0
}
