package com.ztch.medilens_android_app

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp

@Preview()
@Composable
fun DefaultPreview() {
    appbarBottom(
        onNavigateToCamera = {},
        onNavigateToAlarm = {},
        onNavigateToCabinet = {},
        onNavigateToSettings = {},
        onNavigateToMedicard = {},
    )
}

@Composable
fun appbarBottom(
    onNavigateToCamera: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToCabinet: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMedicard: () -> Unit,
) {

    BottomAppBar(
        containerColor = colorResource(R.color.DarkBlue),
        actions = {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionItem(
                    onClick = { onNavigateToMedicard() },
                    icon = R.drawable.medicard_logo,
                    label = "MediCard"
                )

                ActionItem(
                    onClick = { onNavigateToAlarm() },
                    icon = R.drawable.alarm_logo,
                    label = "Alerts"
                )

                ActionCircleItem(
                    onClick = { onNavigateToCamera() },
                    icon = R.drawable.photo_camera_logo,
                    label = "Camera"
                )

                ActionItem(
                    onClick = { onNavigateToCabinet() },
                    icon = R.drawable.prescriptions_logo,
                    label = "Cabinet"
                )

                ActionItem(
                    onClick = { onNavigateToSettings() },
                    icon = R.drawable.settings_logo,
                    label = "Settings"
                )
            }
        },
    )
}

@Composable
fun ActionItem(onClick: () -> Unit, @DrawableRes icon: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(35.dp)
        )
        Text(text = label, color = Color.White)
    }
}

@Composable
fun ActionCircleItem(onClick: () -> Unit, @DrawableRes icon: Int, label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp) // Adjust the size of the circle as needed
                .background(color =  colorResource(R.color.Purple), shape = CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(35.dp)
            )
            Text(text = label, color = Color.White)
        }
    }
}


@Composable
fun GradientBackground(
    colors: List<Color>,
    orientation: GradientOrientation,
) {
    val brush = when (orientation) {
        GradientOrientation.Vertical -> Brush.verticalGradient(colors)
        GradientOrientation.Horizontal -> Brush.horizontalGradient(colors)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    )
}
enum class GradientOrientation {
    Vertical,
    Horizontal
}

