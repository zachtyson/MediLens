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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

fun LogOutAfterDelete(context: Context, onNavigateToHomePage: () -> Unit) {
    TokenAuth.logOut(context)
    onNavigateToHomePage()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings (
    onNavigateToHomePage: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToDoctor: () -> Unit
) {
    val service = RetrofitClient.apiService
    Log.d("Cabinet", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }

    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogOutDialog by remember { mutableStateOf(false) }

    var onEmailUpdateSuccess by remember { mutableStateOf<Boolean?>(null) }

    if (showChangeEmailDialog) {
        ChangeEmailDialog(
            onDismiss = { showChangeEmailDialog = false },
            context = context,
            onUpdateSuccess = { onEmailUpdateSuccess = true },
            onUpdateFailure = { Toast.makeText(context, "Failed to update email", Toast.LENGTH_SHORT).show() }
        )
    }


    if (showDeleteAccountDialog) {
        ConfirmDeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            context = context,
            onUpdateSuccess = { LogOutAfterDelete(context, onNavigateToHomePage)},
            onUpdateFailure = { Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show() }
        )
    }


    var onPasswordUpdateSuccess by remember { mutableStateOf<Boolean?>(null) }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            context = context,
            onUpdateFailure = { Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show() },
            onUpdateSuccess = { onPasswordUpdateSuccess = null }
        )
    }

    if (showLogOutDialog) {
        ConfirmLogOutDialog(
            onDismiss = { showLogOutDialog = false },
            onLogOutConfirmed = {
                // Handle log out here
                showLogOutDialog = false
                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
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
                        onClick = { showChangeEmailDialog = true }
                    )
                    HorizontalDivider()
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Password,
                        title = "Change Password",
                        onClick = { showChangePasswordDialog = true }
                    )
                    HorizontalDivider()
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Logout,
                        title = "Log Out",
                        onClick = { showLogOutDialog = true }
                    )
                    HorizontalDivider()
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Delete,
                        title = "Delete Account",
                        onClick = { showDeleteAccountDialog = true }
                    )
                    HorizontalDivider()
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.LocalPharmacy,
                        title = "Add Doctor",
                        onClick = { onNavigateToDoctor() }
                    )
                    HorizontalDivider()
                }

            }
        }
    )
}

//private fun fetchMedications(
//    service: ApiService,
//    context: Context,
//    medications: MutableState<List<Medication>>
//) {
//    service.getMedications(TokenAuth.getLogInToken(context)).enqueue(object : Callback<List<Medication>> {
//        override fun onResponse(call: Call<List<Medication>>, response: Response<List<Medication>>) {
//            if (response.isSuccessful) {
//                medications.value = response.body() ?: emptyList()
//            } else {
//                Log.e("Cabinet", "Failed to fetch medications")
//            }
//        }
//
//        override fun onFailure(call: Call<List<Medication>>, t: Throwable) {
//            Log.e("Cabinet", "Failed to fetch medications", t)
//        }
//    })
//}

private fun updateUserEmail(
    service: ApiService,
    context: Context,
    oldEmail: String,
    newEmail: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    service.updateEmail(TokenAuth.getLogInToken(context), oldEmail, newEmail, password).enqueue(object : Callback<RegisterResponse> {
        override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
            if (response.isSuccessful) {
                Log.d("Settings", "Email updated")
                onSuccess()
            } else {
                Log.e("Settings", "Failed to update email")
                onFailure("Failed to update email")
            }
        }

        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
            Log.e("Settings", "Failed to update email", t)
            onFailure("Failed to update email")
        }
    })
}

private fun updateUserPassword(
    service: ApiService,
    context: Context,
    oldPassword: String,
    newPassword: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    service.updatePassword(TokenAuth.getLogInToken(context), oldPassword, newPassword).enqueue(object : Callback<RegisterResponse> {
        override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
            if (response.isSuccessful) {
                Log.d("Settings", "Password updated")
                onSuccess()
            } else {
                Log.e("Settings", "Failed to update password")
                onFailure("Failed to update password")
            }
        }

        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
            Log.e("Settings", "Failed to update password", t)
            onFailure("Failed to update password")
        }
    })
}

private fun deleteUserAccount(
    service: ApiService,
    context: Context,
    password: String
) {
    service.deleteUser(TokenAuth.getLogInToken(context), password).enqueue(object : Callback<Map<String, String>> {
        override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
            if (response.isSuccessful) {
                Log.d("Settings", "Account deleted")
            } else {
                Log.e("Settings", "Failed to delete account")
            }
        }

        override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
            Log.e("Settings", "Failed to delete account", t)
        }
    })
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
fun ChangeEmailDialog(onDismiss: () -> Unit,
                      context: Context,
                      onUpdateSuccess: () -> Unit = {},
                      onUpdateFailure: (String) -> Unit = {}
) {

    var oldEmail by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Change Email") },
        text = {
            Column {
                Text("Enter your new email:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = oldEmail,
                    onValueChange = { oldEmail = it },
                    label = { Text("Old Email") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.old_email_icon),
                            contentDescription = "Old Email Icon",
                            modifier = Modifier.size(24.dp)

                        )
                    }
                )
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("New Email") }
                )
                OutlinedTextField(
                    value = confirmEmail,
                    onValueChange = { confirmEmail = it },
                    label = { Text("Confirm New Email") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.new_email_icon),
                            contentDescription = "New Email Icon",
                            modifier = Modifier.size(24.dp)

                        )
                    }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.password_lock),
                            contentDescription = "Password Icon",
                            modifier = Modifier.size(24.dp)

                        )
                    }
                )
                if (error != "") {
                    Text(error, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newEmail != confirmEmail) {
                    error = "Emails do not match"
                    return@TextButton
                }
                error = ""
                // Make email change API call, dismiss if successful
                // Otherwise if API call fails, set error message to 'Failed to change email'
                updateUserEmail(RetrofitClient.apiService, context, oldEmail, newEmail, password, {
                    onUpdateSuccess()
                    onDismiss()
                }, {
                    error = it
                    onUpdateFailure(it)
                })
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
fun ChangePasswordDialog(onDismiss: () -> Unit,
                         context: Context,
                         onUpdateSuccess: () -> Unit = {},
                         onUpdateFailure: (String) -> Unit = {}
) {

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Change Password") },
        text = {
            Column {
                Text("Enter your new password:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") }
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") }
                )
                if (error != "") {
                    Text(error, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPassword != confirmPassword) {
                    error = "Passwords do not match"
                    return@TextButton
                }
                error = ""
                // Make password change API call, dismiss if successful
                // Otherwise if API call fails, set error message to 'Failed to change password'
                updateUserPassword(RetrofitClient.apiService, context, oldPassword, confirmPassword, {
                    onUpdateSuccess()
                    onDismiss()
                }, {
                    error = it
                    onUpdateFailure(it)
                })
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
fun ConfirmLogOutDialog(onDismiss: () -> Unit, onLogOutConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Log Out") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(onClick = onLogOutConfirmed) {
                Text("Log Out")
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
fun ConfirmDeleteAccountDialog(onDismiss: () -> Unit,
                               context: Context,
                               onUpdateSuccess: () -> Unit = {},
                               onUpdateFailure: (String) -> Unit = {},
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete Account") },
        text = {
            Column {
                Text("Enter your password twice to confirm account deletion:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") }
                )
                if (error != "") {
                    Text(error, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = ""
                // Make account deletion API call, dismiss if successful
                // Otherwise if API call fails, set error message to 'Failed to delete account'
                deleteUserAccount(RetrofitClient.apiService, context, password)
                onUpdateSuccess()
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
