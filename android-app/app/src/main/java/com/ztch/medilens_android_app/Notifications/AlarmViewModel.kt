package com.ztch.medilens_android_app.Notifications

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Response

val numIntervals = 20

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private var alarmRepository = AlarmRepository(application)
    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    private val _past_alarms = MutableStateFlow<List<PastAlarmItem>>(emptyList())
    private val _future_alarms = MutableStateFlow<List<FutureAlarmItem>>(emptyList())
    private val _pending_alarms = MutableStateFlow<List<PendingAlarmItem>>(emptyList())
    var alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()
    val past_alarms: StateFlow<List<PastAlarmItem>> = _past_alarms.asStateFlow()
    val future_alarms: StateFlow<List<FutureAlarmItem>> = _future_alarms.asStateFlow()
    val pending_alarms: StateFlow<List<PendingAlarmItem>> = _pending_alarms.asStateFlow()
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            initializeDatabase()
        }
    }
    private suspend fun initializeDatabase() {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            _alarms.value = alarmRepository.alarmDao.getAll()
            _past_alarms.value = alarmRepository.pastAlarmDao.getAll()
            _future_alarms.value = alarmRepository.futureAlarmDao.getAll()
            _pending_alarms.value = alarmRepository.pendingAlarmDao.getAll()
        }
    }

    fun addAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            val updatedAlarms = _alarms.value.toMutableList()
            updatedAlarms.add(alarm)
            _alarms.value = updatedAlarms  // Updating the state with a new list reference
            alarmRepository.alarmDao.insertAll(alarm)
            schedule(alarm, getApplication())
            Log.d("AlarmViewModel", "Alarm added: $alarm")

        }
    }

    fun removeAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            _alarms.value = _alarms.value - alarm
            alarmRepository.alarmDao.delete(alarm)
            cancel(alarm, getApplication())
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            alarmRepository.alarmDao.deleteEverything()
            alarmRepository.futureAlarmDao.deleteEverything()
            _alarms.value = emptyList()
        }
    }


    fun cancel(item: AlarmItem, context: Context) {
        val receiver = AlarmBroadcaster()
        val filter = IntentFilter("android.intent.action.BOOT_COMPLETED")
        context.registerReceiver(receiver, filter)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, AlarmBroadcaster::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        val intent = Intent(context, AlarmBroadcaster::class.java).apply {
            putExtra("EXTRA_MESSAGE", item.message)
            putExtra("EXTRA_REQUEST_CODE", item.hashCode())
        }
        // Cancel the start_time + next n intervals
        for (i in 0..numIntervals) {
            // if time is in the past, skip
            if (item.startTimeMillis + item.intervalMillis * i < System.currentTimeMillis()) {
                continue
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, item.hashCode() + i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // Get the alarm from the futureAlarm table
            val futureAlarm = alarmRepository.futureAlarmDao.getByRequestCode(item.hashCode() + i)
            // Remove the alarm from the futureAlarm table
            if (futureAlarm != null) {
                viewModelScope.launch {
                    alarmRepository.futureAlarmDao.delete(futureAlarm)
                }
            }
            alarmManager.cancel(
                pendingIntent
            )
        }
        // Remove alarm from database
        viewModelScope.launch {
            alarmRepository.alarmDao.delete(item)
        }
    }
    fun schedule(item: AlarmItem, context: Context) {
        val receiver = AlarmBroadcaster()
        val filter = IntentFilter("android.intent.action.BOOT_COMPLETED")
        context.registerReceiver(receiver, filter)
        val message = item.message
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // clear any existing future alarms
        viewModelScope.launch {
            alarmRepository.futureAlarmDao.deleteEverything()
        }

        // schedule the start_time + next 5 intervals
        for (i in 0..numIntervals) {
            Log.d("Num future alarms", alarmRepository.futureAlarmDao.getAll().size.toString())
            // if time is in the past, skip
            if (item.startTimeMillis + item.intervalMillis * i < System.currentTimeMillis()) {
                continue
            }
            val intent = Intent(context, AlarmBroadcaster::class.java).apply {
                putExtra("EXTRA_MESSAGE", message)
                putExtra("EXTRA_REQUEST_CODE", item.hashCode() + i)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, item.hashCode() + i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmPermission(context)
                return
            }
            val zero:Long = 0
            if (item.intervalMillis == zero && i > zero) {
                continue
            }
            val timeInMillis = item.startTimeMillis + item.intervalMillis * i

            // schedule the next n intervals
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            // Add instance to FutureAlarm table
            val futureAlarm = FutureAlarmItem(
                message = item.message,
                timeMillis = timeInMillis,
                imageUri = item.imageUri,
                response = false,
                requestCode = item.hashCode() + i
            )
            viewModelScope.launch {
                alarmRepository.insertFutureAlarm(futureAlarm)
            }
            Log.d("AlarmViewModel", "Alarm scheduled: $item at $timeInMillis")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    }

}





