package com.ztch.medilens_android_app

import android.Manifest
import android.app.Application


import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController


import com.ztch.medilens_android_app.Camera.*

import com.ztch.medilens_android_app.ui.theme.MedilensandroidappTheme
import com.ztch.medilens_android_app.Authenticate.*
import com.ztch.medilens_android_app.Homepage.HomePage
import com.ztch.medilens_android_app.Notifications.*

// camera permission
@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : ComponentActivity() {
    private val alarmViewModel: AlarmViewModel by viewModels()
    companion object {
        val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA)
    }
    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Log.d("Initial Boot", "Recomposed")
            // not a real permission handling, pictures won't be store on the phone
            if(!hasRequiredPermissions()) {
                ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
            }

            MedilensandroidappTheme{

                MyApp( viewModel = alarmViewModel)
            }
        }
    }
}

// === Composable Functions ===//

// === `NavController` and `NavHost`====//
@Composable
fun MyApp(viewModel: AlarmViewModel = viewModel()) {

    Log.d("myapp", "Recomposed")
    val navController = rememberNavController()

    NavHost(navController, startDestination = "Login") {
        composable("SignUp") {
            SignUp(
                onNavigateToHome = { navController.navigate("HomePage") {} }
                , onNavigateToLogin = { navController.navigate("Login") })
        }

        composable("Login") {
            Login(
                onNavigateToHomePage = { navController.navigate("Home") {}}
                ,onNavigateToSignUp = { navController.navigate("SignUp") })
        }

        composable("Camera") {
            CameraXGuideTheme(
                onNavigateToHomePage = { navController.navigate("Home") {} })
        }

        composable("Home") {

            HomePage(
                onNavigateToCamera = { navController.navigate("Camera") }
                ,onNavigateToAlarm = { navController.navigate("Alarm") {} },viewModel = viewModel)

        }

        composable("Alarm") {
            notificationScreen(
                onNavigateToHomePage = { navController.navigate("Home")},
                onNavigateToAlarmAdd = { navController.navigate("AlarmAdd") {}},viewModel = viewModel)
        }


        composable("AlarmAdd") {
            AddReminderScreen(
                onNavigateToAlert = { navController.navigate("Alarm") {} }, viewModel = viewModel)
        }

  }

}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}