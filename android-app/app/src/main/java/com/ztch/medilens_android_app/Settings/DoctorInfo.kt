package com.ztch.medilens_android_app.Settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Preview(showSystemUi = true)
@Composable
fun doctorPreview() {
    PharmacyInfo( onNavigateToSettings = {}, onNavigateToLogin = {}, onNavigateToAddPharmacist = {}, onNavigateToModifyPharmacist = {}, sharedDoctorModel = SharedDoctorModel())
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyInfo(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddPharmacist: () -> Unit,
    onNavigateToModifyPharmacist: () -> Unit,
    sharedDoctorModel: SharedDoctorModel
) {
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToLogin()
    }
    val token = TokenAuth.getLogInToken(context)
    val service = RetrofitClient.apiService

    val doctors = remember { mutableStateOf<List<Doctor>>(emptyList()) }
    LaunchedEffect(token) {
        getDoctors(token, service, doctors)
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
                        "Add Doctor",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "backAlert"
                        )
                    }
                },
            )
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->
            // Button that navigates to AddPharmacist
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.DarkestBlue))
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    doctors.value.forEach { doctor ->
                        DoctorCard(doctor, onNavigateToModifyPharmacist, sharedDoctorModel)
                    }
                }
                Button(
                    onClick = { onNavigateToAddPharmacist() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.DarkBlue),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Add Pharmacist")
                    Text("Add Doctor", color = Color.White)
                }
            }
        }
    )

}

fun getDoctors(token: String, service: ApiService, doctors: MutableState<List<Doctor>>) {
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

@Composable
fun DoctorCard(doctor: Doctor, onNavigateToModifyPharmacist: () -> Unit, sharedDoctorModel: SharedDoctorModel) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        sharedDoctorModel.doctor = doctor
                        onNavigateToModifyPharmacist()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.DarkBlue),
                        contentColor = Color.White
                    )
                ) {
                    Text("Modify", color = Color.White)
                }
                Button(
                    onClick = {/*TODO*/},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.DarkBlue),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}
