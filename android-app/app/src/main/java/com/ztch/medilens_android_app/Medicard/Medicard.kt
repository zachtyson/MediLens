package com.ztch.medilens_android_app.Medicard

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Notifications.AlarmViewModel
import com.ztch.medilens_android_app.Notifications.FutureAlarmItem
import com.ztch.medilens_android_app.Notifications.PastAlarmItem
import com.ztch.medilens_android_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
    val pastAlarms by alarmViewModel?.past_alarms?.collectAsState(initial = emptyPastAlarms) ?: remember { mutableStateOf(emptyPastAlarms) }
    val futureAlarms by alarmViewModel?.future_alarms?.collectAsState(initial = emptyFutureAlarms) ?: remember { mutableStateOf(emptyFutureAlarms) }
    val token = TokenAuth.getLogInToken(context)
    val service = RetrofitClient.apiService
    LaunchedEffect(token) {
        getUserInfo(token, service, userInfoResponse)
        getDoctors(token, service, userDoctors)
        getMedications(token, service, allMedications)
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
                        "Medication History",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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

                    userInfo()
                    MediCardBox()
                }
        }
    )
}

@Composable
fun profileImage(imageSize: Dp,){ // 135 for big
        Surface(
            modifier = Modifier
                .size(imageSize+17.dp)
                .padding(5.dp),
            shape = CircleShape,
            border = BorderStroke(0.5.dp, Color.LightGray),
            //elevation = 4.dp,
            color = MaterialTheme.colorScheme.secondary,

            ) {
            Image(
                painter = painterResource(id = R.drawable.medilens_logo),
                contentDescription = "profile image",
                modifier = Modifier.size(imageSize),
                contentScale = ContentScale.Crop
            )

        }
}

@Composable
fun userInfo() {
    Column(
        modifier = Modifier
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        profileImage(135.dp)
        Text(
            text = "FetchName()",
            color = Color.White,
            fontSize = 16.sp,
        )

        Text(
            text = "FetchEmail()",
            color = Color.White,
            fontSize = 16.sp,
        )

        Text(
            text = "@Medicard",
            fontSize = 24.sp,
            color = Color.Magenta,
            modifier = Modifier
                .clickable(onClick = { /*TODO*/})
        )

    }
}

@Composable
fun MediCardBox() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Surface(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(corner = CornerSize(6.dp)),
            border = BorderStroke(
                width = 2.dp,
                color = Color.LightGray
            )
        )
        {
            MediCardsList(
                data = listOf(
                    "Cabinet()",
                    "Alarms()",
                    "PharmcyInfo()",
                    "Personal Information()",
                )
            )
        }
    }
}



@Composable
fun MediCardsList(data: List<String>) {
    LazyColumn {
        items(data) { item ->
            Card(
                modifier = Modifier
                    .padding(13.dp)
                    .fillMaxWidth(),
                shape = RectangleShape,
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(7.dp)
                ) {
                    profileImage(50.dp)
                    Column(modifier = Modifier
                        .padding(7.dp)
                        .align(alignment = CenterVertically))
                    {
                        Text(
                            text = item,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}


private fun getMedications (token: String, service: ApiService, allMedications: MutableState<List<Medication>>) {
    service.getMedications(token).enqueue(object : Callback<List<Medication>> {
        override fun onResponse(call: Call<List<Medication>>, response: Response<List<Medication>>) {
            if (response.isSuccessful) {
                allMedications.value = response.body() ?: emptyList()
            } else {
                Log.e("Medication", "Failed to get medications")
            }
        }

        override fun onFailure(call: Call<List<Medication>>, t: Throwable) {
            Log.e("Medication", "Failed to get medications")
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
