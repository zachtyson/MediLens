package com.ztch.medilens_android_app.Settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.*
import com.ztch.medilens_android_app.Camera.SharedViewModel
import com.ztch.medilens_android_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings (
    onNavigateToHomePage: () -> Unit,
    onNavigateToAlarm: () -> Unit
) {
    val service = RetrofitClient.apiService
    Log.d("Cabinet", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }

    // Column of boxes, each box is a medication
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    if (showChangeEmailDialog) {
        ChangeEmailDialog(onDismiss = { showChangeEmailDialog = false })
    }

    if (showDeleteAccountDialog) {
        ConfirmDeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onDeleteConfirmed = {
                // Handle account deletion here
                showDeleteAccountDialog = false
                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                // Navigate to login or home screen
                TokenAuth.logOut(context)
                onNavigateToHomePage()
            }
        )
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
                        "Settings",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToHomePage() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Return to home screen"
                        )
                    }
                },
            )
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .width(400.dp)
                    .padding(innerPadding) // Use the padding provided by Scaffold for the content
                    .background(color = colorResource(R.color.DarkGrey))
            ) {
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Email,
                        title = "Change Email",
                        onClick = {  }
                    )
                    Divider()
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Password,
                        title = "Change Password",
                        onClick = {  }
                    )
                    Divider()
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Delete,
                        title = "Delete Account",
                        onClick = {  }
                    )
                    Divider()
                }


            }
        }
    )
}

@Composable
fun SettingsOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title)
    }
}


@Composable
fun ChangeEmailDialog(onDismiss: () -> Unit) {
    var newEmail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Change Email") },
        text = {
            Column {
                Text("Enter your new email address:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("Email") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Add logic to change email
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Change Password") },
        text = {
            Column {
                Text("Enter your new password:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Password") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Add logic to change password
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ConfirmDeleteAccountDialog(onDismiss: () -> Unit, onDeleteConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete Account") },
        text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onDeleteConfirmed) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
