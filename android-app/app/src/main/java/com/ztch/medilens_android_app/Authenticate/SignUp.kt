package com.ztch.medilens_android_app.Authenticate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUp(onNavigateToHome: () -> Unit,onNavigateToLogin: () -> Unit,) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var legalName by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        signupHeader { onNavigateToLogin() }

        OutlinedTextField(
            value = legalName,
            onValueChange = { legalName = it },
            label = { Text("Full Name") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),

            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),

            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
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
            modifier = Modifier
                .size(150.dp, 50.dp)
                .align(Alignment.End)
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(250.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ")
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
fun signupHeader(onNavigateToLogin: () -> Unit,) {

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {


        IconButton(
            onClick = { onNavigateToLogin() }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create Account",
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

}


