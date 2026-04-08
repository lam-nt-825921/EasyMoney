package com.example.easymoney.domain.model

/**
 * Result từ ML Kit Face Detection
 */
data class FaceDetectionResult(
    val hasFace: Boolean,
    val faceCount: Int = 0,
    val faces: List<FaceInfo> = emptyList(),
    val reason: FaceDetectionReason = FaceDetectionReason.UNKNOWN,
    val canCapture: Boolean = false
)

data class FaceInfo(
    val faceBoundingBox: FaceBoundingBox,
    val headEulerAngleY: Float = 0f,   // yaw (-90 to 90)
    val headEulerAngleZ: Float = 0f,   // roll (-90 to 90)
    val headEulerAngleX: Float = 0f,   // pitch (-90 to 90)
    val leftEyeOpen: Boolean = false,
    val rightEyeOpen: Boolean = false,
    val smiling: Boolean = false
)

data class FaceBoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val width: Float,
    val height: Float
)

enum class FaceDetectionReason {
    UNKNOWN,
    NO_FACE,
    MULTIPLE_FACES,
    FACE_TOO_SMALL,
    FACE_OUT_OF_FRAME,
    FACE_TILTED,
    LOW_LIGHT,
    BLURRY_IMAGE,
    READY_TO_CAPTURE
}

