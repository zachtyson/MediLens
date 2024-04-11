package com.ztch.medilens_android_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


import com.ztch.medilens_android_app.Camera.*

import com.ztch.medilens_android_app.ui.theme.MedilensandroidappTheme
import com.ztch.medilens_android_app.Authenticate.*
import com.ztch.medilens_android_app.Homepage.HomePage
import com.ztch.medilens_android_app.Notifications.*
import com.ztch.medilens_android_app.Refill.AddMedication
import com.ztch.medilens_android_app.Refill.Cabinet
import com.ztch.medilens_android_app.Settings.Settings

// camera permission
@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : ComponentActivity() {
    private val alarmViewModel: AlarmViewModel by viewModels()
    companion object {
        val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA)
    }
    fun hasRequiredPermissions(): Boolean {
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

    val sharedCameraImageViewerModel: SharedViewModel = SharedViewModel()

    NavHost(navController, startDestination = "Login") {

        composable("SignUp") {
            SignUp(
                onNavigateToHome = { navController.navigate("HomePage") {} },
                onNavigateToLogin = { navController.navigate("Login") })
        }

        composable("Login") {
            Login(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToSignUp = { navController.navigate("SignUp") })
        }

        composable("Home") {

            HomePage(
                onNavigateToCamera = { navController.navigate("Camera") },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToLogin = { navController.navigate("Login") {} },
                onNavigateToCabinet = { navController.navigate("Cabinet") {} },
                onNavigateToSettings = { navController.navigate("Settings") {} },
                viewModel = viewModel
            )

        }

        composable("Alarm") {
            notificationScreen(
                onNavigateToHomePage = { navController.navigate("Home") },
                onNavigateToPillInformation = { navController.navigate("PillInformation") {} }, viewModel = viewModel
            )
        }

        composable("Camera") {
            CameraXGuideTheme(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToImageViewer = { navController.navigate("ImageViewer") {} },
                sharedViewModel = sharedCameraImageViewerModel
            )
        }

        composable("ImageViewer") {
            ImageViewer(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToCamera = { navController.navigate("Camera") },
                onNavigateToPillViewer = { navController.navigate("PillViewer") },
                sharedViewModel = sharedCameraImageViewerModel
            )
        }

        composable("PillViewer") {
            PillViewer(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToCamera = { navController.navigate("Camera") },
                onNavigateToImageViewer = { navController.navigate("ImageViewer") {} },
                sharedViewModel = sharedCameraImageViewerModel
            )
        }

        composable("Settings") {
            Settings(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToAlarm = { navController.navigate("Alarm") {} }
            )
        }



        composable("PillInformation") {
            PillInformationScreen(
                onNavigateToAlarmTimes = { mediName, dose, strength, RX, form ->
                    navController.navigate("AlarmTimes/$mediName/$dose/$strength/$RX/$form")
                },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
            )
        }

        composable(
            route = "AlarmTimes/{mediName}/{dose}/{strength}/{RX}/{form}",

            arguments = listOf(
                navArgument("mediName") { type = NavType.StringType; defaultValue = "" },
                navArgument("dose") { type = NavType.StringType; defaultValue = "" },
                navArgument("strength") { type = NavType.StringType; nullable = true; defaultValue = "" },
                navArgument("RX") { type = NavType.StringType; nullable = true; defaultValue = "" },
                navArgument("form") { type = NavType.StringType; nullable = true; defaultValue = "" }
            )
        ) { backStackEntry ->
            AlarmTimesScreen(
                mediName = backStackEntry.arguments?.getString("mediName") ?: "",
                dose = backStackEntry.arguments?.getString("dose") ?: "",
                strength = backStackEntry.arguments?.getString("strength") ?: "",
                RX = backStackEntry.arguments?.getString("RX") ?: "",
                form = backStackEntry.arguments?.getString("form") ?: "",
                onNavigateBack = { navController.popBackStack() },
                alarmViewModel = viewModel,
                onNavigateToAlarm = { navController.navigate("Alarm") {} }
            )
        }
        composable("Cabinet") {
            Cabinet(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToAddMedication = { navController.navigate("AddMedication") {} }
            )
        }

        composable("AddMedication") {
            AddMedication(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToCabinet = { navController.navigate("Cabinet") {} }
            )
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
