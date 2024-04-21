package com.ztch.medilens_android_app.Authenticate


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.ApiUtils.LoginTokenResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.R

@Composable
fun Login(onNavigateToHomePage: () -> Unit,onNavigateToSignUp: () -> Unit) {
    val context = LocalContext.current
    Log.d("login", "Recomposed")
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.DarkestBlue))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.DarkestBlue)),
            horizontalAlignment = Alignment.CenterHorizontally
            ) {

            // logo is medilens_logo.svg

            // place logo at small size
            Image(
                painter = painterResource(id = R.drawable.medilens_logo),
                contentDescription = "MediLens Logo",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Login",
                color = Color.White,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 5.dp)
                    .align(Alignment.CenterHorizontally),

            )
            Text(
                text = "Please Sign in To Continue ",
                color = Color.White,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 5.dp)
                    .align(Alignment.CenterHorizontally),
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
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.email_logo),
                        contentDescription = "Email Icon",
                        modifier = Modifier.size(24.dp)

                    )
                }
            )

            OutlinedTextField(
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.DarkBlue),
                    focusedContainerColor = colorResource(id = R.color.DarkBlue),
                ),
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password
                ),
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.password_lock),
                        contentDescription = "Password Icon",
                        modifier = Modifier.size(24.dp)

                    )
                }
            )

            Button(
                onClick = {
                    // Call login API
                    val service = RetrofitClient.apiService
                    service.loginUser(email, password).enqueue(object : retrofit2.Callback<LoginTokenResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<LoginTokenResponse>,
                            response: retrofit2.Response<LoginTokenResponse>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("Login Success", "Token: ${response.body()?.access_token}")
                                // Save the token and try to navigate to the home page
                                val token = response.body()?.access_token
                                if (token == null) {
                                    Log.d("Login Error", "Token is null")
                                    return
                                }
                                // get id from response
                                // jwt contains " sub = {"email": user.email, "id": user.id}"
                                // decode jwt using base64 and get id
                                val id = getUserIdFromJwt(token)
                                hashAndStorePassword(context, password, id)

                                if (TokenAuth.logIn(context, token)) {
                                    onNavigateToHomePage()
                                } else {
                                    Log.d("Login Error", "Failed to save token")
                                }

                                // After password to hash and use as encryption key


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
                    .align(Alignment.CenterHorizontally)
                    .testTag("LoginButton")
            ) {
                Text("Login")
            }

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
}


@Preview(showSystemUi = true)
@Composable
fun loginPreview() {
    Login(
        onNavigateToHomePage= {},
        onNavigateToSignUp = {}
    )
}
