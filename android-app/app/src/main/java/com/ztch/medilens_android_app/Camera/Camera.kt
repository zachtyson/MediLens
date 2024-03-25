package com.ztch.medilens_android_app.Camera


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.ApiUtils.PredictionResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

data class ImageAndPrediction(var bitmap: Bitmap? = null, var prediction: PredictionResponse? = null, var displayPrediction: Boolean = false,var base64String: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraXGuideTheme(onNavigateToHomePage: () -> Unit, onNavigateToImageViewer: () -> Unit, sharedViewModel: SharedViewModel) {
    val service = RetrofitClient.apiService

    Log.d("camera", "Recomposed")
    val context = LocalContext.current
    if(!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    //fun getImagesAndPredictions(context: Context): List<ImageAndPrediction>? {
    //        val sharedPref = context.getSharedPreferences("medilens", Context.MODE_PRIVATE)
    //        val json = sharedPref.getString("images", null)
    //        return if (json != null) {
    //            val type = object : TypeToken<List<ImageAndPrediction>>() {}.type
    //            Gson().fromJson(json, type)
    //        } else {
    //            null
    //        }
    //    }
    var imagesGet = TokenAuth.getImagesAndPredictions(context)
    val images = remember { mutableStateListOf<ImageAndPrediction>() }
    if (imagesGet != null) {
        images.addAll(imagesGet)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val curImage = ImageAndPrediction(bitmap = bitmap)
                images.add(curImage)
                // Convert bitmap to byte array then convert byte array to multipart body
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

                // Send the image to the server using a POST request and MultiPartBody
                service.uploadImageAndGetPrediction(imagePart).enqueue(object : retrofit2.Callback<PredictionResponse> {
                    override fun onResponse(call: retrofit2.Call<PredictionResponse>, response: retrofit2.Response<PredictionResponse>) {
                        if (response.isSuccessful) {
                            Log.d("Prediction Success", "Prediction: ${response.body()}")
                            curImage.prediction = response.body()
                            // weird hack to trigger recomposition, add to the list again and then remove it
                            val temp = images.last()
                            images.add(temp)
                            images.remove(temp)
                            // save the prediction to the preferences
                            //TokenAuth.saveImagesAndPredictions(context, images)
                        } else {
                            Log.d("Prediction Failure", "Failed to predict")
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<PredictionResponse>, t: Throwable) {
                        Log.d("Prediction Error", t.message ?: "An error occurred")
                    }
                })
            }
        }
    }
    // Rather than preview the camera, use the android camera api and then just display a gallery of the images taken
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            (result.data
                ?.extras?.get("data") as? Bitmap)?.let { bitmap ->
                val curImage = ImageAndPrediction(bitmap = bitmap)
                images.add(curImage)
                // Convert bitmap to byte array then convert byte array to multipart body
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

                // Send the image to the server using a POST request and MultiPartBody
                service.uploadImageAndGetPrediction(imagePart).enqueue(object : retrofit2.Callback<PredictionResponse> {
                    override fun onResponse(call: retrofit2.Call<PredictionResponse>, response: retrofit2.Response<PredictionResponse>) {
                        if (response.isSuccessful) {
                            Log.d("Prediction Success", "Prediction: ${response.body()}")
                            curImage.prediction = response.body()
                            // weird hack to trigger recomposition, add to the list again and then remove it
                            val temp = images.last()
                            images.add(temp)
                            images.remove(temp)

                        } else {
                            Log.d("Prediction Failure", "Failed to predict")
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<PredictionResponse>, t: Throwable) {
                        Log.d("Prediction Error", t.message ?: "An error occurred")
                    }
                })
            }
        }
    }
    // Function to launch gallery
    fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // Function to launch camera
    fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { launchCamera() }) {
                Text("Take Photo")
            }
            Button(onClick = { launchGallery() }) {
                Text("Pick from Gallery")
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(images.size) { image ->
                images[image].bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                // show text if it exists
                                images[image].prediction?.let {
                                    images[image].displayPrediction = !images[image].displayPrediction
                                }
                                // weird hack to trigger recomposition, add to the list again and then remove it
                                val temp = images[image]
                                images.add(temp)
                                images.remove(temp)

                                sharedViewModel.imageAndPrediction = images[image]
                                onNavigateToImageViewer()
                            }
                    )
                }
                // display the prediction if it exists
                if (images[image].displayPrediction) {
                    images[image].prediction?.let {
                        Text(text = it.toString())
                    }
                }
            }
        }
    }


}

