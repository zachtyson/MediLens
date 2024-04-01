package com.ztch.medilens_android_app.Camera

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ztch.medilens_android_app.ApiUtils.PillInfoResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import com.ztch.medilens_android_app.ApiUtils.TokenAuth

//PillViewer(
//                onNavigateToHomePage = { navController.navigate("Home") {} },
//                onNavigateToCamera = { navController.navigate("Camera") },
//                onNavigateToImageViewer = { navController.navigate("ImageViewer") {} },
//                sharedViewModel = sharedCameraImageViewerModel
//            )
@Composable
fun PillViewer(
    onNavigateToHomePage: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToImageViewer: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    // Display an individual pill's information
    // Display the image of the pill by getting the image from the sharedViewModel's imageAndPrediction
    // Crop to the pill's bounding box using the sharedViewModel's currentPillInfo which has the index value

    // Immediately use the pillFromImprintDemo to display the pill's information,
    // but allow the user to modify the pill's information and re-submit the pill's information
    // since the AI model isn't perfect and the user may want to correct the pill's information

    val service = RetrofitClient.apiService
    Log.d("imageviewer", "Recomposed")
    val context = LocalContext.current
    if (!TokenAuth.isLoggedIn(context)) {
        // if user is not logged in, navigate to home page, which will redirect to login page
        onNavigateToHomePage()
    }
    // Display the image overlay the prediction on the image
    if (sharedViewModel.imageAndPrediction == null) {
        onNavigateToCamera()
    }
    if (sharedViewModel.currentPillInfo == null) {
        onNavigateToImageViewer()
    }

    val imageAndPrediction = sharedViewModel.imageAndPrediction!!
    val bitmap = imageAndPrediction.bitmap!!
    if (imageAndPrediction.prediction == null) {
        onNavigateToCamera()
    }
    if(imageAndPrediction.prediction == null) {
        onNavigateToCamera()
    }
    val prediction = imageAndPrediction.prediction!!
    val displayPrediction = imageAndPrediction.displayPrediction

    val originalHeightOfImage = bitmap.height
    val originalWidthOfImage = bitmap.width
    val heightOfImageDp = 300.dp
    val heightOfImage = with(LocalDensity.current) { heightOfImageDp.toPx() }
    var widthOfImagePx = remember { mutableStateOf(0) }

    val textPaint = remember {
        Paint().apply {
            color = Color.WHITE
            textSize = 40f // Adjust text size as needed
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    // Display current pill's information
    val currentPillInfo = sharedViewModel.currentPillInfo!!
    val imprint = currentPillInfo.imprint
    val color = currentPillInfo.color
    val shape = currentPillInfo.shape


    val imprintState = remember { mutableStateOf(imprint) }
    // colorState is an enum of ColorValuesDC
    val initialColor: ColorValuesDC = try {
        ColorValuesDC.valueOf(color)
    } catch (e: IllegalArgumentException) {
        ColorValuesDC.ANY_COLOR
    }
    val initialShape: ShapeValuesDC = try {
        ShapeValuesDC.valueOf(shape)
    } catch (e: IllegalArgumentException) {
        ShapeValuesDC.ANY_SHAPE
    }

    val colorState = remember { mutableStateOf(initialColor) }
    val shapeState = remember { mutableStateOf(initialShape) }
    val expanded = remember { mutableStateOf(false) }

    val pillInfo = remember { mutableStateOf(emptyList<PillInfoResponse>()) }
    LaunchedEffect(Unit) {
        service.pillFromImprintDemo(imprint, colorState.value.ordinal, shapeState.value.ordinal).enqueue(object : retrofit2.Callback<List<PillInfoResponse>> {
            override fun onResponse(call: retrofit2.Call<List<PillInfoResponse>>, response: retrofit2.Response<List<PillInfoResponse>>) {
                if (response.isSuccessful) {
                    Log.d("PillInfo Success", "PillInfo: ${response.body()}")
                    pillInfo.value = response.body()!!

                } else {
                    Log.d("PillInfo Failure", "Failed to get pill info")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<PillInfoResponse>>, t: Throwable) {
                Log.d("PillInfo Error", t.message ?: "An error occurred")
            }
        })
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // Display the pill's information
        LazyColumn {
            item {
                TextField(
                    value = imprintState.value,
                    onValueChange = { imprintState.value = it },
                    label = { Text("Imprint") }
                )
                TextField(
                    value = colorState.value.toString().replace('_', ' '),
                    onValueChange = {},
                    readOnly = true, // Make TextField read-only
                    label = { Text("Color") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, "Dropdown", Modifier.clickable { expanded.value = true })
                    }
                )
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    ColorValuesDC.values().forEach { color ->
                        DropdownMenuItem(
                            text = { Text(color.name.replace('_', ' ')) },
                            onClick = {
                                colorState.value = color
                                expanded.value = false
                            }
                        )
                    }
                }
                TextField(
                    value = shapeState.value.toString().replace('_', ' '),
                    onValueChange = {},
                    readOnly = true, // Make TextField read-only
                    label = { Text("Shape") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, "Dropdown", Modifier.clickable { expanded.value = true })
                    }
                )
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    ShapeValuesDC.values().forEach { shape ->
                        DropdownMenuItem(
                            text = { Text(shape.name.replace('_', ' ')) },
                            onClick = {
                                shapeState.value = shape
                                expanded.value = false
                            }
                        )
                    }
                }
                Button(onClick = {
                    service.pillFromImprintDemo(imprintState.value, colorState.value.ordinal, shapeState.value.ordinal).enqueue(object : retrofit2.Callback<List<PillInfoResponse>> {
                        override fun onResponse(call: retrofit2.Call<List<PillInfoResponse>>, response: retrofit2.Response<List<PillInfoResponse>>) {
                            if (response.isSuccessful) {
                                Log.d("PillInfo Success", "PillInfo: ${response.body()}")
                                pillInfo.value = response.body()!!

                            } else {
                                Log.d("PillInfo Failure", "Failed to get pill info")
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<List<PillInfoResponse>>, t: Throwable) {
                            Log.d("PillInfo Error", t.message ?: "An error occurred")
                        }
                    })
                }) {
                    Text("Submit")
                }
                // Column for each pill's info
                Column {
                    pillInfo.value.forEach {
                        Text("Imprint: ${it.imprint}")
                        Text("Color: ${it.color}")
                        Text("Shape: ${it.shape}")
                        // Fetch the pill's picture from the image URL
                        val imageURL = it.imageURL
                        Image(
                            painter = rememberImagePainter(imageURL),
                            contentDescription = "Pill Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                }

            }
        }
    }

}
