package com.example.easymoney.domain.model

import android.graphics.Bitmap

/**
 * Frame data từ CameraX
 */
data class CameraFrameData(
    val bitmap: Bitmap,
    val timestamp: Long,
    val rotationDegrees: Int = 0,
    val width: Int,
    val height: Int
)

