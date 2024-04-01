package com.ztch.medilens_android_app.Notifications

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import retrofit2.Call
import com.ztch.medilens_android_app.ApiUtils.MedicationInteractionResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Callback
import retrofit2.Response

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val service= RetrofitClient.apiService

    // StateFlow to store list of medication interactions
    private val _medicationInteractionsList = MutableStateFlow<List<MedicationInteractionResponse>>(emptyList())
    val medicationInteractionsList: StateFlow<List<MedicationInteractionResponse>> = _medicationInteractionsList

    fun getMedicationInteractions(drugA: String, drugB: String) {
        Log.d("getMedicationInteractions", "getMedicationInteractions: $drugA, $drugB")

        // Assuming service is obtained from RetrofitClient.apiService
        service.getMedicationInteractions(drugA, drugB).enqueue(object : Callback<MedicationInteractionResponse> {
            override fun onResponse(call: retrofit2.Call<MedicationInteractionResponse>, response: retrofit2.Response<MedicationInteractionResponse>) {
                if (response.isSuccessful) {
                    Log.d("Success", "onResponse: ${response.body()}")
                    val interaction = response.body()
                    interaction?.let {
                        val updatedList = _medicationInteractionsList.value.toMutableList()
                        updatedList.add(it)
                        _medicationInteractionsList.value = updatedList
                    }
                } else {
                    Log.d("Error", "onResponse: ${response.body()}")
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<MedicationInteractionResponse>, t: Throwable) {
                // Handle failure
                Log.d("Error", t.message ?: "An error occurred")
            }
        })
    }

    val _alarms = mutableStateListOf<AlarmItem>()
    private val scheduler: AlarmScheduler = AlarmScheduler(application.applicationContext)

    fun addAlarm(alarm: AlarmItem) {
        _alarms.add(alarm)
    }

    fun removeAlarm(alarm: AlarmItem) {
        _alarms.remove(alarm)
        alarm.let(scheduler::cancel)
    }
}




