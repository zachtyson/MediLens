package com.ztch.medilens_android_app.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.Medicard.ProfileImage
import com.ztch.medilens_android_app.R
import java.time.format.TextStyle


@Preview(showSystemUi = true)
@Composable
fun mediPreview() {
    DoctorScreen( onNavigateToSettings = {})
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScreen(onNavigateToSettings: () -> Unit ) {
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToSettings()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Use the padding provided by Scaffold for the content
                    .background(color = colorResource(R.color.DarkGrey)),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {

              DoctorInfo()
            }
        }
    )
}

 @Composable
fun DoctorInfo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colorResource(id = R.color.DarkGrey))
    ) {

        Row ()
        {

            ProfileImage(imageSize = 50.dp)
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
                .padding(horizontal = 8.dp, vertical = 0.dp)
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
                .padding(horizontal = 8.dp, vertical = 0.dp)
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
                .padding(horizontal = 8.dp, vertical = 0.dp)
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
                .padding(horizontal = 8.dp, vertical = 0.dp)

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
                .padding(horizontal = 8.dp, vertical = 0.dp)

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
                .padding(horizontal = 8.dp)
        )

        }
    }

