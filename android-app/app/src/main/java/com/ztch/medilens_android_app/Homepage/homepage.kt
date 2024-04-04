package com.ztch.medilens_android_app.Homepage

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import com.ztch.medilens_android_app.ApiUtils.MedicationInteractionResponse

import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.appbarBottom
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.Notifications.*

@Composable
fun HomePage(onNavigateToCamera: () -> Unit,
             onNavigateToAlarm: () -> Unit,
             onNavigateToLogin: () -> Unit,
             onNavigateToCabinet: () -> Unit,
             viewModel: AlarmViewModel,
             ) {

    /*
   val context = LocalContext.current

   if(!TokenAuth.isLoggedIn(context)) {
       // if user is not logged in, navigate to login page
       onNavigateToLogin()
   }
   */



    val dataSource = CalendarDataSource()
    // we use `mutableStateOf` and `remember` inside composable function to schedules recomposition
    var calendarUiModel by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }

    //to offload calendar data to a background thread
    val coroutineScope = rememberCoroutineScope()

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
              onNavigateToCabinet = onNavigateToCabinet)
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
                AlarmsList(viewModel = viewModel, data = calendarUiModel)

                // Add more components as needed
            }
        }
    )
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

@Composable
fun AlarmsList(viewModel: AlarmViewModel, data: CalendarUiModel) {
    val selectedDate = data.selectedDate.date

    val alarmsForSelectedDate = remember(selectedDate, viewModel._alarms) {
        viewModel._alarms.filter { alarm ->
            alarm.time.toLocalDate() == selectedDate ||
                    alarm.repetition == Repetition.EVERY_DAY ||
                    (alarm.repetition == Repetition.WEEKLY && alarm.time.dayOfWeek == selectedDate.dayOfWeek)
        }
    }



        LaunchedEffect(alarmsForSelectedDate) {
            alarmsForSelectedDate.forEachIndexed { indexA, alarmA ->
                alarmsForSelectedDate.forEachIndexed { indexB, alarmB ->
                    // Make sure we're not pairing a drug with itself and not pairing the same pair in reverse order
                    if (indexA < indexB) {
                        Log.d("inside","AlarmsList: Calling getMedicationInteractions")
                        viewModel.getMedicationInteractions(alarmA.message, alarmB.message)
                    }
                }
            }
        }


    // Observe the medicationInteractions StateFlow from the ViewModel
    val medicationInteractions by viewModel.medicationInteractionsList.collectAsState()

    // Display medication interactions if available
    medicationInteractions?.let { interactions ->
        interactions.forEach { interaction ->
            interactionDialog(interaction)
        }
    }

    LazyColumn {
        items(alarmsForSelectedDate) { alarm ->
            AlarmCard(alarm, viewModel::removeAlarm)
        }
    }

    if (alarmsForSelectedDate.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.DarkestBlue)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            Text(
                text = "No Medications for today",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
@Composable
fun interactionDialog(interaction: MedicationInteractionResponse) {
    var extendedDescriptionVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Dismiss the dialog */ },
        title = { Text(text = "Drug Interaction Detected") },
        text = {
            Column {
                Text(text = "Severity: ${interaction.severity}")
                Row() {
                    Text(text = "Drugs: ")
                    Text(text = "${interaction.drugA} and ${interaction.drugB}")
                }
                Text(text = "Description: ${interaction.description}")

                // Display the "See Extended Description" text
                ClickableText(
                    text = AnnotatedString("See Extended Description"),
                    onClick = {
                        // Toggle the visibility of the extended description
                        extendedDescriptionVisible = !extendedDescriptionVisible
                    }
                )

                // Display the extended description if it's visible
                if (extendedDescriptionVisible) {
                    Text(text = "Extended Description: ${interaction.extendedDescription}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { /* Dismiss the dialog */ }
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
fun AlarmCard(alarm: AlarmItem, onDeleteClicked: (AlarmItem) -> Unit) {

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
                text = formatLocalDateTimeWithAMPM(alarm.time),
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
                    text = "Medication: ${alarm.message} " +
                            "\nDosage: ${alarm.dosage} " +
                            "\nForm: ${alarm.form} ",
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
                Text(text = "Delete")
            }
        }
    }
}
