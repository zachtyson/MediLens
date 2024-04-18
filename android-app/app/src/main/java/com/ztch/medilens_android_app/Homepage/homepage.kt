package com.ztch.medilens_android_app.Homepage

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.ztch.medilens_android_app.ApiUtils.*

import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.appbarBottom
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


import com.ztch.medilens_android_app.Authenticate.decryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey
import com.ztch.medilens_android_app.Notifications.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Composable
fun HomePage(onNavigateToCamera: () -> Unit,
             onNavigateToAlarm: () -> Unit,
             onNavigateToLogin: () -> Unit,
             onNavigateToCabinet: () -> Unit,
             onNavigateToSettings: () -> Unit,
             onNavigateToMediCard: () -> Unit,
             alarmViewModel: AlarmViewModel,
) {


    val context = LocalContext.current

    if(!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to login page
        onNavigateToLogin()
    }
    val service = RetrofitClient.apiService
    val dataSource = CalendarDataSource()
    // we use `mutableStateOf` and `remember` inside composable function to schedules recomposition
    var calendarUiModel by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }
    // display only alarms that are today
    var selectedDate = remember { mutableStateOf(dataSource.today) }

    //to offload calendar data to a background thread
    val coroutineScope = rememberCoroutineScope()

    val tok = TokenAuth.getLogInToken(context)
    var refreshKey = remember { mutableIntStateOf(0) } // State variable to trigger refresh

    LaunchedEffect(refreshKey) {  // Using Unit as a constant key
        fetchUserAlarmsAndScheduleAlarms(context, alarmViewModel)
    }

    Scaffold(
        topBar = {
            homepageHeader(
                data = calendarUiModel,
                selectedDate = selectedDate,
                onDateClickListener = { date ->
                    coroutineScope.launch {
                        calendarUiModel = dataSource.getData(lastSelectedDate = date.date)
                        selectedDate.value = date.date
                    }
                }
            )
        },
        bottomBar = {

            appbarBottom(
                onNavigateToCamera = onNavigateToCamera,
                onNavigateToAlarm = onNavigateToAlarm,
                onNavigateToCabinet = onNavigateToCabinet,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToMedicard = onNavigateToMediCard)
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(color = colorResource(R.color.DarkGrey))
                    .verticalScroll(rememberScrollState())
            ) {

                AlarmsListScreen(alarmViewModel = alarmViewModel, service = service, tok = tok, refreshKey = refreshKey, selectedDate = selectedDate)
                // Log button that prints all alarms to the logcat
                Button(
                    onClick = {
                        Log.d("HomePage", "Alarms: ${alarmViewModel.alarms.value}")
                        Log.d("HomePage", "Past Alarms: ${alarmViewModel.past_alarms.value}")
                        Log.d("HomePage", "Future Alarms: ${alarmViewModel.future_alarms.value}")
                        Log.d("HomePage", "Pending Alarms: ${alarmViewModel.pending_alarms.value}")
                        Log.d("Selected Date", selectedDate.value.toString())
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text(text = "Log Alarms")
                }
            }
        }
    )
}

@Composable
fun AlarmsListScreen(alarmViewModel: AlarmViewModel, service: ApiService = RetrofitClient.apiService, tok: String, refreshKey: MutableState<Int>, selectedDate: MutableState<LocalDate>) {
    val alarms by alarmViewModel.alarms.collectAsState()
    val past_alarms by alarmViewModel.past_alarms.collectAsState()
    val future_alarms by alarmViewModel.future_alarms.collectAsState()
    val pending_alarms by alarmViewModel.pending_alarms.collectAsState()
    Log.d("Past Alarms", past_alarms.toString())
    Log.d("Future Alarms", future_alarms.toString())
    Log.d("Pending Alarms", pending_alarms.toString())
    Log.d("Alarms", alarms.toString())
    AlarmsList(alarms = alarms, onDeleteClicked = {
        alarmViewModel.removeAlarm(it)
        // callback is Map<String,String>
        service.removeMedicationSchedule(token = tok, it.dbId, it.dbUserId).enqueue(object : Callback<Map<String,String>> {
            override fun onResponse(call: Call<Map<String,String>>, response: Response<Map<String,String>>) {
                if (response.isSuccessful) {
                    Log.d("HomePage", "Successfully removed alarm")
                    refreshKey.value += 1
                } else {
                    Log.e("HomePage", "Failed to remove alarm")
                }
            }

            override fun onFailure(call: Call<Map<String,String>>, t: Throwable) {
                Log.e("HomePage", "Failed to remove alarm", t)
            }
        })
    })
    Spacer(modifier = Modifier.height(16.dp))
    PastAlarmsList(pastAlarms = past_alarms, selectedDate = selectedDate)
    Spacer(modifier = Modifier.height(16.dp))
    FutureAlarmsList(futureAlarms = future_alarms, selectedDate = selectedDate)
    Spacer(modifier = Modifier.height(16.dp))
    PendingAlarmList(pendingAlarms = pending_alarms, selectedDate = selectedDate, alarmViewModel = alarmViewModel)

}


// private function to fetch user alarms during app start up
// basically whenever the user opens the application every alarm they have scheduled is deleted and then rescheduled again
// this is so that the alarms are always up-to-date with whatever is within the backend
// using the AlarmSchedulerManager

private fun fetchUserAlarmsAndScheduleAlarms(context: Context, alarmViewModel: AlarmViewModel) {
    // get the list of alarms from the backend
    // assuming the service is obtained from RetrofitClient.apiService
    // and the token is stored in the shared preferences
    val token = TokenAuth.getLogInToken(context)
    val service = RetrofitClient.apiService
    alarmViewModel.deleteAllItems()
    service.getScheduledMedications(token).enqueue(object : Callback<List<Medication>> {
        override fun onResponse(call: Call<List<Medication>>, response: Response<List<Medication>>) {
            if (response.isSuccessful) {
                // delete all alarms
                Log.d("All scheduled alarms", response.body().toString())
                // iterate over the list of medications and schedule alarms
                for (medication in response.body() ?: emptyList()) {
                    // decrypt the medication name
                    medication.name = decryptData(medication.name, getLocalEncryptionKey(context), medication.init_vector)
                    // schedule the alarm
                    val alarm = AlarmItem(
                        message = medication.name,
                        startTimeMillis = medication.schedule_start?.time ?: 0,
                        intervalMillis = medication.interval_milliseconds ?: 0,
                        imageUri = "",
                        dbId = medication.id,
                        dbUserId = medication.owner_id
                    )
                    alarmViewModel.addAlarm(alarm)
                    Log.d("ALarm", "${alarm.message} scheduled")
                }
                Log.d("HomePage", "Successfully fetched user alarms")

            } else {
                Log.e("HomePage", "Failed to fetch user alarms")
            }
        }

        override fun onFailure(call: Call<List<Medication>>, t: Throwable) {
            Log.e("HomePage", "Failed to fetch user alarms", t)
        }
    })
}


// Start of Header creation
@Composable
fun homepageHeader(data: CalendarUiModel,onDateClickListener: (CalendarUiModel.Date) -> Unit, selectedDate: MutableState<LocalDate>) {
    Log.d("header", "Recomposed")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(R.color.DarkBlue)),

        ) {

        Row{
            Text(
                // show "Today" if user selects today's date
                text = if (data.selectedDate.isToday) {
                    "Today"
                }else{
                    data.selectedDate.dayHeader
                },
                fontSize = 32.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f)

            )
        }
        Row {
            Text(
                // else, show the full format of the date
                text = data.selectedDate.date.format(DateTimeFormatter.ofPattern("MMMM"))+"   " +
                        data.selectedDate.date.format(DateTimeFormatter.ofPattern("d")),//data.selectedDate.date.format(

                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f)
                    .align(Alignment.CenterVertically)

            )
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Spacer(modifier = Modifier.height(14.dp))
        RowOfDates(data = data,onDateClickListener = onDateClickListener)
    }
}


@Composable
fun DateCard(data: CalendarUiModel.Date,onDateClickListener: (CalendarUiModel.Date) -> Unit) {
    Log.d("datecard", "Recomposed")
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 4.dp),
        onClick = { onDateClickListener(data) },
        colors = CardDefaults.cardColors(
            // background colors of the selected date
            // and the non-selected date are different
            containerColor = if (data.isSelected) {
                colorResource(R.color.DarkestBlue)
            } else {
                colorResource(R.color.Blue)

            }
        )
    ) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(48.dp)
                .padding(4.dp)
        ) {
            Text(
                text = data.day,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = data.date.dayOfMonth.toString(),// date "15", "16"
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun RowOfDates(data: CalendarUiModel, onDateClickListener: (CalendarUiModel.Date) -> Unit) {
    Log.d("rowofdates", "Recomposed")
    LazyRow {
        // Using the date as a key to optimize recompositions
        // left arrow to shift the visible dates to the left
        item {
            IconButton(onClick = {
                data.onLeftArrowClick()
            }) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Previous")
            }
        }
        items(items = data.visibleDates, key = { it.date }) { date ->
            // Ensuring DateCard is only recomposed if necessary
            DateCard(date, onDateClickListener)
        }
        // right arrow to shift the visible dates to the right
        item {
            IconButton(onClick = {
                data.onRightArrowClick()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Next")
            }
        }

    }
}

@Composable
fun AlarmsList(alarms: List<AlarmItem>, onDeleteClicked: (AlarmItem) -> Unit) {
    for (alarm in alarms) {
        AlarmCard(alarm, onDeleteClicked)
    }
}

@Composable
fun PastAlarmsList(pastAlarms: List<PastAlarmItem>, selectedDate: MutableState<LocalDate>) {
    val zoneId = remember { ZoneId.systemDefault() }  // Use remember for stable ZoneId across recompositions

    // Recalculate only if selectedDate or zoneId changes
    val startOfDay = remember(selectedDate.value, zoneId) {
        selectedDate.value.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
    val endOfDay = remember(selectedDate.value, zoneId) {
        selectedDate.value.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    Column {
        pastAlarms.filter { it.timeMillis in startOfDay until endOfDay }.forEach { alarm ->
            PastAlarmCard(alarm)
        }
    }
}

@Composable
fun PastAlarmCard(alarm: PastAlarmItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.Purple)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(215.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Time: ${alarm.timeMillis}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Row  (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .padding(8.dp)
            ){
                alarm.imageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .width(75.dp) // Set the width of the image
                            .height(75.dp) // Set the height of the image
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Text(
                    text = "Past Medication: ${alarm.message}",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FutureAlarmsList(futureAlarms: List<FutureAlarmItem>, selectedDate: MutableState<LocalDate>) {
    val zoneId = remember { ZoneId.systemDefault() }  // Use remember for stable ZoneId across recompositions

    // Recalculate only if selectedDate or zoneId changes
    val startOfDay = remember(selectedDate.value, zoneId) {
        selectedDate.value.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
    val endOfDay = remember(selectedDate.value, zoneId) {
        selectedDate.value.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    Column {
        futureAlarms.filter { it.timeMillis in startOfDay until endOfDay }.forEach { alarm ->
            FutureAlarmCard(alarm)
        }
    }
}



@Composable
fun FutureAlarmCard(alarm: FutureAlarmItem) {
    Card(
        colors = CardDefaults.cardColors(
            // Red
            containerColor = colorResource(R.color.Grey)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(215.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Time: ${alarm.timeMillis}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Row  (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .padding(8.dp)
            ){
                alarm.imageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .width(75.dp) // Set the width of the image
                            .height(75.dp) // Set the height of the image
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Text(
                    text = "Future Medication: ${alarm.message}",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PendingAlarmList(pendingAlarms: List<PendingAlarmItem>, selectedDate: MutableState<LocalDate>, alarmViewModel: AlarmViewModel) {
    val zoneId = remember { ZoneId.systemDefault() }  // Use remember for stable ZoneId across recompositions

    // Recalculate only if selectedDate or zoneId changes
    val startOfDay = remember(selectedDate.value, zoneId) {
        selectedDate.value.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
    val endOfDay = remember(selectedDate.value, zoneId) {
        selectedDate.value.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
    Column {
        pendingAlarms.filter { it.timeMillis in startOfDay until endOfDay }.forEach { alarm ->
            PendingAlarmCard(alarm, alarmViewModel)
        }
    }
}


fun convertMillisToLocalDate(timeMillis: Long): LocalDate {
    val zoneId = TimeZone.getDefault().toZoneId() // Get the default timezone from the device
    val instant = Instant.ofEpochMilli(timeMillis)
    val zonedDateTime = instant.atZone(zoneId)
    return zonedDateTime.toLocalDate()
}
@Composable
fun PendingAlarmCard(alarm: PendingAlarmItem, alarmViewModel: AlarmViewModel) {
    val localDate = convertMillisToLocalDate(alarm.timeMillis)
    val date = localDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.DarkBlue)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Date: $date",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .padding(8.dp)
            ) {
                alarm.imageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .width(75.dp) // Set the width of the image
                            .height(75.dp) // Set the height of the image
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Pending Medication: ${alarm.message}",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            // Buttons row to accept or reject the medication
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), // Add padding around the buttons
                horizontalArrangement = Arrangement.Center // Center the buttons horizontally
            ) {
                Button(
                    onClick = {
                        // convert the pending alarm to a past alarm
                        // and delete the pending alarm
                        // this will log that the user took the medication at the scheduled time
                        // and remove the pending alarm from the list
                        alarmViewModel.convertPendingAlarmToPastAlarm(alarm, true)
                    }
                ) {
                    Text(text = "Accept")
                }
                Spacer(modifier = Modifier.width(16.dp)) // Space between the buttons
                Button(
                    onClick = {
                        // convert the pending alarm to a past alarm
                        // and delete the pending alarm
                        // this will log that the user did not take the medication at the scheduled time
                        // and remove the pending alarm from the list
                        alarmViewModel.convertPendingAlarmToPastAlarm(alarm, false)
                    }
                ) {
                    Text(text = "Reject")
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: AlarmItem, onDeleteClicked: (AlarmItem) -> Unit) {
    val time = alarm.startTimeMillis
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.DarkestBlue)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(215.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Time: ${time}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Row  (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .padding(8.dp)
            ){
                alarm.imageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .width(75.dp) // Set the width of the image
                            .height(75.dp) // Set the height of the image
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Text(
                    text = "Medication: ${alarm.message}",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onDeleteClicked(alarm) },
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Text(text = "Remove schedule")
            }
        }
    }
}
