package com.example.easymoney.ui.common.identity

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executor

/**
 * Image Analysis for Face Detection and Liveness (Skeleton logic)
 */
class FaceAnalyzer(
    private val onFaceDetected: (Boolean, Map<String, Any>) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val metadata = mutableMapOf<String, Any>()
                        
                        // Simple liveness check logic (e.g., blinking)
                        metadata["leftEyeOpen"] = face.leftEyeOpenProbability ?: -1f
                        metadata["rightEyeOpen"] = face.rightEyeOpenProbability ?: -1f
                        metadata["smile"] = face.smilingProbability ?: -1f
                        
                        onFaceDetected(true, metadata)
                    } else {
                        onFaceDetected(false, emptyMap())
                    }
                }
                .addOnFailureListener {
                    Log.e("FaceAnalyzer", "Face detection failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
