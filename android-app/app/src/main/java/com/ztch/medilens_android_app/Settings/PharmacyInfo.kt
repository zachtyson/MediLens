package com.ztch.medilens_android_app.Settings

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.Medicard.profileImage
import com.ztch.medilens_android_app.R
@Preview(showSystemUi = true)
@Composable
fun doctorPreview() {
    DoctorScreen( onNavigateToSettings = {}, onNavigateToLogin = {})

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToLogin()
    }

    var doctorName by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var officeNumber by remember { mutableStateOf("") }
    var officeAddress by remember { mutableStateOf("") }
    var emergencyNumber by remember { mutableStateOf("") }


    val onSaveDoctorInfo: () -> Unit = {
        // save doctor info


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
                actions = {
                    IconButton(
                        onClick = { onSaveDoctorInfo() }
                    ) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save"
                        )
                    }
                }
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
                            value = "", onValueChange = { },
                            label = { Text("Doctor Name",color = Color.White) },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                                focusedContainerColor = colorResource(id = R.color.DarkBlue),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 0.dp),
                            leadingIcon = {
                                Image(
                                    painter = painterResource(id = R.drawable.face_icon),
                                    contentDescription = "Face Icon",
                                    modifier = Modifier.size(24.dp)

                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
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
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.job_icon),
                                contentDescription = "Job Icon",
                                modifier = Modifier.size(24.dp)

                            )
                        }
                    )

                    //include a spacer
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        label = { Text("doctor@email.com", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.email_logo),
                                contentDescription = "Email Icon",
                                modifier = Modifier.size(24.dp)

                            )
                        }
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        label = { Text("Office number", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.phone_icon),
                                contentDescription = "Phone Icon",
                                modifier = Modifier.size(24.dp)

                            )
                        }

                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        label = { Text("Office Address", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.office_icon),
                                contentDescription = "Office Icon",
                                modifier = Modifier.size(24.dp)

                            )
                        }

                    )
                    Spacer(Modifier.height(18.dp))

                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        label = { Text("Emergency number", color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedContainerColor = colorResource(id = R.color.DarkBlue),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.contact_emergency_icon),
                                contentDescription = "Emergency Icon",
                                modifier = Modifier.size(24.dp)

                            )
                        }
                    )

                }
            }
        }
    )
}



