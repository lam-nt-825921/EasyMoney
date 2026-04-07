package com.example.easymoney.domain.model

import java.io.File

/**
 * Request model cho face capture API
 */
data class EkycCaptureRequest(
    val sessionId: String,
    val flowId: String,
    val step: String = "selfie",
    val imageFile: File,
    val captureTimestamp: Long,
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val cameraLens: String = "front",
    val imageWidth: Int,
    val imageHeight: Int,
    val precheckPassed: Boolean,
    val precheckReasons: List<String> = emptyList(),
    val faceBoundingBox: String? = null,
    val qualityScore: Float? = null
)

/**
 * Response từ backend sau upload
 */
data class EkycCaptureResponse(
    val captureId: String,
    val status: String,  // "accepted" | "rejected"
    val reason: String? = null,
    val nextStep: String? = null,
    val message: String? = null
)

