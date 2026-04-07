package com.example.easymoney.ui.loan.information.ekyc

data class EkycUiState(
    val permissionState: PermissionState = PermissionState.NotAsked,
    val cameraState: CameraState = CameraState.Idle,
    val faceDetectionResult: com.example.easymoney.domain.model.FaceDetectionResult? = null,
    val precheckMessage: String? = null,
    val precheckMessageType: MessageType = MessageType.INFO,
    val capturedImageFile: java.io.File? = null,
    val capturedImageUri: android.net.Uri? = null,
    val uploadState: UploadState = UploadState.Idle,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null,
    val errorRetryCount: Int = 0,
    val sessionId: String = "",
    val flowId: String = "",
    val attemptCount: Int = 0,
    val isInitializing: Boolean = true
)

enum class PermissionState {
    NotAsked, Granted, Denied, PermanentlyDenied
}

enum class CameraState {
    Idle, Previewing, Capturing, Processing
}

enum class UploadState {
    Idle, Uploading, Success, Error, Retry
}

enum class MessageType {
    INFO, WARNING, ERROR
}

