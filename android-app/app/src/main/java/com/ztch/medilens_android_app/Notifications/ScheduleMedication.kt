package com.ztch.medilens_android_app.Notifications


import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Authenticate.createRandomIV
import com.ztch.medilens_android_app.Authenticate.encryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey
import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.Refill.SharedMedicationModel
import androidx.compose.material.*
import java.util.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMedication (
    onNavigateToHomePage: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToCabinet: () -> Unit,
    sharedMedicationModel: SharedMedicationModel
) {
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()
    SnackbarHost(hostState = snackState, Modifier)

    val service = RetrofitClient.apiService
    Log.d("ModifyMedication", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    if (sharedMedicationModel.medication == null) {
        onNavigateToCabinet()
    }

    val medication = sharedMedicationModel.medication!!
    var medicationScheduleStartDate by remember { mutableStateOf(medication.schedule_start) }
    var medicationScheduleInterval by remember { mutableStateOf(medication.interval_milliseconds) }
    var medicationScheduleIntervalTimePicker by remember { mutableStateOf(IntervalSchedule(0, 0, 0, 0)) }
    var errorText = remember { mutableStateOf("") }

    val openDatePicker = remember { mutableStateOf(false) }
    val openTimePicker = remember { mutableStateOf(false) }
    val openIntervalPicker = remember { mutableStateOf(false) }
    if (openDatePicker.value) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                openDatePicker.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDatePicker.value = false
                        snackScope.launch {
                            snackState.showSnackbar(
                                "Selected date timestamp: ${datePickerState.selectedDateMillis}"
                            )
                            val newDate = Date(datePickerState.selectedDateMillis!!)
                            medicationScheduleStartDate = newDate
                            Log.d("DatePicker", "Selected date: $newDate")
                        }
                        // open time picker
                        openTimePicker.value = true

                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDatePicker.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (openTimePicker.value) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                // Change the time of the medication schedule start date but not the date
                val newDate = medicationScheduleStartDate?.let {
                    val newCalendar = Calendar.getInstance().apply {
                        time = it
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }
                    newCalendar.time
                }
                medicationScheduleStartDate = newDate
                openTimePicker.value = false
            },
            12,
            0,
            false
        ).show()
    }
    if (openIntervalPicker.value) {
        val intervalPickerState = remember {
            mutableStateOf(IntervalSchedule(0, 0, 0, 0))
        }
        val confirmEnabled = remember {
            derivedStateOf {
                intervalPickerState.value.weeks > 0 ||
                        intervalPickerState.value.days > 0 ||
                        intervalPickerState.value.hours > 0 ||
                        intervalPickerState.value.minutes > 0
            }
        }
        AlertDialog(
            onDismissRequest = {
                openIntervalPicker.value = false
            },
            title = {
                Log.d("openDatePicker", openDatePicker.value.toString())
                Log.d("openTimePicker", openTimePicker.value.toString())
                Log.d("openIntervalPicker", openIntervalPicker.value.toString())
                Text("Select Interval")
            },
            text = {
                Column {
                    IntervalPicker(
                        state = intervalPickerState,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openIntervalPicker.value = false
                        medicationScheduleIntervalTimePicker = intervalPickerState.value
                        medicationScheduleInterval = intervalPickerState.value.toMilliseconds()
                        Log.d("IntervalPicker", "Selected interval: $medicationScheduleInterval")
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openIntervalPicker.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.DarkBlue),
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "Schedule Medication",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToCabinet() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Use the padding provided by Scaffold for the content
                    .background(color = colorResource(R.color.DarkGrey))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Card with medication information
                    Card( colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.DarkBlue),
                        contentColor = colorResource(R.color.DarkBlue),

                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            // take up as much height as needed
                            .height(IntrinsicSize.Min)
                            .padding(16.dp)
                    ) {
                        InfoField(
                            label = "Name",
                            value = medication.name
                        )
                        InfoField(
                            label = "Description",
                            value = medication.description
                        )
                        InfoField(
                            label = "Color",
                            value = medication.color
                        )
                        InfoField(
                            label = "Imprint",
                            value = medication.imprint
                        )
                        InfoField(
                            label = "Shape",
                            value = medication.shape
                        )
                        InfoField(
                            label = "Dosage",
                            value = medication.dosage
                        )
                        InfoField(
                            label = "Intake Method",
                            value = medication.intake_method
                        )
                    }
                    var scheduleDateString = "Not selected"
                    if (medicationScheduleStartDate != null) {
                        scheduleDateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(medicationScheduleStartDate!!)
                    }
                    OutlinedTextField(
                        value = scheduleDateString,
                        onValueChange = {},
                        label = { Text("Start Date And Time") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    //medicationScheduleIntervalTimePicker.let {
                    //                            "${it.weeks} weeks, ${it.days} days, ${it.hours} hours, ${it.minutes} minutes"
                    var intervalString = "Not selected"
                    if (medicationScheduleIntervalTimePicker.notDefault()) {
                        intervalString = "${medicationScheduleIntervalTimePicker.weeks} weeks, ${medicationScheduleIntervalTimePicker.days} days, ${medicationScheduleIntervalTimePicker.hours} hours, ${medicationScheduleIntervalTimePicker.minutes} minutes"
                    }
                    // white color interval
                    OutlinedTextField(
                        value = intervalString,
                        onValueChange = {},
                        label = { Text("Interval") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // let user pick a start date using a date picker
                    Button(
                        onClick = {
                            openDatePicker.value = true
                        },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                    ) {
                        Text(text = "Select Start Date And Time", color = Color.White)
                    }
                    if (medicationScheduleStartDate != null) {
                        Button(
                            onClick = {
                                openIntervalPicker.value = true
                            },
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                        ) {
                            Text(text = "Select Interval", color = Color.White)
                        }

                    }
                    Button(
                        onClick = {
                            // if schedule start date is not set, do not allow user to proceed
                            if (medicationScheduleStartDate == null) {
                                errorText.value = "Please select a start date and interval"
                            } else {
                                // encrypt medication

                                medication.schedule_start = medicationScheduleStartDate
                                medication.interval_milliseconds = medicationScheduleInterval
                                //todo fix error 422

                                val encryptionKey = getLocalEncryptionKey(context)
                                val iv = createRandomIV()

                                val token = TokenAuth.getLogInToken(context)

                                val mc = MedicationModify(
                                    name = encryptData(medication.name, encryptionKey, iv),
                                    description = medication.description?.let { encryptData(it, encryptionKey, iv) },
                                    color = medication.color?.let { encryptData(it, encryptionKey, iv) },
                                    imprint = medication.imprint?.let { encryptData(it, encryptionKey, iv) },
                                    shape = medication.shape?.let { encryptData(it, encryptionKey, iv) },
                                    dosage = medication.dosage?.let { encryptData(it, encryptionKey, iv) },
                                    intake_method = medication.intake_method?.let { encryptData(it, encryptionKey, iv) },
                                    init_vector = iv,
                                    id = medication.id,
                                    owner_id = medication.owner_id,
                                    schedule_start = medication.schedule_start,
                                    interval_milliseconds = medication.interval_milliseconds
                                )

                                val call = RetrofitClient.apiService.modifyMedication(token, mc)
                                call.enqueue(object : retrofit2.Callback<Map<String, String>> {
                                    override fun onResponse(call: retrofit2.Call<Map<String, String>>, response: retrofit2.Response<Map<String, String>>) {
                                        if (response.isSuccessful) {
                                            Log.d("ModifyMedication", "Medication modified successfully")
                                            if (errorText.value == "") {
                                                onNavigateToCabinet()
                                            }
                                        } else {
                                            Log.d("ModifyMedication", "Failed to modify medication")
                                            errorText.value = "Failed to modify medication"
                                        }
                                    }

                                    override fun onFailure(call: retrofit2.Call<Map<String, String>>, t: Throwable) {
                                        Log.d("ModifyMedication", "Failed to modify medication, error: ${t.message}")
                                        errorText.value = "Failed to modify medication"
                                    }
                                })

                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Submit Schedule", color = Color.White)
                    }
                }
            }
        }

    )

}

data class IntervalSchedule(
    val weeks: Int,
    val days: Int,
    val hours: Int,
    val minutes: Int
) {
    fun toMilliseconds(): Long {
        return (weeks * 604800000L) + (days * 86400000L) + (hours * 3600000L) + (minutes * 60000L)
    }

    fun notDefault(): Boolean {
        return weeks > 0 || days > 0 || hours > 0 || minutes > 0
    }
}
@Composable
fun InfoField(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: ${value ?: "N/A"}",
            color = Color.White
        )
    }
}

// Lets user pick an interval in weeks, days, hours, and minutes
// Simple text boxes that only allow numbers
// or a checkbox for one-time medication
@Composable
fun IntervalPicker (
    state: MutableState<IntervalSchedule>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TextField(
            value = state.value.weeks.toString(),
            onValueChange = {
                state.value = state.value.copy(weeks = it.toIntOrNull() ?: 0)
            },
            label = { Text("Weeks") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.value.days.toString(),
            onValueChange = {
                state.value = state.value.copy(days = it.toIntOrNull() ?: 0)
            },
            label = { Text("Days") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.value.hours.toString(),
            onValueChange = {
                state.value = state.value.copy(hours = it.toIntOrNull() ?: 0)
            },
            label = { Text("Hours") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.value.minutes.toString(),
            onValueChange = {
                state.value = state.value.copy(minutes = it.toIntOrNull() ?: 0)
            },
            label = { Text("Minutes") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
