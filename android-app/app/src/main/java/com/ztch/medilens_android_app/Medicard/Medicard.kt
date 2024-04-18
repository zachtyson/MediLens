package com.ztch.medilens_android_app.Medicard

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
import androidx.compose.runtime.Composable
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
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.Notifications.AlarmViewModel
import com.ztch.medilens_android_app.R



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
