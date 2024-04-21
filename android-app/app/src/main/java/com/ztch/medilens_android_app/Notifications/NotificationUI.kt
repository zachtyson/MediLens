package com.ztch.medilens_android_app.Notifications

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Authenticate.decryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey
//import com.ztch.medilens_android_app.Homepage.AlarmsList
import com.ztch.medilens_android_app.Homepage.CalendarDataSource
import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.Refill.SharedMedicationModel
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun notificationScreen(onNavigateToHomePage: () -> Unit,
                       onNavigateToPillInformation : ()-> Unit,
                       onNavigateToUnscheduledMedications: () -> Unit,
                       sharedMedicationModel: SharedMedicationModel,
                       alarmViewModel: AlarmViewModel,
                       onNavigateToModifyMedication: () -> Unit

                       ) {

    val context = LocalContext.current
    if(!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    val tok = TokenAuth.getLogInToken(context)
    val dataSource = CalendarDataSource()
    // we use `mutableStateOf` and `remember` inside composable function to schedules recomposition
    //
    val alarms = alarmViewModel.alarms.collectAsState()
    val service = RetrofitClient.apiService

    val userID = remember { mutableIntStateOf(0) }

    // mutable state empty list medication
    val medications = remember { mutableStateOf<List<Medication>>(emptyList()) }

    // Fetch all medications that are in the cabinet
    LaunchedEffect(Unit) {
        fetchMedications(service, context, medications, userID)
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
                AlarmsList(alarms = alarms.value, onDeleteClicked = {
                    alarmViewModel.removeAlarm(it)
                    // callback is Map<String,String>
                    service.removeMedicationSchedule(token = tok, it.dbId, it.dbUserId).enqueue(object : Callback<Map<String,String>> {
                        override fun onResponse(call: Call<Map<String,String>>, response: Response<Map<String,String>>) {
                            if (response.isSuccessful) {
                                Log.d("HomePage", "Successfully removed alarm")
                            } else {
                                Log.e("HomePage", "Failed to remove alarm")
                            }
                        }

                        override fun onFailure(call: Call<Map<String,String>>, t: Throwable) {
                            Log.e("HomePage", "Failed to remove alarm", t)
                        }
                    })
                },
                    sharedMedicationModel = sharedMedicationModel,
                    onNavigateToModifyMedication = onNavigateToModifyMedication,
                    allMedications = medications
                )

            }

        }
    )

}

private fun fetchMedications(
    service: ApiService,
    context: Context,
    allMedications: MutableState<List<Medication>>,
    userId: MutableIntState,
) {

    service.getMedications(TokenAuth.getLogInToken(context)).enqueue(object : Callback<List<Medication>> {
        override fun onResponse(call: Call<List<Medication>>, response: Response<List<Medication>>) {
            if (response.isSuccessful) {
                allMedications.value = response.body() ?: emptyList()
                // iterate over medications and decrypt them
                for (medication in allMedications.value) {
                    userId.intValue = medication.owner_id
                    val localEncryptionKey = getLocalEncryptionKey(context)
                    val decryptedName = decryptData(medication.name, localEncryptionKey, medication.init_vector)
                    val decryptedDescription = decryptData(medication.description ?: "", localEncryptionKey, medication.init_vector)
                    val decryptedColor = decryptData(medication.color ?: "", localEncryptionKey, medication.init_vector)
                    val decryptedImprint = decryptData(medication.imprint ?: "", localEncryptionKey, medication.init_vector)
                    val decryptedShape = decryptData(medication.shape ?: "", localEncryptionKey, medication.init_vector)
                    val decryptedDosage = decryptData(medication.dosage ?: "", localEncryptionKey, medication.init_vector)
                    val decryptedIntakeMethod = decryptData(medication.intake_method ?: "", localEncryptionKey, medication.init_vector)
                    medication.name = decryptedName
                    medication.description = decryptedDescription
                    medication.color = decryptedColor
                    medication.imprint = decryptedImprint
                    medication.shape = decryptedShape
                    medication.dosage = decryptedDosage
                    medication.intake_method = decryptedIntakeMethod
                }
            } else {
                Log.e("Cabinet", "Failed to fetch medications")
            }
        }

        override fun onFailure(call: Call<List<Medication>>, t: Throwable) {
            Log.e("Cabinet", "Failed to fetch medications", t)
        }
    })
}
private fun convertMillisToHumanReadableTime(timeMillis: Long): String {
    val localDateTime = Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return formatDateTime(localDateTime)
}

@Composable
private fun AlarmsList(alarms: List<AlarmItem>, onDeleteClicked: (AlarmItem) -> Unit,
                       sharedMedicationModel: SharedMedicationModel,
                       onNavigateToModifyMedication: () -> Unit,
                       allMedications: MutableState<List<Medication>> ) {
    for (alarm in alarms) {
        AlarmCard(alarm, onDeleteClicked, sharedMedicationModel, onNavigateToModifyMedication, allMedications)
    }
}

@Composable
private fun AlarmCard(alarm: AlarmItem, onDeleteClicked: (AlarmItem) -> Unit,
                      sharedMedicationModel: SharedMedicationModel,
                      onNavigateToModifyMedication: () -> Unit, allMedications: MutableState<List<Medication>>) {
    val time = convertMillisToHumanReadableTime(alarm.startTimeMillis)
    val interval = convertMillisecondsToHumanReadableTime(alarm.intervalMillis)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.DarkestBlue)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Started at $time",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Occurs every $interval",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
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
            // Button to modify the alarm, which should navigate to the modify alarm screen
            Button(
                onClick = {
                    // navigate to modify alarm screen
                    val medication = allMedications.value.find { it.name == alarm.message && it.owner_id == alarm.dbUserId && it.id == alarm.dbId }
                    sharedMedicationModel.medication = medication
                    onNavigateToModifyMedication()
                },
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Text(text = "Modify schedule")
            }
        }
    }
}
