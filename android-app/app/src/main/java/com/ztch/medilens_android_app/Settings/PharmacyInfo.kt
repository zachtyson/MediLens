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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Medicard.profileImage
import com.ztch.medilens_android_app.R
@Preview(showSystemUi = true)
@Composable
fun doctorPreview() {
    PharmacyInfo( onNavigateToSettings = {}, onNavigateToLogin = {})
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyInfo(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddPharmacist: () -> Unit = {},
) {
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToLogin()
    }
    val token = TokenAuth.getLogInToken(context)
    val service = RetrofitClient.apiService

    val doctors = remember { mutableStateOf<List<Doctor>>(emptyList()) }

    // Button that navigates to AddPharmacist
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.DarkestBlue))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = { Text("Doctor Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.DarkBlue),
                    titleContentColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                doctors.value.forEach { doctor ->
                    DoctorCard(doctor)
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

}

@Composable
fun DoctorCard(doctor: Doctor) {
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
                text = doctor.doctor_name ?: "",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = doctor.specialty ?: "",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = doctor.office_number ?: "",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = doctor.emergency_number ?: "",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = doctor.office_address ?: "",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = doctor.email,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
