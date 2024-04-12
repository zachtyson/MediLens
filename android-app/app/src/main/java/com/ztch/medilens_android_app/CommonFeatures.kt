package com.ztch.medilens_android_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CardMembership
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun appbarBottom(onNavigateToCamera: () -> Unit,
                 onNavigateToAlarm: () -> Unit,
                 onNavigateToCabinet: () -> Unit,
                 onNavigateToSettings: () -> Unit,
                 onNavigateToMedicard: () -> Unit,)

{
    val colorPurple = colorResource(R.color.Purple)

    BottomAppBar(
        containerColor = colorResource(R.color.DarkBlue),
        actions = {

            Column(
                modifier = Modifier.padding(start = 24.dp)

            )
            {

                IconButton(
                    onClick = { onNavigateToMedicard() },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CardMembership,
                        contentDescription = "medicard",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text(text = "MediCard", color = Color.White)
            }

            Column(
                modifier = Modifier.padding(start = 24.dp)

            )
            {

                IconButton(
                    onClick = { onNavigateToAlarm() },
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "alerts",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text(text = "Alerts", color = Color.White)
            }


            Column(

                modifier = Modifier.padding(start = 24.dp)

            )
            {

                IconButton(
                    onClick = { onNavigateToCamera() },
                    modifier = Modifier.drawBehind {
                        drawCircle(
                            color = colorPurple,
                            radius = this.size.maxDimension,

                            )
                    }.clickable(onClick = { onNavigateToCamera() })
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "camera",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text(text = "")
            }

            Column(
                modifier = Modifier.padding(start = 24.dp)

            )
            {

                IconButton(
                    onClick = { onNavigateToCabinet() },
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = "Cabinet",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text(text = "Cabinet", color = Color.White)
            }

            Column(
                modifier = Modifier.padding(start = 24.dp)

            )
            {

                IconButton(
                    onClick = { onNavigateToSettings()},
                ) {
                    Icon(
                        imageVector = Icons.Default.Reorder,
                        contentDescription = "settings",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text(text = "Settings", color = Color.White)
            }

        },
    )
}
