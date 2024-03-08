package com.ztch.medilens_android_app.Notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.R
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Preview(showSystemUi = true, device = "id:pixel_7_pro")
@Composable
fun notifcationADDPreview() {
    AddReminderScreen(onNavigateToAlert = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(onNavigateToAlert: () -> Unit) {
    val context = LocalContext.current
    val scheduler = AlarmScheduler(context)
    var alarmItem: AlarmItem? = null

    var dayText by remember { mutableStateOf("") }
    var hourText by remember { mutableStateOf("") }
    var minuteText by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("") }
    var monthText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var isDropdownVisible by remember { mutableStateOf(false) }

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
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                        ),
                        value = dayText,
                        onValueChange = { dayText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Day", color = Color.White  )
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                        ),
                        value = hourText,
                        onValueChange = { hourText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Hour", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                             // Change Color.Red to your desired focused border color
                        ),
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Minute", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                        ),
                        value = monthText,
                        onValueChange = { monthText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Month", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                        ),
                        value = yearText,
                        onValueChange = { yearText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Year", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                        ),
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Message", color = Color.White)
                        }
                    )

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
                            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            try {
                                val dateTime = LocalDateTime.of(
                                    yearText.toIntOrNull() ?: 0,
                                    monthText.toIntOrNull() ?: 1,
                                    dayText.toIntOrNull() ?: 1,
                                    hourText.toIntOrNull() ?: 0,
                                    minuteText.toIntOrNull() ?: 0
                                )
                                alarmItem = AlarmItem(
                                    time = dateTime,
                                    message = message,
                                    repetition = selectedRepetition
                                )
                                alarmItem?.let(scheduler::schedule)
                                dayText = ""
                                monthText = ""
                                yearText = ""
                                hourText = ""
                                minuteText = ""
                                message = ""
                            } catch (e: DateTimeException) {
                                // Handle parsing error, show a message to the user, etc.
                            }
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
                                val currentDateTime = LocalDateTime.now()

                                // Set the test alarm for the next minute
                                val testAlarmTime = currentDateTime.plusMinutes(1)

                                alarmItem = AlarmItem(
                                    time = testAlarmTime,
                                    message = "Test Alarm",
                                    repetition = Repetition.NONE // Assuming you want this as a one-time alarm for testing
                                )

                                // Schedule the test alarm
                                alarmItem?.let(scheduler::schedule)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text("Schedule Test Alarm Now")
                        }
                    }
                }

            }

        }
    )

}

