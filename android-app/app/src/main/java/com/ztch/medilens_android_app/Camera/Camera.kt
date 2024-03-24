package com.ztch.medilens_android_app.Camera


import android.annotation.SuppressLint
import android.content.Context

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.ztch.medilens_android_app.ApiUtils.LoginAuth
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.ztch.medilens_android_app.ApiUtils.ApiService
import com.ztch.medilens_android_app.ApiUtils.PredictionResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraXGuideTheme(onNavigateToHomePage: () -> Unit,) {
    Log.d("camera", "Recomposed")
    val context = LocalContext.current
    if(!LoginAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE
            )
        }
    }
    val viewModel = viewModel<MainViewModel>()
    val bitmaps by viewModel.bitmaps.collectAsState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            PhotoGalleryContents(
                bitmaps = bitmaps,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CameraPreview(
                controller = controller,
                modifier = Modifier
                    .fillMaxSize()
            )

                IconButton(
                    onClick = { onNavigateToHomePage() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        tint = Color.White,
                        contentDescription = "Back"
                    )
                }




            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.LightGray)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Open gallery"
                    )
                }

                IconButton(
                    onClick = {
                        takePhoto(controller = controller,
                            onPhotoTaken = viewModel::onTakePhoto,
                            applicationContext = context)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take photo"
                    )
                }
            }
        }
    }
}


@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}



@SuppressLint("CoroutineCreationDuringComposition")

fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    applicationContext: Context
) {
    try {
        val image = controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )
                    Toast.makeText(applicationContext, "Photo taken!", Toast.LENGTH_SHORT).show()
                    onPhotoTaken(rotatedBitmap)
                    // Call the non-composable function for backend handling
                    val service = RetrofitClient.apiService
                    // Convert bitmap to byte array then convert byte array to multipart body
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

                    // Send the image to the server using a POST request and MultiPartBody
                    service.uploadImageAndGetPrediction(imagePart).enqueue(object : retrofit2.Callback<PredictionResponse> {
                        override fun onResponse(call: retrofit2.Call<PredictionResponse>, response: retrofit2.Response<PredictionResponse>) {
                            if (response.isSuccessful) {
                                Log.d("Prediction Success", "Prediction: ${response.body()}")
                            } else {
                                Log.d("Prediction Failure", "Failed to predict")
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<PredictionResponse>, t: Throwable) {
                            Log.d("Prediction Error", t.message ?: "An error occurred")
                        }
                    })
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                }
            }
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

