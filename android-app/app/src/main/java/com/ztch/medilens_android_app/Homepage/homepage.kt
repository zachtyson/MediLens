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

    //to offload calendar data to a background thread
    val coroutineScope = rememberCoroutineScope()
    val alarms by alarmViewModel.alarms.collectAsState()
    val tok = TokenAuth.getLogInToken(context)
    var refreshKey by remember { mutableStateOf(0) } // State variable to trigger refresh

    LaunchedEffect(refreshKey) {  // Using Unit as a constant key
        fetchUserAlarmsAndScheduleAlarms(context, alarmViewModel, alarms.toMutableList())
    }

    Scaffold(
        topBar = {
            homepageHeader(
                data = calendarUiModel,
                onDateClickListener = { date ->
                    coroutineScope.launch {
                        calendarUiModel = dataSource.getData(lastSelectedDate = date.date)
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
                AlarmsList(alarms = alarms, onDeleteClicked = {
                    alarmViewModel.removeAlarm(it)
                    // callback is Map<String,String>
                    service.removeMedicationSchedule(token = tok, it.dbId, it.dbUserId).enqueue(object : Callback<Map<String,String>> {
                        override fun onResponse(call: Call<Map<String,String>>, response: Response<Map<String,String>>) {
                            if (response.isSuccessful) {
                                Log.d("HomePage", "Successfully removed alarm")
                                refreshKey += 1
                            } else {
                                Log.e("HomePage", "Failed to remove alarm")
                            }
                        }

                        override fun onFailure(call: Call<Map<String,String>>, t: Throwable) {
                            Log.e("HomePage", "Failed to remove alarm", t)
                        }
                    })
                })
            }
        }
    )
}


// private function to fetch user alarms during app start up
// basically whenever the user opens the application every alarm they have scheduled is deleted and then rescheduled again
// this is so that the alarms are always up-to-date with whatever is within the backend
// using the AlarmSchedulerManager
private fun fetchUserAlarmsAndScheduleAlarms(context: Context, alarmViewModel: AlarmViewModel, alarms : MutableList<AlarmItem>) {
    // get the list of alarms from the backend
    // assuming the service is obtained from RetrofitClient.apiService
    // and the token is stored in the shared preferences
    val token = TokenAuth.getLogInToken(context)
    val service = RetrofitClient.apiService
    service.getScheduledMedications(token).enqueue(object : Callback<List<Medication>> {
        override fun onResponse(call: Call<List<Medication>>, response: Response<List<Medication>>) {
            if (response.isSuccessful) {
                // delete all alarms
                alarmViewModel.deleteAllItems()
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
                    alarms.add(alarm)
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
fun homepageHeader(data: CalendarUiModel,onDateClickListener: (CalendarUiModel.Date) -> Unit) {
    Log.d("header", "Recomposed")
    Column(
        modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.padding(start = 14.dp)
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
                modifier = Modifier.padding(start = 14.dp)
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
