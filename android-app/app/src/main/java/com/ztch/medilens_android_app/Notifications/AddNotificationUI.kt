package com.ztch.medilens_android_app.Notifications


import android.Manifest

import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.core.content.ContextCompat
import com.ztch.medilens_android_app.R
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(onNavigateToAlert: () -> Unit,viewModel: AlarmViewModel) {
    val context = LocalContext.current
    val scheduler = AlarmScheduler(context)
    var alarmItem: AlarmItem? = null

    var selectedTime by remember { mutableStateOf(LocalDateTime.now()) }
    var mediName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf("") }
    var RX by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("") }
    val forms: List<String> = listOf("Tablet", "Injectable", "Capsule", "Solution", "Cream", "Drops","Spray")
    var isDropdownVisible by remember { mutableStateOf(false) }
    var isFormDropdownVisible by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else mutableStateOf(true)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    // Add state to track the selected repetition option
    var selectedRepetition by remember { mutableStateOf(Repetition.EVERY_DAY) }

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
                    IconButton(onClick = { onNavigateToAlert() }) {
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

                    Text(
                        text = "Selected Alarm Time : ${formatLocalDateTimeWithAMPM(selectedTime)}",
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Button to open the time picker dialog
                    Button(
                        onClick = {
                            showTimePickerDialog(context, selectedTime) {
                                selectedTime = it
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Ajust Alarm Time")
                    }

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
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                            }}

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


                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {

                        OutlinedButton(
                            onClick = { isDropdownVisible = true }
                        ) {
                            Text("Select Repetition: ${selectedRepetition.name}", color = Color.White)
                        }

                        DropdownMenu(
                            expanded = isDropdownVisible,
                            onDismissRequest = { isDropdownVisible = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Every Day")},
                                onClick = {
                                    selectedRepetition = Repetition.EVERY_DAY
                                    isDropdownVisible = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Once" ) },
                                onClick = {
                                    selectedRepetition = Repetition.ONCE
                                    isDropdownVisible = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hourly" ) },
                                onClick = {
                                    selectedRepetition = Repetition.HOURLY
                                    isDropdownVisible = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Weekly" ) },
                                onClick = {
                                    selectedRepetition = Repetition.WEEKLY
                                    isDropdownVisible = false
                                }
                            )
                        }
                    }

                    // Buttons to schedule or cancel the alarm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {


                                alarmItem = AlarmItem(
                                    time = selectedTime,
                                    message = mediName,
                                    dosage = dose,
                                    strength = strength,
                                     RX = RX,
                                    form = form,
                                    repetition = selectedRepetition
                                )
                                alarmItem?.let(scheduler::schedule)

                                viewModel.addAlarm(alarmItem!!)
                                // Reset the input fields
                                mediName = ""
                                dose = ""
                                strength = ""
                                RX = ""
                                form = ""

                        }) {
                            Text(text = "Schedule")
                        }
                        Button(onClick = {
                            alarmItem?.let(scheduler::cancel)
                        }) {
                            Text(text = "Cancel")
                        }

                        Button(
                            onClick = {
                                // Set the test alarm for the next minute
                                val testAlarmTime = LocalDateTime.now().plusSeconds(10)

                                alarmItem = AlarmItem(
                                    time = testAlarmTime,
                                    message = "Test Alarm",
                                    dosage = "Test Dosage",
                                    strength = "Test Strength",
                                    RX = "Test RX",
                                    form = "Test Form",
                                    repetition = Repetition.ONCE // Assuming you want this as a one-time alarm for testing
                                )

                                // Schedule the test alarm
                                alarmItem?.let(scheduler::schedule)
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Schedule Test Alarm Now")
                        }
                    }
                }
            }

        }
    )
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
        false // Set to false to use AM/PM format
    ).show()
}

fun formatLocalDateTimeWithAMPM(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return localDateTime.format(formatter)
}