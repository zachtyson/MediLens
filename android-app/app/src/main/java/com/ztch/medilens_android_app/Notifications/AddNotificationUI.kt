package com.ztch.medilens_android_app.Notifications

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.R
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillInformationScreen(
    onNavigateToAlarm: () -> Unit,
    onNavigateToAlarmTimes: (String, String, String, String, String) -> Unit,
) {


    var mediName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf("") }
    var RX by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("") }
    val forms: List<String> = listOf("Tablet", "Injectable", "Capsule", "Solution", "Cream", "Drops","Spray")
    var isFormDropdownVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.DarkBlue),
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "Add Reminder Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToAlarm() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "backAlert"
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

                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                            // Change Color.Red to your desired focused border color
                        ),
                        value = mediName,
                        onValueChange = { mediName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Medication Name", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = dose,
                        onValueChange = { dose = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Dosage", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = strength,
                        onValueChange = { strength = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Strength (mg)", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = RX,
                        onValueChange = { RX = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "RX Number", color = Color.White)
                        }
                    )

                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = form,
                        onValueChange = { form = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Form ", color = Color.White)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isFormDropdownVisible = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color.White
                                )
                            }
                        }

                    )
                    DropdownMenu(
                        expanded = isFormDropdownVisible,
                        onDismissRequest = { isFormDropdownVisible = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        forms.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    form = option
                                    isFormDropdownVisible = false
                                }
                            )
                        }
                    }

                    if (showError) {
                        Snackbar(
                            modifier = Modifier.padding(top = 8.dp),
                            action = {
                                Button(
                                    onClick = { showError = false },
                                ) {
                                    Text(text = "Close", color = Color.White)
                                }
                            }
                        ) {
                            Text(text = "All text fields required!", color = Color.White)
                        }
                    }

                    Button(
                        onClick = {
                            if (mediName.isNotEmpty() && dose.isNotEmpty() && strength.isNotEmpty() && RX.isNotEmpty() && form.isNotEmpty()) {
                                onNavigateToAlarmTimes(mediName, dose, strength, RX, form)
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Set Alarm Times")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTimesScreen(
    mediName: String,
    dose: String,
    strength: String,
    RX: String,
    form: String,

    onNavigateBack: () -> Unit,
    alarmViewModel: AlarmViewModel,
    onNavigateToAlarm: () -> Unit
) {
    val context = LocalContext.current
    val scheduler = AlarmScheduler(context)
    var alarmItem: AlarmItem?
    val forms: List<String> = listOf("Once", "Hourly", "Weekly", "Every Day")
    var selectedRepetition by remember { mutableStateOf(Repetition.EVERY_DAY) }
    var isDropdownVisible by remember { mutableStateOf(false) }
    var addAdditionalAlarmsPrompt by remember { mutableStateOf(if (dose.toInt() >= 2) true else false) }
    var addAdditionalAlarms by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    val additionalAlarmTimes: MutableList<MutableState<LocalDateTime>> = remember {
        mutableListOf<MutableState<LocalDateTime>>().apply {
            // Initialize the list with the current time for each alarm
            repeat(dose.toInt()) {
                add(mutableStateOf(LocalDateTime.now()))
            }
        }
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
                        "Add Alarm Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "backAlert"
                        )
                    }
                },
            )
        },
        containerColor = colorResource(R.color.DarkGrey)
    ) { innerPadding ->
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


                if (addAdditionalAlarmsPrompt) {
                    AlertDialog(
                        onDismissRequest = {
                            addAdditionalAlarms = false
                            addAdditionalAlarmsPrompt = false
                        },
                        title = { Text("Add Additional Alarms") },
                        text = { Text("Would you like to add additional alarms per each dosage?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    // Add your logic here for adding additional alarms per dosage
                                    addAdditionalAlarms = true
                                    addAdditionalAlarmsPrompt = false
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    addAdditionalAlarms = false
                                    addAdditionalAlarmsPrompt = false
                                }
                            ) {
                                Text("No")
                            }
                        }
                    )
                }



                if (addAdditionalAlarms) {
                    additionalAlarmTimes.forEachIndexed { index, alarmTimeState ->


                        Text(
                            text = "Alarm ${ index+1}",
                            fontSize = 26.sp,
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = formatLocalDateTimeWithAMPM(alarmTimeState.value),
                                fontSize = 32.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = 16.dp)
                                    .clickable(onClick = {
                                        showTimePickerDialog(context, alarmTimeState.value) { newTime ->
                                            // Update the alarm time state with the new time
                                            alarmTimeState.value = newTime
                                        }
                                    })
                            )
                            Switch(
                                checked = true,
                                onCheckedChange = { isChecked ->
                                },
                                modifier = Modifier.padding(start = 160.dp)
                            )
                        }
                    }
                }else{
                    Text(
                        text = "Alarm 1",
                        fontSize = 26.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = formatLocalDateTimeWithAMPM(additionalAlarmTimes[0].value),
                            fontSize = 32.sp,
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp)
                                .clickable(onClick = {
                                    showTimePickerDialog(context, additionalAlarmTimes[0].value) { newTime ->
                                        // Update the alarm time state with the new time
                                        additionalAlarmTimes[0].value = newTime
                                    }
                                })
                        )
                        Switch(
                            checked = true,
                            onCheckedChange = { isChecked ->
                            },
                            modifier = Modifier.padding(start = 200.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Add Image for Medication",
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)

                        )
                    Switch(
                        checked = true,
                        onCheckedChange = { isChecked ->
                            openGallery()
                        },
                        modifier = Modifier.padding(start = 70.dp)
                    )
                }



                DropdownMenu(
                    expanded = isDropdownVisible,
                    onDismissRequest = { isDropdownVisible = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    forms.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedRepetition = when (option) {
                                    "Every Day" -> Repetition.EVERY_DAY
                                    "Once" -> Repetition.ONCE
                                    "Hourly" -> Repetition.HOURLY
                                    "Weekly" -> Repetition.WEEKLY
                                    else -> selectedRepetition // Fallback to current selected repetition
                                }
                                isDropdownVisible = false
                            }
                        )
                    }
                }


            // Buttons to schedule or select repitition
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom, // Align vertically
            ) {
                Button(
                    onClick = { isDropdownVisible = true }

                ) {
                    Text("Repetition: ${selectedRepetition.name}", color = Color.White)
                }

                Button(
                    onClick = {
                        if (addAdditionalAlarms) {
                            additionalAlarmTimes.forEach {
                                Log.d("Alarm","${it.value}")
                                alarmItem = AlarmItem(
                                    time = it.value,
                                    message = mediName,
                                    dosage = dose,
                                    strength = strength,
                                    RX = RX,
                                    form = form,
                                    repetition = selectedRepetition,
                                    imageUri = selectedImageUri
                                )
                                alarmItem?.let(scheduler::schedule)
                                alarmViewModel.addAlarm(alarmItem!!)
                            }
                        } else {
                            alarmItem = AlarmItem(
                                time = additionalAlarmTimes[0].value,
                                message = mediName,
                                dosage = dose,
                                strength = strength,
                                RX = RX,
                                form = form,
                                repetition = selectedRepetition,
                                imageUri = selectedImageUri
                            )
                            alarmItem?.let(scheduler::schedule)
                            alarmViewModel.addAlarm(alarmItem!!)
                        }
                        onNavigateToAlarm()
                    }
                ) {
                    Text(text = "Set Alarms")
                } }
            }
        }
    }
}

fun showTimePickerDialog(context: android.content.Context, initialTime: LocalDateTime, callback: (LocalDateTime) -> Unit) {
    val calendar = Calendar.getInstance()
    calendar.time = Date.from(initialTime.atZone(ZoneId.systemDefault()).toInstant())
    val initialHour = initialTime.hour
    val initialMinute = initialTime.minute

    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val newDateTime = LocalDateTime.of(initialTime.toLocalDate(), LocalTime.of(hourOfDay, minute))
            callback(newDateTime)
        },
        initialHour,
        initialMinute,
        false // false to use AM/PM format
    ).show()
}

fun formatLocalDateTimeWithAMPM(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return localDateTime.format(formatter)
}
