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
import java.time.LocalDateTime



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

    var hourText by remember { mutableStateOf("") }
    var minuteText by remember { mutableStateOf("") }
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
                    // Your OutlinedTextField for hour, minute, and message remains unchanged

                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White
                        ),
                        value = hourText,
                        onValueChange = { hourText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Hour (1-24)", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White
                        ),
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Minute (1-60)", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White
                            // Change Color.Red to your desired focused border color
                        ),
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Enter Message", color = Color.White)
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

                                val scheduledTime = LocalDateTime.now()
                                    .withHour(hourText.toIntOrNull() ?: 0)
                                    .withMinute(minuteText.toIntOrNull() ?: 0)
                                    .withSecond(0) // Ignore seconds

                                alarmItem = AlarmItem(
                                    time = scheduledTime,
                                    message = message,
                                    repetition = selectedRepetition
                                )
                                alarmItem?.let(scheduler::schedule)

                                // Reset the input fields
                                hourText = ""
                                minuteText = ""
                                message = ""
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
                                    repetition = Repetition.ONCE // Assuming you want this as a one-time alarm for testing
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
