package com.ztch.medilens_android_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


import com.ztch.medilens_android_app.Camera.*

import com.ztch.medilens_android_app.ui.theme.MedilensandroidappTheme
import com.ztch.medilens_android_app.Authenticate.Login
import com.ztch.medilens_android_app.Authenticate.SignUp
import com.ztch.medilens_android_app.Homepage.HomePage


// camera permissions are
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
            // not a real permission handling, pictures wont be store on the phone
            if(!hasRequiredPermissions()) {
                ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
            }


            MedilensandroidappTheme {
                MyApp(applicationContext)
            }
        }
    }
}

// === Composable Functions ===//

// === `NavController` and `NavHost`====//
@Composable
fun MyApp(applicationContext: Context) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "Login") {
        composable("SignUp") { SignUp(onNavigateToHome = { navController.navigate("Home") },
                onNavigateToLogin = { navController.navigate("Login") } ) }

        composable("Login") { Login(onNavigateToHomePage = { navController.navigate("Home") },
                onNavigateToSignUp = { navController.navigate("SignUp") }) }

        composable("Camera") { CameraXGuideTheme(onNavigateToLogin = { navController.navigate("Login") }, applicationContext = applicationContext) }

        composable("Home") {
            HomePage(onNavigateToCamera = { navController.navigate("Camera") })}
      //  composable ("Home") { Home(onNavigateToLogin = { navController.navigate("Login") }) }
    }

}

