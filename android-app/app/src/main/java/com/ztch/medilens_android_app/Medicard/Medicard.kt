package com.ztch.medilens_android_app.Medicard

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Authenticate.decryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey
import com.ztch.medilens_android_app.Notifications.AlarmViewModel
import com.ztch.medilens_android_app.Notifications.FutureAlarmItem
import com.ztch.medilens_android_app.Notifications.PastAlarmItem
import com.ztch.medilens_android_app.Notifications.PendingAlarmItem
import com.ztch.medilens_android_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date


@Preview(showSystemUi = false, showBackground = false)
@Composable
fun mediPreview1() {
    MediCardScreen( onNavigateToHomePage = {}, alarmViewModel = null)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediCardScreen(onNavigateToHomePage: () -> Unit,
                   alarmViewModel: AlarmViewModel?
) {
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    // call alarmviewmodel function to get past_alarms table and parse it into a readable report
    val userInfoResponse = remember { mutableStateOf<UserInfoResponse?>(null) }
    val userDoctors = remember { mutableStateOf<List<Doctor>>(emptyList()) }
    val allMedications = remember { mutableStateOf<List<Medication>>(emptyList()) }
    val emptyPastAlarms = remember { listOf<PastAlarmItem>() }
    val emptyFutureAlarms = remember { listOf<FutureAlarmItem>() }
    val emptyPendingAlarms = remember { listOf<PendingAlarmItem>() }
    //MutableIntState
    val userID = remember { mutableIntStateOf(0) }
    val pastAlarms by alarmViewModel?.past_alarms?.collectAsState(initial = emptyPastAlarms) ?: remember { mutableStateOf(emptyPastAlarms) }
    val futureAlarms by alarmViewModel?.future_alarms?.collectAsState(initial = emptyFutureAlarms) ?: remember { mutableStateOf(emptyFutureAlarms) }
    val pendingAlarms by alarmViewModel?.pending_alarms?.collectAsState(initial = emptyPendingAlarms) ?: remember { mutableStateOf(emptyPendingAlarms) }

    val token = TokenAuth.getLogInToken(context)
    val service = RetrofitClient.apiService
    LaunchedEffect(token) {
        getUserInfo(token, service, userInfoResponse)
        getDoctors(token, service, userDoctors)
        getMedications(service, context, allMedications, allMedications, userID)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = "Medication History", onNavigateToHomePage)
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(color = colorResource(R.color.DarkGrey)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        userInfo(userInfoResponse.value)
                    }
                    item {
                        doctorsList(userDoctors.value)
                    }
                    item {
                        medicationList(allMedications.value)
                    }
                    item {
                        pastAlarmSection("Past Alarms", pastAlarms)
                    }
                    item {
                        pendingAlarmSection("Pending Alarms", pendingAlarms)
                    }
                    item {
                        futureAlarmSection("Future Alarms", futureAlarms)
                    }
                }
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: String, onNavigateToHomePage: () -> Unit) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.DarkBlue),
            titleContentColor = Color.White
        ),
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { onNavigateToHomePage() }) {
                Icon(
                    tint = Color.White,
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "backAlert"
                )
            }
        },
    )
}

@Composable
fun userInfo(userInfo: UserInfoResponse?) {
    userInfo?.let {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Name: ${it.name}", color = Color.White)
            Text("Email: ${it.email}", color = Color.White)
        }
    }
}

@Composable
fun doctorsList(doctors: List<Doctor>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Doctors", fontSize = 20.sp, color = Color.White)
        doctors.forEach { doctor ->
            Text("Name: ${doctor.doctor_name}, Specialty: ${doctor.specialty}", color = Color.White)
            Text("Email: ${doctor.email}", color = Color.White)
            Text("Phone: ${doctor.office_number}", color = Color.White)
            Text("Address: ${doctor.office_address}", color = Color.White)
            Text("Emergency Contact: ${doctor.emergency_number}", color = Color.White)
        }
    }
}

@Composable
fun medicationList(medications: List<Medication>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Medications", fontSize = 20.sp, color = Color.White)
        medications.forEach { medication ->
            Text("Name: ${medication.name}, Dosage: ${medication.dosage}", color = Color.White)
            Text("Take every: ${medication.interval_milliseconds?.let { convertMillisecondsToHumanReadableTime(it) }}", color = Color.White)
        }
    }
}

@Composable
fun pastAlarmSection(title: String, alarms: List<PastAlarmItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, fontSize = 20.sp, color = Color.White)
        alarms.forEach { alarm ->
            Text("Medication: ${alarm.message}, Taken at: ${Date(alarm.timeMillis)}", color = Color.White)
        }
    }
}

@Composable
fun pendingAlarmSection(title: String, alarms: List<PendingAlarmItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, fontSize = 20.sp, color = Color.White)
        alarms.forEach { alarm ->
            Text("Medication: ${alarm.message}, Time: ${Date(alarm.timeMillis)}", color = Color.White)
            Text("Unknown if taken", color = Color.White)
        }
    }
}

@Composable
fun futureAlarmSection(title: String, alarms: List<FutureAlarmItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, fontSize = 20.sp, color = Color.White)
        alarms.forEach { alarm ->
            Text("Message: ${alarm.message}, Scheduled for: ${Date(alarm.timeMillis)}", color = Color.White)
        }
    }
}

@Composable
fun profileImage(imageSize: Dp,){ // 135 for big
    Surface(
        modifier = Modifier
            .size(imageSize + 17.dp)
            .padding(5.dp),
        shape = CircleShape,
        border = BorderStroke(0.5.dp, Color.LightGray),
        //elevation = 4.dp,
        color = MaterialTheme.colorScheme.secondary,

        ) {
        Image(
            painter = painterResource(id = R.drawable.medilens_logo),
            contentDescription = "profile image",
            modifier = Modifier.size(imageSize),
            contentScale = ContentScale.Crop
        )

    }
}



@Composable
fun MediCardsList(data: List<String>) {
    LazyColumn {
        items(data) { item ->
            Card(
                modifier = Modifier
                    .padding(13.dp)
                    .fillMaxWidth(),
                shape = RectangleShape,
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(7.dp)
                ) {
                    profileImage(50.dp)
                    Column(modifier = Modifier
                        .padding(7.dp)
                        .align(alignment = CenterVertically))
                    {
                        Text(
                            text = item,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}


private fun getMedications(
    service: ApiService,
    context: Context,
    allMedications: MutableState<List<Medication>>,
    medications: MutableState<List<Medication>>,
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
                medications.value = allMedications.value
            } else {
                Log.e("MediCard", "Failed to fetch medications")
            }
        }

        override fun onFailure(call: Call<List<Medication>>, t: Throwable) {
            Log.e("MediCard", "Failed to fetch medications", t)
        }
    })
}
private fun getDoctors(token: String, service: ApiService, doctors: MutableState<List<Doctor>>) {
    service.getUserDoctors(token).enqueue(object : Callback<List<Doctor>> {
        override fun onResponse(call: Call<List<Doctor>>, response: Response<List<Doctor>>) {
            if (response.isSuccessful) {
                doctors.value = response.body() ?: emptyList()
            } else {
                Log.e("Doctor", "Failed to get doctors")
            }
        }

        override fun onFailure(call: Call<List<Doctor>>, t: Throwable) {
            Log.e("Doctor", "Failed to get doctors")
        }
    })
}

private fun getUserInfo(token: String, service: ApiService, userInfoResponse: MutableState<UserInfoResponse?>) {
    service.getUserInfo(token).enqueue(object : Callback<UserInfoResponse> {
        override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
            if (response.isSuccessful) {
                userInfoResponse.value = response.body()
            } else {
                Log.e("UserInfo", "Failed to get user info")
            }
        }

        override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
            Log.e("UserInfo", "Failed to get user info")
        }
    })
}
