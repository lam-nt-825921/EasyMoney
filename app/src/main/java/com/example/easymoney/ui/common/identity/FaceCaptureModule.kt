package com.example.easymoney.ui.common.identity

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.easymoney.R
import java.util.concurrent.Executors

/**
 * Shared Module for Face Capture and Liveness Detection.
 * Uses CameraX and ML Kit for face processing.
 */
@Composable
fun FaceCaptureModule(
    onResult: (FaceCaptureResult) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val statusInitial = stringResource(R.string.face_capture_status_initial)
    val statusConfirming = stringResource(R.string.face_capture_status_confirming)
    val statusBlink = stringResource(R.string.face_capture_status_blink)
    val statusNoFace = stringResource(R.string.face_capture_status_no_face)
    var isFaceDetected by remember { mutableStateOf(false) }
    var livenessStatus by remember { mutableStateOf(statusInitial) }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, FaceAnalyzer { detected, metadata ->
                                    isFaceDetected = detected
                                    if (detected) {
                                        val leftEye = metadata["leftEyeOpen"] as? Float ?: -1f
                                        val rightEye = metadata["rightEyeOpen"] as? Float ?: -1f
                                        // Check for blink (liveness)
                                        if (leftEye < 0.2f && rightEye < 0.2f) {
                                            livenessStatus = statusConfirming
                                            onResult(FaceCaptureResult(null, metadata, true))
                                        } else {
                                            livenessStatus = statusBlink
                                        }
                                    } else {
                                        livenessStatus = statusNoFace
                                    }
                                })
                            }

                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview, imageAnalyzer
                            )
                        } catch (exc: Exception) {
                            android.util.Log.e("FaceCaptureModule", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Circular Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val circleRadius = size.width * 0.35f
                val circleCenter = Offset(size.width / 2, size.height * 0.45f)
                
                val path = Path().apply {
                    addOval(Rect(circleCenter, circleRadius))
                }
                
                // Draw darkened background outside the circle
                clipPath(path, clipOp = ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.7f))
                }
                
                // Draw circle border
                drawCircle(
                    color = if (isFaceDetected) Color.Green else Color.White,
                    radius = circleRadius,
                    center = circleCenter,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
            }

            // Instructions
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = livenessStatus,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Permission not granted UI
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.face_capture_camera_denied),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.face_capture_close),
                tint = Color.White
            )
        }
    }
}
