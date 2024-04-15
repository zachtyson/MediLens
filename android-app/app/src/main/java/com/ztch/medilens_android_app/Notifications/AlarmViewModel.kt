package com.ztch.medilens_android_app.Notifications

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.Call
import com.ztch.medilens_android_app.ApiUtils.MedicationInteractionResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Response

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AlarmDatabase.getInstance(application)
    private var alarmDao = db.alarmDao()
    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initializeDatabase()
        }
    }
    private suspend fun initializeDatabase() {
        val dbInstance = AlarmDatabase.getInstance(getApplication())
        alarmDao = dbInstance.alarmDao()
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            _alarms.value = alarmDao.getAll()
        }
    }

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


    fun addAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            _alarms.value = _alarms.value + alarm
            alarmDao.insertAll(alarm)
            schedule(alarm, getApplication())
            Log.d("AlarmViewModel", "Alarm added: $alarm")

        }
    }

    fun removeAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            _alarms.value = _alarms.value - alarm
            alarmDao.delete(alarm)
            cancel(alarm, getApplication())
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            alarmDao.deleteAll()
            _alarms.value = emptyList()
        }
    }


    fun cancel(item: AlarmItem, context: Context) {
        val receiver = ComponentName(context, AlarmBroadcaster::class.java)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        val alarmId = item.id
        val intent = Intent(context, AlarmBroadcaster::class.java).apply {
            putExtra("EXTRA_MESSAGE", item.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(
            pendingIntent
        )
        // Remove alarm from database
        viewModelScope.launch {
            alarmDao.delete(item)
        }
    }
    fun schedule(item: AlarmItem, context: Context) {
        val message = item.message
        val alarmId = item.id
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcaster::class.java).apply {
            putExtra("EXTRA_MESSAGE", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            requestExactAlarmPermission(context)
            return
        }

        val timeInMillis = item.startTimeMillis

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )


    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    }

}





