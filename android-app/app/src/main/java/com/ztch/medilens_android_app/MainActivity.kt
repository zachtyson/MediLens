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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ztch.medilens_android_app.ApiUtils.TokenAuth

import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


import com.ztch.medilens_android_app.Camera.*

import com.ztch.medilens_android_app.ui.theme.MedilensandroidappTheme
import com.ztch.medilens_android_app.Authenticate.*
import com.ztch.medilens_android_app.Homepage.HomePage
import com.ztch.medilens_android_app.Medicard.MediCardScreen

import com.ztch.medilens_android_app.Notifications.*
import com.ztch.medilens_android_app.Refill.*
import com.ztch.medilens_android_app.Settings.DoctorScreen
import com.ztch.medilens_android_app.Settings.Settings

// camera permission
@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : ComponentActivity() {
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

                MyApp()
            }
        }
    }
}

// === Composable Functions ===//

// === `NavController` and `NavHost`====//
@Composable
fun MyApp() {

    Log.d("myapp", "Recomposed")
    val navController = rememberNavController()

    val context = LocalContext.current
    val sharedCameraImageViewerModel: SharedViewModel = SharedViewModel()
    val sharedMedicationModel: SharedMedicationModel = SharedMedicationModel()
    val alarmViewModel: AlarmViewModel = viewModel()
    val startDestination = if (TokenAuth.isLoggedIn(context)) "Home" else "Login"
    val userMedicationViewModel: UserMedicationViewModel = UserMedicationViewModel()

    NavHost(navController, startDestination = startDestination) {

        composable("SignUp") {
            SignUp(
                onNavigateToHome = { navController.navigate("Home") {} },
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
                onNavigateToCabinet = {
                    sharedMedicationModel.userIsScheduling = false
                    navController.navigate("Cabinet")
                                      },
                onNavigateToSettings = { navController.navigate("Settings") {} },
                onNavigateToMediCard = { navController.navigate("MediCard") {} },
                alarmViewModel = alarmViewModel
            )

        }

        composable("Alarm") {
            notificationScreen(
                onNavigateToHomePage = { navController.navigate("Home") },
                onNavigateToPillInformation = { navController.navigate("PillInformation") {} },
                onNavigateToUnscheduledMedications = {
                    sharedMedicationModel.userIsScheduling = true
                    navController.navigate("Cabinet")
                },
                sharedMedicationModel = sharedMedicationModel
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
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToDoctor = { navController.navigate("Doctor") {} }
            )
        }

        composable("Doctor") {
            DoctorScreen(
                onNavigateToSettings = { navController.navigate("Settings") {} },
                onNavigateToLogin = { navController.navigate("Login") }
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
//            AlarmTimesScreen(
//                mediName = backStackEntry.arguments?.getString("mediName") ?: "",
//                dose = backStackEntry.arguments?.getString("dose") ?: "",
//                strength = backStackEntry.arguments?.getString("strength") ?: "",
//                RX = backStackEntry.arguments?.getString("RX") ?: "",
//                form = backStackEntry.arguments?.getString("form") ?: "",
//                onNavigateBack = { navController.popBackStack() },
//                alarmViewModel = viewModel,
//                onNavigateToAlarm = { navController.navigate("Alarm") {} }
//            )
        }


        composable("Cabinet") {
            Cabinet(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToAddMedication = { navController.navigate("AddMedication") {} },
                onNavigateToModifyMedication = { navController.navigate("ModifyMedication") {} },
                onNavigateToScheduleMedication = { navController.navigate("ScheduleMedication") {} },
                sharedMedicationModel = sharedMedicationModel,
                userMedicationViewModel = userMedicationViewModel

            )
        }

        composable("ScheduleMedication") {
            ScheduleMedication(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToCabinet = { navController.navigate("Cabinet") {} },
                sharedMedicationModel = sharedMedicationModel
            )
        }

        composable("MediCard") {
            MediCardScreen(
                onNavigateToHomePage = { navController.navigate("Home") {} },
            )
        }

        composable("AddMedication") {
            AddMedication(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToCabinet = { navController.navigate("Cabinet") {} },
                userMedicationViewModel = userMedicationViewModel
            )
        }

        composable("ModifyMedication") {
            ModifyMedication(
                onNavigateToHomePage = { navController.navigate("Home") {} },
                onNavigateToAlarm = { navController.navigate("Alarm") {} },
                onNavigateToCabinet = { navController.navigate("Cabinet") {} },
                sharedMedicationModel = sharedMedicationModel
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
