package com.ztch.medilens_android_app.Medicard

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Authenticate.decryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey
import com.ztch.medilens_android_app.Homepage.convertMillisToHumanReadableTime
import com.ztch.medilens_android_app.Notifications.AlarmViewModel
import com.ztch.medilens_android_app.Notifications.FutureAlarmItem
import com.ztch.medilens_android_app.Notifications.PastAlarmItem
import com.ztch.medilens_android_app.Notifications.PendingAlarmItem
import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.Refill.ImageSection
import com.ztch.medilens_android_app.Refill.InformationSection
import com.ztch.medilens_android_app.Refill.SharedMedicationModel
import com.ztch.medilens_android_app.Refill.getImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId


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
    var to by remember { mutableStateOf("") }
    val subject by remember { mutableStateOf("MediCard Information") }
    var body by remember { mutableStateOf("") }

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
                        PastAlarmSection("Past Medications", pastAlarms)
                    }
                    item {
                        PendingAlarmSection("Pending / Unknown Medications", pendingAlarms)
                    }
                    item {
                        FutureAlarmSection("Future Medications", futureAlarms)
                    }
                    // Text field for letting user input who to send the email to
                    item {
                        Text(
                            text = "Send Email",
                            color = Color.White,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        TextField(
                            value = to,
                            onValueChange = {
                                // do not allow whitspaces
                                to = it.replace(" ", "")
                                to = to.replace("\n", "")
                                to = to.replace("\t", "")

                                            },
                            label = { Text("To:") },
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.DarkGray,
                                focusedIndicatorColor = Color.White,
                                unfocusedIndicatorColor = Color.White
                            ),
                            // white text
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    item {

                        // Button to send email
                        Button(
                            onClick = {
                                body = buildString {
                                    append("<html>\n<head>\n<title>User Information</title>\n</head>\n<body>\n")

                                    // User information section
                                    append("<h1>User Information:</h1>\n")
                                    userInfoResponse.value?.let { userInfo ->
                                        append("<p>Name: ${userInfo.name}</p>\n")
                                        append("<p>Email: ${userInfo.email}</p>\n\n")
                                    }

                                    // User doctors section
                                    if (userDoctors.value.isNotEmpty()) {
                                        append("<h1>User Doctors:</h1>\n")
                                        userDoctors.value.forEach { doctor ->
                                            append("<h2>Doctor Name: ${doctor.doctor_name}</h2>\n")
                                            append("<p>Specialty: ${doctor.specialty}</p>\n")
                                            append("<p>Office Number: ${doctor.office_number}</p>\n")
                                            append("<p>Email: ${doctor.email}</p>\n")
                                            append("<p>Office Address: ${doctor.office_address}</p>\n\n")
                                        }
                                    }

                                    // Medications section
                                    if (allMedications.value.isNotEmpty()) {
                                        append("<h1>Medications:</h1>\n")
                                        allMedications.value.forEach { medication ->
                                            append("<h2>Medication Name: ${medication.name}</h2>\n")
                                            append("<p>Description: ${medication.description}</p>\n")
                                            append("<p>Color: ${medication.color}</p>\n")
                                            append("<p>Imprint: ${medication.imprint}</p>\n")
                                            append("<p>Shape: ${medication.shape}</p>\n")
                                            append("<p>Dosage: ${medication.dosage}</p>\n")
                                            append("<p>Intake Method: ${medication.intake_method}</p>\n\n")
                                        }
                                    }

                                    // Past alarms section
                                    if (pastAlarms.isNotEmpty()) {
                                        append("<h1>Past Alarms:</h1>\n")
                                        pastAlarms.forEach { alarm ->
                                            append("<p>Medication: ${alarm.message}, Taken at: ${convertMillisToHumanReadableTime(alarm.timeMillis)}</p>\n")
                                        }
                                        append("\n")
                                    }

                                    // Future alarms section
                                    if (futureAlarms.isNotEmpty()) {
                                        append("<h1>Future Alarms:</h1>\n")
                                        futureAlarms.forEach { alarm ->
                                            append("<p>Message: ${alarm.message}, Scheduled for: ${convertMillisToHumanReadableTime(alarm.timeMillis)}</p>\n")
                                        }
                                        append("\n")
                                    }

                                    // Pending alarms section
                                    if (pendingAlarms.isNotEmpty()) {
                                        append("<h1>Pending Alarms:</h1>\n")
                                        pendingAlarms.forEach { alarm ->
                                            append("<p>Medication: ${alarm.message}, Time: ${convertMillisToHumanReadableTime(alarm.timeMillis)}</p>\n")
                                            append("<p>Unknown if taken</p>\n")
                                        }
                                        append("\n")
                                    }

                                    append("</body>\n</html>")
                                }
                                val emailRequest = EmailRequest(to, subject, body)
                                service.sendEmail(token, emailRequest).enqueue(object : Callback<EmailResponse> {
                                    override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                                        if (response.isSuccessful) {
                                            val emailResponse = response.body()
                                            // Handle the email response
                                            emailResponse?.let {
                                                Log.d("medicard email suc", "Email sent successfully. Message: ${it.message}")
                                                onNavigateToHomePage()
                                            }
                                        } else {
                                            // Error sending email
                                            Log.e("medicard email error:", "Failed to send email: ${response.code()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                                        // Network error
                                        Log.e("medicard email error:", "Failed to send email: ${t.message}")
                                    }
                                })
                            }
                        ) {
                            Text("Send Email")
                        }
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
            DoctorCard(doctor)
        }
    }
}

@Composable
private fun DoctorCard(
    doctor: Doctor,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardColors(
            contentColor = Color.White,
            containerColor = colorResource(id = R.color.DarkBlue),
            disabledContentColor = Color.White,
            disabledContainerColor = colorResource(id = R.color.DarkBlue),
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Doctor: ${doctor.doctor_name}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Specialty: ${doctor.specialty}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Phone: ${doctor.office_number ?: ""}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Emergency: ${doctor.emergency_number ?: ""}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Address: ${doctor.office_address ?: ""}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Email: ${doctor.email}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


@Composable
fun medicationList(medications: List<Medication>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Medications", fontSize = 20.sp, color = Color.White)
        medications.forEach { medication ->
            MedicationBox(medication)
        }
    }
}

@Composable
private fun MedicationBox(medication: Medication) {
    val scheduleStart = medication.schedule_start?.let { convertToLocalDateTime(it) }
    val humanReadableScheduleStart = scheduleStart?.let { formatDateTime(it) } ?: "N/A"
    val humanReadableInterval = medication.interval_milliseconds?.let {
        "Every ${convertMillisecondsToHumanReadableTime(it)}"
    } ?: "N/A"
    Surface(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth(),

        shape = RoundedCornerShape(8.dp),
        color = colorResource(id = R.color.DarkBlue),
    ) {

        Card(
            // Make entire card DarkBlue
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.DarkBlue),
                contentColor = colorResource(R.color.DarkBlue),

                ),
            modifier = Modifier
                .fillMaxWidth()
                // take up as much height as needed
                .height(IntrinsicSize.Min)
                .padding(16.dp),

            ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                InformationSection(
                    name = medication.name,
                    description = medication.description ?: "N/A",
                    color = medication.color ?: "N/A",
                    imprint = medication.imprint ?: "N/A",
                    shape = medication.shape ?: "N/A",
                    dosage = medication.dosage ?: "N/A",
                    intakeMethod = medication.intake_method ?: "N/A",
                    scheduleStart = humanReadableScheduleStart,
                    interval = humanReadableInterval
                )
            }
        }
    }
}

@Composable
private fun PastAlarmSection(title: String, alarms: List<PastAlarmItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, fontSize = 20.sp, color = Color.White)
        alarms.forEach { alarm ->
            val humanReadableTime = convertMillisToHumanReadableTime(alarm.timeMillis)
            Card(
                colors = CardDefaults.cardColors(containerColor = if (alarm.response) Color(0xFF81C784) else Color(0xFFE57373)), // Green if taken, red if not
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Medication Taken: ${if (alarm.response) "Yes" else "No"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Medication: ${alarm.message}",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        //call formatDateTime on local timezone\
                        text = "Time: ${humanReadableTime}",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingAlarmSection(title: String, alarms: List<PendingAlarmItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, fontSize = 20.sp, color = Color.White)
        alarms.forEach { alarm ->
            val humanReadableTime = convertMillisToHumanReadableTime(alarm.timeMillis)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.DarkestBlue)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Medication: ${alarm.message}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Date: $humanReadableTime",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FutureAlarmSection(title: String, alarms: List<FutureAlarmItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, fontSize = 20.sp, color = Color.White)
        alarms.forEach { alarm ->
            val timeFormatted = convertMillisToHumanReadableTime(alarm.timeMillis)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.Grey)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(150.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Medication: ${alarm.message}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Scheduled Time: $timeFormatted",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//private fun ProfileImage(imageSize: Dp, logoId: Int) { // 135 for big
//    Surface(
//        modifier = Modifier
//            .size(imageSize + 17.dp)
//            .padding(5.dp),
//        shape = CircleShape,
//        border = BorderStroke(0.5.dp, Color.LightGray),
//        //elevation = 4.dp,
//        color = MaterialTheme.colorScheme.secondary,
//    ) {
//        Image(
//            painter = painterResource(id = logoId),
//            contentDescription = "profile image",
//            modifier = Modifier.size(imageSize),
//            contentScale = ContentScale.Crop
//        )
//    }
//}

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
