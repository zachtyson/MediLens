package com.ztch.medilens_android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    Button(onClick = { onNavigateToHome()}) {
        Text("Go to HomePage")
    }
}

@Composable
fun Home(onNavigateToLogin: () -> Unit, ) {

    // Use LocalDensity to convert dp to pixels
    val density = LocalDensity.current.density

    // Set padding for the icon button
    val iconButtonPadding = 16.dp

    // Calculate padding in pixels
    val iconButtonPaddingPx = with(LocalDensity.current) { iconButtonPadding.roundToPx() }

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