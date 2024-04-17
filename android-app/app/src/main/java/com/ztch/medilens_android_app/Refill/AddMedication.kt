package com.ztch.medilens_android_app.Refill

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Authenticate.createRandomIV
import com.ztch.medilens_android_app.Authenticate.encryptData
import com.ztch.medilens_android_app.Authenticate.getLocalEncryptionKey
import com.ztch.medilens_android_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedication (
    onNavigateToHomePage: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToCabinet: () -> Unit,
    userMedicationViewModel : UserMedicationViewModel
) {
    // Add Medication

    val service = RetrofitClient.apiService
    Log.d("AddMedication", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }

    var medicationName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var imprint by remember { mutableStateOf("") }
    var shape by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var intakeMethod by remember { mutableStateOf("") }
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
                        "Add Medication",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToCabinet() }) {
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
                        value = medicationName,
                        onValueChange = { medicationName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Medication Name", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Description", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = color,
                        onValueChange = { color = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Color", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = imprint,
                        onValueChange = { imprint = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Imprint", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = shape,
                        onValueChange = { shape = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Shape", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = dosage,
                        onValueChange = { dosage = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Dosage", color = Color.White)
                        }
                    )
                    OutlinedTextField(
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        value = intakeMethod,
                        onValueChange = { intakeMethod = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Intake Method", color = Color.White)
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
                            if (medicationName.isEmpty() || description.isEmpty() || color.isEmpty() || imprint.isEmpty() || shape.isEmpty()) {
                                errorText = "All text fields required!"
                            } else {
                                // encrypt medication
                                val encryptionKey = getLocalEncryptionKey(context)
                                val iv = createRandomIV()
                                Log.d("AddMedication", "IV: $iv")
                                Log.d("AddMedication", "Encryption Key: $encryptionKey")
                                Log.d("AddMedication", "Medication Name: $medicationName")

                                val initVector = iv
                                val token = TokenAuth.getLogInToken(context)

                                val mc = MedicationCreate(
                                    name = encryptData(medicationName, encryptionKey, iv),
                                    description = encryptData(description, encryptionKey, iv),
                                    color = encryptData(color, encryptionKey, iv),
                                    imprint = encryptData(imprint, encryptionKey, iv),
                                    shape = encryptData(shape, encryptionKey, iv),
                                    dosage = encryptData(dosage, encryptionKey, iv),
                                    intake_method = encryptData(intakeMethod, encryptionKey, iv),
                                    init_vector = initVector,
                                )

                                val call = RetrofitClient.apiService.addMedication(token, mc)
                                call.enqueue(object : retrofit2.Callback<Map<String, String>> {
                                    override fun onResponse(call: retrofit2.Call<Map<String, String>>, response: retrofit2.Response<Map<String, String>>) {
                                        if (response.isSuccessful) {
                                            Log.d("AddMedication", "Medication added successfully")
                                            // After medication is added make dialogs for every interaction'

                                            // list<String>
                                            var drugs = mutableListOf<String>()
                                            for (medication in userMedicationViewModel.unencryptedMedications) {
                                                drugs.add(medication.name)
                                            }

                                            if (errorText.isEmpty()) {
                                                onNavigateToCabinet()
                                            }
                                        } else {
                                            Log.d("AddMedication", "Failed to add medication")
                                            errorText = "Failed to add medication"
                                        }
                                    }

                                    override fun onFailure(call: retrofit2.Call<Map<String, String>>, t: Throwable) {
                                        Log.d("AddMedication", "Failed to add medication")
                                        errorText = "Failed to add medication"
                                    }
                                })

                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Add Medication to Cabinet", color = Color.White)
                    }
                }
            }
        }

    )
}

