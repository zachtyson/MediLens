package com.ztch.medilens_android_app.Settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.Doctor
import com.ztch.medilens_android_app.ApiUtils.MedicationModify
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.Authenticate.createRandomIV
import com.ztch.medilens_android_app.Authenticate.encryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey
import com.ztch.medilens_android_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyPharmacist(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHomePage: () -> Unit,
    onNavigateToPharmacyInfo: () -> Unit,
    sharedDoctorModel: SharedDoctorModel
) {
    Log.d("ModifyPharmacist", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    if (sharedDoctorModel.doctor == null) {
        // if doctor is not set, navigate to pharmacy info page
        onNavigateToPharmacyInfo()
    }

    val doctor = sharedDoctorModel.doctor!!

    val token = TokenAuth.getLogInToken(context)

    var doctorName by remember { mutableStateOf(doctor.doctor_name ?: "") }
    var specialization by remember { mutableStateOf(doctor.specialty ?: "") }
    var email by remember { mutableStateOf(doctor.email ?: "") }
    var officeNumber by remember { mutableStateOf(doctor.office_number ?: "") }
    var officeAddress by remember { mutableStateOf(doctor.office_address ?: "") }
    var emergencyNumber by remember { mutableStateOf(doctor.emergency_number ?: "") }

    var errorText by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.DarkBlue),
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "Modify Medication",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToPharmacyInfo() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Localized description"
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {

                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                            // Change Color.Red to your desired focused border color
                        ),
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Doctor Name", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = specialization,
                        onValueChange = { specialization = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Specialization", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Email", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = officeNumber,
                        onValueChange = { officeNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Office Number", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = officeAddress,
                        onValueChange = { officeAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Office Address", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = emergencyNumber,
                        onValueChange = { emergencyNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Emergency Number", color = Color.White)
                        }
                    )

                    if (errorText.isNotEmpty()) {
                        Snackbar(
                            modifier = Modifier.padding(top = 8.dp),
                            action = {
                                Button(
                                    onClick = { errorText = "All text fields required!" }
                                ) {
                                    Text(text = "Close", color = Color.White)
                                }
                            }
                        ) {
                            Text(text = errorText, color = Color.White)
                        }
                    }

                    Button(
                        onClick = {
                            // At least one field has to be not empty
                            if (doctorName.isEmpty() && specialization.isEmpty() && email.isEmpty() && officeNumber.isEmpty() && officeAddress.isEmpty() && emergencyNumber.isEmpty()) {
                                errorText = "At least one field required!"
                            } else {
                                val d = Doctor(
                                    doctor_name = doctorName,
                                    specialty = specialization,
                                    email = email,
                                    office_number = officeNumber,
                                    office_address = officeAddress,
                                    emergency_number = emergencyNumber,
                                    doctor_id = doctor.doctor_id,
                                    owner_id = doctor.owner_id
                                )

                                val call = RetrofitClient.apiService.modifyDoctor(token, d)
                                Log.d("ModifyPharmacist", "Call: $call")
                                call.enqueue(object : Callback<Map<String,String>> {
                                    override fun onResponse(
                                        call: Call<Map<String, String>>,
                                        response: Response<Map<String, String>>
                                    ) {
                                        if (response.isSuccessful) {
                                            onNavigateToSettings()
                                        } else {
                                            errorText = "Failed to modify doctor"
                                        }
                                    }

                                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                        errorText = "Failed to modify doctor"
                                    }
                                })
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Modify", color = Color.White)
                    }
                }
            }
        }

    )
}
