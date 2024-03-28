package com.ztch.medilens_android_app.Camera

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.PredictionResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import com.ztch.medilens_android_app.ApiUtils.TokenAuth

@Composable
fun ImageViewer(onNavigateToHomePage: () -> Unit, onNavigateToCamera: () -> Unit, onNavigateToPillViewer: () -> Unit, sharedViewModel: SharedViewModel) {
    //class SharedViewModel: ViewModel() {
    //    var imageAndPrediction: ImageAndPrediction? = null
    //}
    //data class ImageAndPrediction(var bitmap: Bitmap? = null, var prediction: PredictionResponse? = null, var displayPrediction: Boolean = false, var base64String: String? = null)
    //

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
    // base64 string likely won't exist

    val originalHeightOfImage = bitmap.height
    val originalWidthOfImage = bitmap.width
    val heightOfImageDp = 300.dp
    val heightOfImage = with(LocalDensity.current) { heightOfImageDp.toPx() }
    var widthOfImagePx = remember { mutableStateOf(0) }

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f // Adjust text size as needed
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        // Display the image
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(heightOfImageDp)
            .onGloballyPositioned { layoutCoordinates ->
                widthOfImagePx.value = layoutCoordinates.size.width
            }
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.matchParentSize()
            )

            if (widthOfImagePx.value > 0) { // Ensure we have a non-zero width before drawing
                val scaleX = widthOfImagePx.value.toFloat() / originalWidthOfImage
                val scaleY = heightOfImage / originalHeightOfImage

                Canvas(modifier = Modifier.matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            prediction.predictions.forEachIndexed { index, p ->
                                val x1 = (p.x1 * scaleX)
                                val y1 = (p.y1 * scaleY)
                                val x2 = (p.x2 * scaleX)
                                val y2 = (p.y2 * scaleY)
                                // todo fix
                                if (offset.x in x1..x2 && offset.y in y1..y2) {
                                    var imprint = ""
                                    if (p.ocr != null) {
                                        Log.d("ImageViewer", p.ocrParsed.toString())
                                        for(ocr in p.ocrParsed!!) {
                                            imprint += ocr.text
                                        }
                                        imprint = p.ocrParsed?.get(0)?.text ?: ""
                                    }
                                    sharedViewModel.currentPillInfo = PillInfo(
                                        imprint = imprint,
                                        color = prediction.predictions[index].color ?: "",
                                        shape = prediction.predictions[index].shape ?: "",
                                        index = index
                                    )
                                    onNavigateToPillViewer()
                                }
                            }
                        }
                    }
                ) { // Ensure Canvas fills the Box exactly like the Image
                    prediction.predictions.forEach { p ->
                        val x1 = (p.x1 * scaleX)
                        val y1 = (p.y1 * scaleY)
                        val x2 = (p.x2 * scaleX)
                        val y2 = (p.y2 * scaleY)

                        drawRect(
                            color = Color.Red.copy(alpha = 0.3f),
                            topLeft = Offset(x1.toFloat(), y1.toFloat()),
                            size = Size((x2 - x1).toFloat(), (y2 - y1).toFloat())
                        )
                        // Draw text that just says the index of the pill
                        val index = prediction.predictions.indexOf(p) + 1
                        drawContext.canvas.nativeCanvas.drawText(
                            "$index",
                            x1.toFloat(),
                            (y1 + textPaint.textSize).toFloat(), // Adjust to position the text correctly within the box
                            textPaint
                        )
                        if (p.ocr != null) {
                            // todo: draw text saying the contents of the OCR
                        }
                    }
                }
                Log.d("ImageViewer", prediction.toString())
                Log.d("ImageViewer", prediction.predictions.size.toString())

            }
            // Iterate over all predictions and display them
            //data class Prediction(z
            //    val x1: Double,
            //    val y1: Double,
            //    val x2: Double,
            //    val y2: Double,
            //    val confidence: Double,
            //    val classId: Int,
            //    val name: String,
            //    val color: String?,
            //    val shape: String?,
            //    val ocr: List<List<Any>>?
            //)
            //
            //data class PredictionResponse(
            //    val predictions: List<Prediction>
            //)
        }

        LazyColumn {
            items(prediction.predictions.size) { p ->
                Text(
                    // Text is 'Pill' + index
                    text = "Pill ${p + 1}",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Confidence: ${prediction.predictions[p].confidence}",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Bounding Box: (${prediction.predictions[p].x1}, ${prediction.predictions[p].y1}) to (${prediction.predictions[p].x2}, ${prediction.predictions[p].y2})",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Color: ${prediction.predictions[p].color}",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Shape: ${prediction.predictions[p].shape}",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall
                )
                if (prediction.predictions[p].ocr != null) {
                    Text(
                        text = "OCR: ${prediction.predictions[p].ocr.toString()}",
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

    }
}
