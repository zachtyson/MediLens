package com.ztch.medilens_android_app.Authenticate

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.R

@Composable
fun SignUp(onNavigateToHome: () -> Unit,onNavigateToLogin: () -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var legalName by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }




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
            onClick = { onNavigateToHome() },
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.Purple)),
            modifier = Modifier
                .size(150.dp, 50.dp)
                .align(Alignment.End)
        ) {
            Text("Sign Up",color = Color.White)
        }

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


