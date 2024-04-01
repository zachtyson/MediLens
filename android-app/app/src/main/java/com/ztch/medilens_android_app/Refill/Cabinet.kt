package com.ztch.medilens_android_app.Refill

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Camera.SharedViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*

@Composable
fun Cabinet (
    onNavigateToHomePage: () -> Unit,
    onNavigateToAlarm: () -> Unit
) {
    val service = RetrofitClient.apiService
    Log.d("Cabinet", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }

    // Fetch all medications that are in the cabinet

    // mutable state empty list medication
    val medications = remember { mutableStateOf<List<Medication>>(emptyList()) }

    // fetch all medications from the server
    LaunchedEffect(Unit) {
        service.getMedications(TokenAuth.getLogInToken(context)).enqueue(object : Callback<List<Medication>> {
            override fun onResponse(call: Call<List<Medication>>, response: Response<List<Medication>>) {
                if (response.isSuccessful) {
                    medications.value = response.body()!!
                } else {
                    Log.e("Cabinet", "Failed to fetch medications")
                }
            }

            override fun onFailure(call: Call<List<Medication>>, t: Throwable) {
                Log.e("Cabinet", "Failed to fetch medications", t)
            }
        })
    }

    // Column of boxes, each box is a medication

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        for (medication in medications.value) {
            item {
                MedicationBox(medication = medication)
            }
        }
    }
}

@Composable
fun MedicationBox(medication: Medication) {
    // turn schedule_start into local date time and human readable format
    var scheduleStart: LocalDateTime? = null
    if (medication.schedule_start != null) {
        scheduleStart = convertToLocalDateTime(medication.schedule_start)
    }
    var humanReadableScheduleStart = "N/A"
    if (scheduleStart != null) {
        humanReadableScheduleStart = formatDateTime(scheduleStart)
    }

    var humanReadableInterval = "N/A"
    if (medication.interval_milliseconds != null) {
        humanReadableInterval = "Every " + convertMillisecondsToHumanReadableTime(medication.interval_milliseconds)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Assuming getImage returns an ImageBitmap. Adjust this part based on your actual image loading logic.
        val image: ImageBitmap = getImage(medication)

        // Image section
        Image(
            bitmap = image,
            contentDescription = "Medication Image",
            modifier = Modifier
                .weight(3f) // Takes 30% of the width
                .fillMaxHeight(),
            contentScale = ContentScale.Fit
        )

        // Information section
        Column(
            modifier = Modifier
                .weight(7f) // Takes the remaining 70% of the width
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            Text(text = "Name: ${medication.name}")
            Text(text = "Description: ${medication.description ?: "N/A"}")
            Text(text = "Color: ${medication.color ?: "N/A"}")
            Text(text = "Imprint: ${medication.imprint ?: "N/A"}")
            Text(text = "Shape: ${medication.shape ?: "N/A"}")
            Text(text = "Dosage: ${medication.dosage ?: "N/A"}")
            Text(text = "Intake Method: ${medication.intake_method ?: "N/A"}")
            Text(text = "Start Date: ${humanReadableScheduleStart ?: "N/A"}")
            Text(text = "Interval: ${humanReadableInterval ?: "N/A"}")
        }
    }
}
@Composable
fun getImage(medication: Medication): ImageBitmap {
    return ImageBitmap(100, 100) // Placeholder, replace with actual image loading
}
