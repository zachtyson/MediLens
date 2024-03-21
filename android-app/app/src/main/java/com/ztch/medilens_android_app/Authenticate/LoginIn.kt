package com.ztch.medilens_android_app.Authenticate


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.ApiUtils.ApiService
import com.ztch.medilens_android_app.ApiUtils.LoginTokenResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient

import com.ztch.medilens_android_app.R

@Composable
fun Login(onNavigateToHomePage: () -> Unit,onNavigateToSignUp: () -> Unit) {
    Log.d("login", "Recomposed")
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.DarkestBlue))
            .padding(16.dp),

    ) {

        Spacer(modifier = Modifier.height(100.dp))
        Text( // repace with an logo
            text = "*MediLens Logo*",
            color = Color.White,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Login",
            color = Color.White,
            style = TextStyle(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 16.dp)

        )
        Text(
            text = "Please Sign in To Continue ",
            color = Color.White,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedContainerColor = colorResource(id = R.color.DarkBlue),
            ),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )

        OutlinedTextField(
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                focusedContainerColor = colorResource(id = R.color.DarkBlue),
            ),
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.White)},
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
                // Call login API
                val service = RetrofitClient.apiService
                service.loginUser(email, password).enqueue(object : retrofit2.Callback<LoginTokenResponse> {
                    override fun onResponse(call: retrofit2.Call<LoginTokenResponse>, response: retrofit2.Response<LoginTokenResponse>) {
                        if (response.isSuccessful) {
                            Log.d("Login Success", "Token: ${response.body()?.access_token}")
                            // Navigate to home page or save the token as needed
                            onNavigateToHomePage()
                        } else {
                            Log.d("Login Failure", "Incorrect email or password")
                            // display on the UI

                        }
                    }

                    override fun onFailure(call: retrofit2.Call<LoginTokenResponse>, t: Throwable) {
                        Log.d("Login Error", t.message ?: "An error occurred")
                    }
                })

            },
            colors = buttonColors(colorResource(id = R.color.Purple)),
            modifier = Modifier
                .size(150.dp, 50.dp)
                .align(Alignment.End),
        ) {
            Text("Login")
        }


        Spacer(modifier = Modifier.height(165.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", color = Color.White)
            Text(
                text = "Sign Up",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun loginPreview() {
    Login(
        onNavigateToHomePage= {},
        onNavigateToSignUp = {}
    )
}
