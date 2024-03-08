package com.ztch.medilens_android_app.Homepage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.appbarBottom
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


@Composable
fun HomePage(onNavigateToCamera: () -> Unit, onNavigateToAlarm: () -> Unit) {

    val dataSource = CalendarDataSource()
    // we use `mutableStateOf` and `remember` inside composable function to schedules recomposition
    var calendarUiModel by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }

    //to offload calendar data to a background thread
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch {
            val newData = dataSource.getData(lastSelectedDate = dataSource.today)
            calendarUiModel = newData
        }
    }

    Scaffold(
        topBar = {
            // Assuming homepageHeader is your topBar content
            homepageHeader(data = calendarUiModel, onDateClickListener = { date ->
                coroutineScope.launch {
                    calendarUiModel = dataSource.getData(lastSelectedDate = date.date)
                }
            })
        },
        bottomBar = {

          appbarBottom(onNavigateToCamera = onNavigateToCamera, onNavigateToAlarm = onNavigateToAlarm)
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Use the padding provided by Scaffold for the content
                    .background(color = colorResource(R.color.DarkGrey))
            ) {
                // Your content goes here. For example, if you want to display a list of items:
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.DarkestBlue)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Medication",
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You have no medication today",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        // If NotificationDemo is a composable you want to display, include it here
                        // NotificationDemo()
                    }
                }
                // Add more components as needed
            }
        }
    )
}


@Preview(showSystemUi = true, device = "id:pixel_7_pro")
@Composable
fun homepagePreview() {
    HomePage(onNavigateToCamera = {}, onNavigateToAlarm = {})
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
        items(items = data.visibleDates, key = { it.date }) { date ->
            // Ensuring DateCard is only recomposed if necessary
            DateCard(date, onDateClickListener)
        }
    }
}

