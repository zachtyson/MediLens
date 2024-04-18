package com.ztch.medilens_android_app.Settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.DoctorCreate
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.Medicard.profileImage
import com.ztch.medilens_android_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPharmacist(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHomePage: () -> Unit,
    onNavigateToPharmacyInfo: () -> Unit
) {
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToLogin()
    }
    val token = TokenAuth.getLogInToken(context)
    val service = RetrofitClient.apiService


    var doctorName by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var officeNumber by remember { mutableStateOf("") }
    var officeAddress by remember { mutableStateOf("") }
    var emergencyNumber by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf("") }
    var successText by remember { mutableStateOf("") }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Use the padding provided by Scaffold for the content
                    .background(color = colorResource(R.color.DarkGrey)),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(colorResource(id = R.color.DarkGrey))
                ) {

                    Row ()
                    {

                        profileImage(imageSize = 50.dp)
                        OutlinedTextField(
                            value = doctorName,
                            onValueChange = { doctorName = it },
                            label = { Text("Doctor Name",color = Color.White) },

                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                                focusedContainerColor = colorResource(id = R.color.DarkBlue),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 0.dp)
                        )
                    }

                    OutlinedTextField(
                        value = specialization,
                        onValueChange = { specialization = it },
                        label = {
                            Text("Specialization", color = Color.White)
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp)
                    )

                    //include a spacer
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Doctor email", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp)
                    )
                    OutlinedTextField(
                        value = officeNumber,
                        onValueChange = { officeNumber = it },
                        label = { Text("Office number", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp)

                    )
                    OutlinedTextField(
                        value = officeAddress,
                        onValueChange = { officeAddress = it },
                        label = { Text("Office Address", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp)

                    )
                    Spacer(Modifier.height(18.dp))

                    OutlinedTextField(
                        value = emergencyNumber,
                        onValueChange = { emergencyNumber = it },
                        label = { Text("Emergency number", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    Button(
                        onClick = {
                            // At least one field has to have something in it
                            if (doctorName.isEmpty() && specialization.isEmpty() && email.isEmpty() && officeNumber.isEmpty() && officeAddress.isEmpty() && emergencyNumber.isEmpty()) {
                                errorText = "Please fill in at least one field"
                                return@Button
                            }
                            // doctor number and emergency number cannot be more than 20 characters
                            if (officeNumber.length > 20 || emergencyNumber.length > 20) {
                                errorText = "Phone numbers cannot be more than 20 characters"
                                return@Button
                            }
                            errorText = ""
                            // Add doctor api
                            val doctor = DoctorCreate(
                                doctor_name = doctorName,
                                specialty = specialization,
                                office_number = officeNumber,
                                emergency_number = emergencyNumber,
                                office_address = officeAddress,
                                email = email
                            )// Call<Map<String, String>>
                            service.addDoctor(token, doctor).enqueue(object : Callback<Map<String, String>> {
                                override fun onResponse(
                                    call: Call<Map<String, String>>,
                                    response: Response<Map<String, String>>
                                ) {
                                    if (response.isSuccessful) {
                                        successText = "Doctor added successfully"
                                        onNavigateToPharmacyInfo()
                                    } else {
                                        errorText = "Error adding doctor"
                                    }
                                }

                                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                    errorText = "Error adding doctor"
                                    Log.e("DoctorScreen", "Error adding doctor", t)
                                }
                            })
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.Purple)),
                        modifier = Modifier
                            .size(150.dp, 50.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("SubmitDoctorButton"),
                    ) {
                        Text("Submit")
                    }
                    Text(
                        text = errorText,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = successText,
                        color = Color.Green,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                }
            }
        }
    )
}
