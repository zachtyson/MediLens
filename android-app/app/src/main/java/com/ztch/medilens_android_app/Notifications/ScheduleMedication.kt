package com.ztch.medilens_android_app.Notifications


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.*
import androidx.compose.ui.platform.LocalContext
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Authenticate.createRandomIV
import com.ztch.medilens_android_app.Authenticate.encryptData
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMedication (
    onNavigateToHomePage: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToCabinet: () -> Unit,
    sharedMedicationModel: SharedMedicationModel
) {
    // Add Medication

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
    var showDatePicker by remember { mutableStateOf(false) }
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
                    // Unmodifiable text fields for the rest of the medication details
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = "Name: ${medication.name}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = "Description: ${medication.description ?: "N/A"}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = "Color: ${medication.color ?: "N/A"}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = "Imprint: ${medication.imprint ?: "N/A"}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = "Shape: ${medication.shape ?: "N/A"}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = "Dosage: ${medication.dosage ?: "N/A"}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = "Intake Method: ${medication.intake_method ?: "N/A"}",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // let user pick a start date using a date picker
                    //todo implement datepicker/timepicker
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                    ) {
                        Text(text = "Select Start Date")
                    }

                    medicationScheduleStartDate?.let {
                        Text("Start Date: ${formatDateTime(convertToLocalDateTime(it))}")
                    }
                    Button(
                        onClick = {
                            // if schedule start date is not set, do not allow user to proceed
                            if (medicationScheduleStartDate == null) {
                                errorText.value = "Please select a start date and interval"
                            } else {
                                // encrypt medication
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
                        Text(text = "Modify Medication", color = Color.White)
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
}
