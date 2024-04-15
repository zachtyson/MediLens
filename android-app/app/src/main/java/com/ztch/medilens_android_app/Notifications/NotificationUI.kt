package com.ztch.medilens_android_app.Notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
//import com.ztch.medilens_android_app.Homepage.AlarmsList
import com.ztch.medilens_android_app.Homepage.CalendarDataSource
import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.Refill.SharedMedicationModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun notificationScreen(onNavigateToHomePage: () -> Unit,
                       onNavigateToPillInformation : ()-> Unit,
                       onNavigateToUnscheduledMedications: () -> Unit,
                       sharedMedicationModel: SharedMedicationModel
) {
    /*
    val context = LocalContext.current
    if(!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    */


    val dataSource = CalendarDataSource()
    // we use `mutableStateOf` and `remember` inside composable function to schedules recomposition
    var calendarUiModel by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.DarkBlue),
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "Reminders",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToHomePage() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onNavigateToUnscheduledMedications()
                        sharedMedicationModel.userIsScheduling = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            tint = Color.White,
                            contentDescription = "addReminder"
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
                // Your content goes here. For example, if you want to display a list of items:
//                AlarmsList(viewModel = viewModel, data = calendarUiModel)

            }

        }
    )

}
