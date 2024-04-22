package com.ztch.medilens_android_app.Camera

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import com.ztch.medilens_android_app.GradientBackground
import com.ztch.medilens_android_app.GradientOrientation
import com.ztch.medilens_android_app.R
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageViewer(
    onNavigateToHomePage: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToPillViewer: () -> Unit,
    onNavigateToAddMedication: (Any?, Any?, Any?, Any?) -> Unit,
    sharedViewModel: SharedViewModel
){
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
    var selectedPillIndex by remember { mutableStateOf(0) }
    val predictionListSize = prediction.value?.predictions?.size ?: 0

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f // Adjust text size as needed
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "Results",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToCamera() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        },
        containerColor = colorResource(R.color.DarkGrey),
        content = { innerPadding ->

            GradientBackground(
                colors = listOf(colorResource(R.color.black), colorResource(R.color.DarkBlue)),
                orientation = GradientOrientation.Vertical
            )


            Column(modifier = Modifier.fillMaxSize()
                .background(color = Color.Transparent)
                .padding(innerPadding)

            ) {
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
                                    prediction.value?.predictions?.forEachIndexed { index, p ->
                                        val x1 = (p.x1 * scaleX)
                                        val y1 = (p.y1 * scaleY)
                                        val x2 = (p.x2 * scaleX)
                                        val y2 = (p.y2 * scaleY)
                                        // todo fix
                                        if (offset.x in x1..x2 && offset.y in y1..y2) {
                                            var imprint = ""
                                            if (p.ocr != null) {
                                                Log.d("ImageViewer", p.ocr.toString())
                                                Log.d("ImageViewer", p.ocrParsed.toString())
                                                if (p.ocrParsed == null) {
                                                    p.ocrParsed = p.getOCRParsed()
                                                }
                                                if (p.ocrParsed != null) {
                                                    for (ocrDetection in p.ocrParsed!!) {
                                                        imprint += ocrDetection.getText()
                                                    }
                                                }
                                            }
                                            sharedViewModel.currentPillInfo = PillInfo(
                                                imprint = imprint,
                                                color = prediction.value?.predictions?.get(index)?.color ?: "",
                                                shape = prediction.value?.predictions?.get(index)?.shape ?: "",
                                                index = index
                                            )
                                            onNavigateToPillViewer()
                                        }
                                    }
                                }
                            }
                        ) { // Ensure Canvas fills the Box exactly like the Image
                            prediction.value?.predictions?.forEach { p ->
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
                                val index = prediction.value?.predictions?.indexOf(p) ?: 0
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
                        Log.d("ImageViewer", prediction.value?.predictions?.size.toString())

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

                val pagerState = rememberPagerState(pageCount = { prediction.value?.predictions?.size ?: 0 })
                val coroutineScope = rememberCoroutineScope()


                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = { },
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = Color.White,
                            height = 2.dp
                        )
                    }
                ) {
                    prediction.value?.predictions?.forEachIndexed { index,pill ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },//get name
                            text = { Text(pill.name) },
                            modifier = Modifier.background(color = Color.Transparent)
                        )
                    }
                }

                HorizontalPager(state = pagerState, modifier = Modifier.background(color = Color.Transparent)) { page ->
                    prediction.value?.predictions?.getOrNull(page)?.let { pill ->
                        when (page) {
                            // Display characteristics
                            else -> {
                                val confidencePercentage = pill.confidence *100
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    )
                                    {
                                        Text(
                                            text = "Color: ${pill.color}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )

                                        Text(
                                            text = "Shape: ${pill.shape}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White, fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Confidence Level",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White, fontSize = 14.sp
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        Text(
                                            text = String.format("%.2f%%", confidencePercentage),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        LinearProgressIndicator(
                                            progress = { (confidencePercentage.toFloat() * 100) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(8.dp),
                                            color = getColorForConfidence(confidencePercentage.toFloat()),
                                            trackColor = getTrackColorForConfidence(confidencePercentage.toFloat())
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Log.d("ocr", "OCR: ${pill.ocr}")
                                    Log.d("ocrParsed", "OCR: ${pill.ocrParsed}")

                                    Log.d("get", "OCR: ${pill.getOCRParsed()}")



                                    pill.ocr?.let {
                                        pill.getOCRParsed()
                                        pill.ocrParsed?.forEach {
                                            Text(
                                                text = "Detected Text:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White, fontSize = 14.sp
                                            )
                                            Text(
                                                text = it.getText(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White, fontSize = 14.sp
                                            )
                                        }
                                    }


                                    //add medication
                                    Button(
                                        onClick = {
                                            onNavigateToAddMedication(pill.name, pill.color,pill.ocr?.get(0)!![1], pill.shape)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text("Add Medication To Cabinet")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


fun getColorForConfidence(percentage: Float): Color {
    return when {
        percentage <= 50 -> Color.Red
        percentage <= 60 -> Color.Yellow
        percentage <= 75 -> Color.Blue
        else -> Color.Green
    }
}

fun getTrackColorForConfidence(percentage: Float): Color {
    return when {
        percentage <= 25 -> Color.Gray
        percentage <= 50 -> Color.LightGray
        percentage <= 75 -> Color.Gray
        else -> Color.LightGray
    }
}

