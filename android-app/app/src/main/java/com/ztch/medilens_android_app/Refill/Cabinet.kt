package com.ztch.medilens_android_app.Refill

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Camera.SharedViewModel
import com.ztch.medilens_android_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.Authenticate.decryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cabinet (
    onNavigateToHomePage: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToModifyMedication: () -> Unit,
    onNavigateToScheduleMedication: () -> Unit,
    sharedMedicationModel: SharedMedicationModel
) {
    val service = RetrofitClient.apiService
    Log.d("Cabinet", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    val topText = remember { mutableStateOf("Medications") }
    val userIsScheduling = remember { sharedMedicationModel.userIsScheduling }

    if (sharedMedicationModel.userIsScheduling) {
        topText.value = "Unscheduled Medications"
    }


    // Fetch all medications that are in the cabinet

    // mutable state empty list medication
    val medications = remember { mutableStateOf<List<Medication>>(emptyList()) }
    val allMedications = remember { mutableStateOf<List<Medication>>(emptyList()) }

    // fetch all medications from the server
    LaunchedEffect(Unit) {
        fetchMedications(service, context, allMedications, medications, userIsScheduling)
    }

    // Column of boxes, each box is a medication

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.DarkBlue),
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        topText.value,
                        maxLines = 1,
                        // lower font size
                        fontSize = 18.sp,
                        overflow = TextOverflow.Ellipsis)

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
                    Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        // center objects horizontally
                        IconButton(onClick = {
                            toggleUserIsScheduling(
                                sharedMedicationModel,
                                topText,
                                medications,
                                allMedications
                            )
                        }) {
                            Icon(
                                tint = Color.White,
                                imageVector = Icons.Filled.FilterAlt,
                                contentDescription = "Localized description"
                            )
                        }
                        IconButton(onClick = { onNavigateToAddMedication() }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                tint = Color.White,
                                contentDescription = "addMedication"
                            )
                        }

                    }
                },

                )
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .width(400.dp)
                    .padding(innerPadding) // Use the padding provided by Scaffold for the content
                    .background(color = colorResource(R.color.DarkGrey))
            ) {
                for (medication in medications.value) {
                    item {
                        MedicationBox(medication = medication, sharedMedicationModel = sharedMedicationModel,
                            onNavigateToModifyMedication = onNavigateToModifyMedication,
                            onNavigateToScheduleMedication = onNavigateToScheduleMedication)
                    }
                }
            }
        }
    )
}
private fun fetchMedications(
    service: ApiService,
    context: Context,
    allMedications: MutableState<List<Medication>>,
    medications: MutableState<List<Medication>>,
    userIsScheduling: Boolean
) {

    service.getMedications(TokenAuth.getLogInToken(context)).enqueue(object : Callback<List<Medication>> {
        override fun onResponse(call: Call<List<Medication>>, response: Response<List<Medication>>) {
            if (response.isSuccessful) {
                allMedications.value = response.body() ?: emptyList()
                // iterate over medications and decrypt them
                for (medication in allMedications.value) {
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
                if (userIsScheduling) {
                    // if user is scheduling, filter out medications that are already scheduled
                    medications.value = medications.value.filter { medication ->
                        medication.schedule_start == null }
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

fun toggleUserIsScheduling(sharedMedicationModel: SharedMedicationModel,
                           topText: MutableState<String>,
                           medications: MutableState<List<Medication>>,
                           allMedications: MutableState<List<Medication>>) {
    sharedMedicationModel.userIsScheduling = !sharedMedicationModel.userIsScheduling
    if (sharedMedicationModel.userIsScheduling) {
        topText.value = "Unscheduled Medications"
    } else {
        topText.value = "Medications"
    }
    medications.value = allMedications.value
    if (sharedMedicationModel.userIsScheduling) {
        // if user is scheduling, filter out medications that are already scheduled
        medications.value = medications.value.filter { medication ->
            medication.schedule_start == null }
    }
}

@Composable
fun MedicationBox(medication: Medication,
                  sharedMedicationModel: SharedMedicationModel,
                  onNavigateToModifyMedication: () -> Unit,
                  onNavigateToScheduleMedication: () -> Unit
) {
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
                // Medication image on the left
                val image: ImageBitmap = getImage(IntSize(100, 100))
                ImageSection(image = image)
                Spacer(modifier = Modifier.width(8.dp)) // Add some space between the image and the text

                // Medication details on the right
                Column { // Keeps the text vertical
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
                    // Box for two buttons 'Modify' and 'Schedule'
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                sharedMedicationModel.medication = medication
                                onNavigateToModifyMedication()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.LightBlue),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Modify", fontSize = 10.sp, color = Color.White, textAlign = TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                sharedMedicationModel.medication = medication
                                onNavigateToScheduleMedication()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.LightBlue),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Schedule", fontSize = 10.sp, color = Color.White, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun InformationSection(name: String, description: String, color: String, imprint: String, shape: String, dosage: String, intakeMethod: String, scheduleStart: String, interval: String) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
        MedInfoText("Description: $description")
        MedInfoText("Color: $color")
        MedInfoText("Imprint: $imprint")
        MedInfoText("Shape: $shape")
        MedInfoText("Dosage: $dosage")
        MedInfoText("Intake Method: $intakeMethod")
        MedInfoText("Schedule Start: $scheduleStart")
        MedInfoText("Interval: $interval")
    }
}
@Composable
fun MedInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White
    )
}


@Composable
fun ImageSection(image: ImageBitmap) {
    Image(
        bitmap = image,
        contentDescription = "Medication Image",
        modifier = Modifier
            .fillMaxHeight()
            .width(100.dp),
        contentScale = ContentScale.Fit
    )
}
@Composable
fun getImage(size: IntSize): ImageBitmap {
    val imageBitmap = ImageBitmap(size.width, size.height)
    val canvas = Canvas(imageBitmap)

    canvas.drawCircle(
        center = Offset(size.width / 2f, size.height / 2f),
        radius = size.width / 2f,
        paint = Paint().apply {
            color = Color.Red
        }
    )

    return imageBitmap
}
