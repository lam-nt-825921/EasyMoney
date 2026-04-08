package com.example.easymoney.ui.loan.information.ekyc

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.easymoney.domain.model.FaceDetectionReason
import com.example.easymoney.domain.model.FaceDetectionResult
import com.example.easymoney.domain.model.FaceInfo
import com.example.easymoney.domain.model.FaceBoundingBox
import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.ui.components.AppTopBarOverride
import com.example.easymoney.ui.components.RegisterTopBarOverride
import com.example.easymoney.ui.components.TopBarMode
import com.example.easymoney.ui.theme.EkycColors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import com.example.easymoney.ui.common.error.AppErrorScreen

@Composable
fun EkycFaceCaptureScreen(
    onBackToIntro: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToError: (String) -> Unit,
    viewModel: EkycCameraViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    RegisterTopBarOverride(
        ownerRoute = AppDestination.LoanFlow.route,
        override = AppTopBarOverride(topBarMode = TopBarMode.HIDDEN)
    )

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onEvent(EkycUiEvent.OnPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is EkycUiEffect.NavigateToNextStep -> onSuccess()
                is EkycUiEffect.NavigateBack -> onBackToIntro()
                is EkycUiEffect.ShowError -> onNavigateToError(effect.message)
                is EkycUiEffect.OpenSettings -> {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    })
                }
                else -> {}
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(EkycUiEvent.OnScreenEnter)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EkycColors.cameraSceneBackground)
    ) {
        IconButton(
            onClick = { viewModel.onEvent(EkycUiEvent.OnBackClick) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = EkycColors.cameraIconColor
            )
        }

        when (uiState.permissionState) {
            PermissionState.Granted -> {
                if (uiState.uploadState == UploadState.Error) {
                    AppErrorScreen(
                        title = "Xác thực không thành công",
                        message = uiState.errorMessage ?: "Lỗi xác thực khuôn mặt. Vui lòng thử lại.",
                        buttonText = "Chụp lại ngay",
                        onButtonClick = { viewModel.onEvent(EkycUiEvent.OnRetakeClick) },
                        secondaryButtonText = "Quay lại hướng dẫn",
                        onSecondaryButtonClick = onBackToIntro,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CameraReadyContent(viewModel = viewModel)
                }
            }
            PermissionState.PermanentlyDenied -> PermissionBlockedContent(
                onOpenSettings = { viewModel.onEvent(EkycUiEvent.OnOpenSettings) },
                onBack = onBackToIntro
            )
            PermissionState.NotAsked,
            PermissionState.Denied -> PermissionRequestContent(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onBack = onBackToIntro
            )
        }

        // Uploading Overlay
        if (uiState.uploadState == UploadState.Uploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Đang xử lý khuôn mặt...", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CameraReadyContent(viewModel: EkycCameraViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EkycColors.cameraSceneBackground)
    ) {
        EkycCameraPreview(
            modifier = Modifier.fillMaxSize(),
            onImageCaptureReady = { imageCapture = it },
            onFrameQualityChanged = { ready, message, result ->
                viewModel.onFrameAnalyzed(result)
            },
            triggerCapture = uiState.cameraState == CameraState.Capturing,
            onImageCaptured = { file ->
                viewModel.onImageCaptured(file)
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chụp ảnh chân dung",
                color = EkycColors.cameraTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            EkycDashedGuideFrame(isReady = uiState.faceDetectionResult?.canCapture == true)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.precheckMessage ?: "Căn chỉnh khung hình để cho phép chụp",
                color = if (uiState.faceDetectionResult?.canCapture == true) EkycColors.cameraSuccessText else EkycColors.cameraTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.onEvent(EkycUiEvent.OnCaptureClick) },
                enabled = uiState.faceDetectionResult?.canCapture == true && uiState.uploadState == UploadState.Idle,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EkycColors.cameraButtonBackground,
                    contentColor = EkycColors.cameraButtonText,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color(0xFF3D3D3D)
                ),
                modifier = Modifier.size(72.dp)
            ) {
                Text("●", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun EkycCameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onFrameQualityChanged: (ready: Boolean, message: String, result: FaceDetectionResult) -> Unit,
    triggerCapture: Boolean = false,
    onImageCaptured: (File) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    // Handle triggerCapture
    LaunchedEffect(triggerCapture) {
        if (triggerCapture && imageCaptureRef != null) {
            takePhoto(
                context = context,
                imageCapture = imageCaptureRef,
                onCaptureResult = { file, success ->
                    if (success && file != null) {
                        onImageCaptured(file)
                    }
                }
            )
        }
    }
    
    // Initialize detector once and remember it
    val detector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        )
    }

    DisposableEffect(lifecycleOwner, previewView) {
        bindCameraUseCases(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            analyzerExecutor = analyzerExecutor,
            detector = detector,
            onImageCaptureReady = { 
                imageCaptureRef = it
                onImageCaptureReady(it) 
            },
            onFrameQualityChanged = onFrameQualityChanged
        )

        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
            analyzerExecutor.shutdown()
            detector.close()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
private fun EkycDashedGuideFrame(isReady: Boolean) {
    val borderColor = if (isReady) EkycColors.cameraSuccessText else EkycColors.cameraFrameGuide

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        // Portrait oval frame (taller than wide - matches face proportion)
        val ovalWidth = size.width * 0.75f
        val ovalHeight = size.height * 0.95f
        val offsetX = (size.width - ovalWidth) / 2
        val offsetY = (size.height - ovalHeight) / 2

        // Filled background
        drawOval(
            color = EkycColors.cameraFrameGuideFill,
            topLeft = Offset(offsetX, offsetY),
            size = androidx.compose.ui.geometry.Size(ovalWidth, ovalHeight)
        )

        // Dashed border - changes to green when ready
        drawOval(
            color = borderColor,
            topLeft = Offset(offsetX, offsetY),
            size = androidx.compose.ui.geometry.Size(ovalWidth, ovalHeight),
            style = Stroke(
                width = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 18f), 0f)
            )
        )
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ung dung can truy cap may anh",
            color = EkycColors.permissionText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Vui long cap quyen camera de bat dau chup anh chan dung.",
            color = EkycColors.cameraTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EkycColors.permissionButtonBackground,
                contentColor = EkycColors.permissionButtonText
            )
        ) {
            Text("Cho phep truy cap")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = EkycColors.permissionText
            )
        ) {
            Text("Quay lai")
        }
    }
}

@Composable
private fun PermissionBlockedContent(
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera dang bi chan",
            color = EkycColors.permissionText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Vui long mo Cai dat va cap quyen Camera cho ung dung.",
            color = EkycColors.cameraTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EkycColors.permissionButtonBackground,
                contentColor = EkycColors.permissionButtonText
            )
        ) {
            Text("Mo Cai dat")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = EkycColors.permissionText
            )
        ) {
            Text("Quay lai")
        }
    }
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    analyzerExecutor: ExecutorService,
    detector: com.google.mlkit.vision.face.FaceDetector,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onFrameQualityChanged: (ready: Boolean, message: String, result: FaceDetectionResult) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                    performPrecheckAnalysis(imageProxy, detector, onFrameQualityChanged)
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("EkycFaceCapture", "bindToLifecycle error: ${e.message}", e)
        }

        onImageCaptureReady(imageCapture)
    }, ContextCompat.getMainExecutor(context))
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onCaptureResult: (file: File?, success: Boolean) -> Unit
) {
    val capture = imageCapture ?: run {
        onCaptureResult(null, false)
        return
    }

    val outputFile = File(
        context.cacheDir,
        "ekyc_face_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    capture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onCaptureResult(outputFile, true)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("EkycFaceCapture", "Capture error", exception)
                onCaptureResult(null, false)
            }
        }
    )
}

@OptIn(ExperimentalGetImage::class)
private fun performPrecheckAnalysis(
    imageProxy: androidx.camera.core.ImageProxy,
    detector: com.google.mlkit.vision.face.FaceDetector,
    onFrameQualityChanged: (ready: Boolean, message: String, result: FaceDetectionResult) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    try {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val (isReady, message, result) = evaluateFaceDetectionResult(
                    faces = faces,
                    imageWidth = mediaImage.width,
                    imageHeight = mediaImage.height,
                    imageProxy = imageProxy
                )
                onFrameQualityChanged(isReady, message, result)
            }
            .addOnFailureListener { e ->
                Log.e("EkycFaceCapture", "Face detection error: ${e.message}", e)
                onFrameQualityChanged(false, "Lỗi xử lý: ${e.message?.take(30) ?: "không xác định"}", 
                    FaceDetectionResult(false, reason = FaceDetectionReason.UNKNOWN))
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } catch (e: Exception) {
        Log.e("EkycFaceCapture", "Unexpected error in precheck: ${e.message}", e)
        onFrameQualityChanged(false, "Lỗi hệ thống", FaceDetectionResult(false, reason = FaceDetectionReason.UNKNOWN))
        imageProxy.close()
    }
}

private fun evaluateFaceDetectionResult(
    faces: List<com.google.mlkit.vision.face.Face>,
    imageWidth: Int,
    imageHeight: Int,
    imageProxy: androidx.camera.core.ImageProxy
): Triple<Boolean, String, FaceDetectionResult> {
    if (faces.isEmpty()) {
        return Triple(false, "không phát hiện khuôn mặt", FaceDetectionResult(false, faceCount = 0, reason = FaceDetectionReason.NO_FACE))
    }
    if (faces.size > 1) {
        return Triple(false, "Chỉ một người trong khung", FaceDetectionResult(true, faceCount = faces.size, reason = FaceDetectionReason.MULTIPLE_FACES))
    }

    val face = faces[0]
    val frameArea = (imageWidth * imageHeight).toFloat()
    val faceArea = face.boundingBox.width() * face.boundingBox.height()
    val faceRatio = faceArea / frameArea

    val faceInfo = FaceInfo(
        faceBoundingBox = FaceBoundingBox(
            face.boundingBox.left.toFloat(), face.boundingBox.top.toFloat(),
            face.boundingBox.right.toFloat(), face.boundingBox.bottom.toFloat(),
            face.boundingBox.width().toFloat(), face.boundingBox.height().toFloat()
        ),
        headEulerAngleY = face.headEulerAngleY,
        headEulerAngleX = face.headEulerAngleX,
        headEulerAngleZ = face.headEulerAngleZ
    )

    if (faceRatio < 0.20f) {
        return Triple(false, "Di chuyển gần hơn", FaceDetectionResult(true, 1, listOf(faceInfo), FaceDetectionReason.FACE_TOO_SMALL))
    }
    if (faceRatio > 0.60f) {
        return Triple(false, "Di chuyển xa hơn", FaceDetectionResult(true, 1, listOf(faceInfo), FaceDetectionReason.FACE_OUT_OF_FRAME))
    }

    val maxAngle = 15f
    if (Math.abs(face.headEulerAngleY) > maxAngle || Math.abs(face.headEulerAngleX) > maxAngle || Math.abs(face.headEulerAngleZ) > maxAngle) {
        return Triple(false, "Quay thẳng phía trước", FaceDetectionResult(true, 1, listOf(faceInfo), FaceDetectionReason.FACE_TILTED))
    }

    val averageLuma = calculateAverageLuma(imageProxy)
    if (averageLuma < 45f) {
        return Triple(false, "Căn chỉnh ánh sáng (quá tối)", FaceDetectionResult(true, 1, listOf(faceInfo), FaceDetectionReason.LOW_LIGHT))
    }

    return Triple(true, "Khung hình đạt điều kiện, có thể chụp", 
        FaceDetectionResult(true, 1, listOf(faceInfo), FaceDetectionReason.READY_TO_CAPTURE, canCapture = true))
}

private fun calculateAverageLuma(imageProxy: androidx.camera.core.ImageProxy): Float {
    val plane = imageProxy.planes.firstOrNull() ?: return 128f
    val buffer = plane.buffer
    buffer.rewind()
    val size = buffer.remaining()
    if (size <= 0) return 128f
    var sum = 0L
    val step = 4
    var count = 0
    for (i in 0 until size step step) {
        sum += buffer.get(i).toInt() and 0xFF
        count++
    }
    return if (count > 0) sum.toFloat() / count else 128f
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}