package com.ztch.medilens_android_app

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.ztch.medilens_android_app.ui.theme.MedilensandroidappTheme
import com.ztch.medilens_android_app.Authenticate.Login
import com.ztch.medilens_android_app.Authenticate.SignUp


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

// === Composable Functions ===//

@Composable
fun Home(onNavigateToLogin: () -> Unit, onNavigateToSignUp: () -> Unit, ) {

    // Set padding for the icon button
    val iconButtonPadding = 16.dp

    Column( // root container
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ){

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,

        ) {

            IconButton(
                onClick = { onNavigateToLogin() },

            ) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { onNavigateToSignUp() },

            ){
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "Person",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// === `NavController` and `NavHost`====//
@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "SignUp") {
        composable("SignUp") { SignUp(onNavigateToHome = { navController.navigate("Home") },
                onNavigateToLogin = { navController.navigate("Login") } ) }

        composable("Login") { Login(onNavigateToHome = { navController.navigate("Home") },
                onNavigateToSignUp = { navController.navigate("SignUp") }) }

        composable("Home") { Home(onNavigateToLogin= { navController.navigate("Login")},
                onNavigateToSignUp = { navController.navigate("SignUp") }) }

    }
}