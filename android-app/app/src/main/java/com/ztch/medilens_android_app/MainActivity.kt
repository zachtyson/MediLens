package com.ztch.medilens_android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.ztch.medilens_android_app.ui.theme.MedilensandroidappTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedilensandroidappTheme {

                MyApp()
            }
        }
    }
}

// Define the Profile composable.
@Composable
fun Login(onNavigateToHome: () -> Unit) {


    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sign Up",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
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

        Button(
            onClick = { onNavigateToHome() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ")
            Text(
                text = "Log In",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { //nagviagte
                }
            )
        }
    }
}

@Composable
fun Home(onNavigateToLogin: () -> Unit, ) {

    // Set padding for the icon button
    val iconButtonPadding = 16.dp
    // Create a Compose Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = iconButtonPadding)
            .padding(bottom = iconButtonPadding)

    ) {
        IconButton(
            onClick = { onNavigateToLogin() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}



// Define the MyApp composable, including the `NavController` and `NavHost`.
@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "Login") {
        composable("Login") { Login(onNavigateToHome = { navController.navigate("Home") }) }
        composable("Home") { Home(onNavigateToLogin= { navController.navigate("Login") }) }
    }
}