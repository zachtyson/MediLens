package com.ztch.medilens_android_app.Authenticate

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.ApiUtils.RegisterResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import com.ztch.medilens_android_app.ApiUtils.UserRegistrationCredentials
import com.ztch.medilens_android_app.R

@Composable
fun SignUp(onNavigateToHome: () -> Unit,onNavigateToLogin: () -> Unit) {
    Log.d("signup", "Recomposed")
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var legalName by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var successText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.DarkestBlue))
            .padding(16.dp),
    ) {
        signupHeader { onNavigateToLogin() }

        OutlinedTextField(
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            value = legalName,
            onValueChange = { legalName = it },
            label = { Text("Full Name",color = Color.White) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),

            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email",color = Color.White) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),

            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            value = password,
            onValueChange = { password = it },
            label = { Text("Password",color = Color.White) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password",color = Color.White) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                // Call Register API
                val service = RetrofitClient.apiService
                // data class UserRegistrationCredentials(
                //    val email: String,
                //    val password: String,
                //)
                // check if password and confirm password are the same
                if (password != confirmPassword) {
                    Log.d("Register", "Passwords do not match")
                    errorText = "Passwords do not match"
                    return@Button
                }
                if (email.isEmpty() || password.isEmpty() || legalName.isEmpty()) {
                    Log.d("Register", "Empty fields")
                    errorText = "Please fill all fields"
                    return@Button
                }
                else {
                    errorText = ""
                }
                val userToCreate = UserRegistrationCredentials(email, password)
                service.createUser(userToCreate).enqueue(object : retrofit2.Callback<RegisterResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<RegisterResponse>,
                        response: retrofit2.Response<RegisterResponse>
                    ) {
                        if (response.isSuccessful) {
                            val registerResponse = response.body()
                            Log.d("Register", "Response: $registerResponse")
                            successText = "Account created successfully"
                            // Wait for a second before navigating to the login page
                            android.os.Handler().postDelayed({
                                onNavigateToLogin()
                            }, 500)
                            // Navigate to Login page
                            onNavigateToLogin()
                        } else {
                            // if code 409 is returned, it means the email is already in use
                            errorText = if (response.code() == 409) {
                                "Email already in use"
                            } else {
                                "An error occurred"
                            }
                            Log.d("Register", "Error: ${response.errorBody()}")
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<RegisterResponse>, t: Throwable) {
                        Log.d("Register", "Error: $t")
                    }
                })
            },
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.Purple)),
            modifier = Modifier
                .size(150.dp, 50.dp)
                .align(Alignment.CenterHorizontally)
                .testTag("SignUpButton"),
        ) {
            Text("Register")
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

        Spacer(modifier = Modifier.height(250.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ",color = Color.White)
            Text(
                text = "Log In",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}


@Composable
fun signupHeader(onNavigateToLogin: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {


        IconButton(
            onClick = { onNavigateToLogin() }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                tint = Color.White,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create Account",
            color = Color.White,
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),

            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.Start)
        )
    }
}


@Preview(showSystemUi = true)
@Composable
fun signinPreview() {
    SignUp(
        onNavigateToHome = {},
        onNavigateToLogin = {})
}


