package com.example.easymoney.ui.loan.information.ekyc

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.domain.model.EkycCaptureRequest
import com.example.easymoney.domain.model.FaceDetectionResult
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EkycCameraViewModel @Inject constructor(
    app: Application,
    private val loanRepository: LoanRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(EkycUiState(
        sessionId = UUID.randomUUID().toString(),
        flowId = UUID.randomUUID().toString()
    ))
    val uiState: StateFlow<EkycUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EkycUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    private val context = app.applicationContext

    init {
        checkPermission()
    }

    private fun checkPermission() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.update {
            it.copy(
                permissionState = if (hasCameraPermission) {
                    PermissionState.Granted
                } else {
                    PermissionState.NotAsked
                },
                isInitializing = false
            )
        }
    }

    fun onFrameAnalyzed(result: FaceDetectionResult) {
        _uiState.update {
            it.copy(
                faceDetectionResult = result,
                cameraState = CameraState.Previewing,
                precheckMessage = precheckMessageFor(result),
                precheckMessageType = precheckMessageTypeFor(result)
            )
        }
    }

    fun onCaptureClick() {
        val result = _uiState.value.faceDetectionResult

        if (result?.canCapture != true) {
            _uiState.update {
                it.copy(
                    precheckMessage = UiText.StringResource(R.string.ekyc_precheck_unknown),
                    precheckMessageType = MessageType.ERROR
                )
            }
            return
        }

        _uiState.update {
            it.copy(cameraState = CameraState.Capturing)
        }
    }

    fun onImageCaptured(imageFile: File) {
        _uiState.update {
            it.copy(
                capturedImageFile = imageFile,
                cameraState = CameraState.Processing,
                precheckMessage = UiText.StringResource(R.string.ekyc_processing_image)
            )
        }

        uploadFaceImage(imageFile)
    }

    private fun uploadFaceImage(imageFile: File) {
        viewModelScope.launch {
            val state = _uiState.value
            val result = state.faceDetectionResult

            _uiState.update {
                it.copy(uploadState = UploadState.Uploading)
            }

            val request = EkycCaptureRequest(
                sessionId = state.sessionId,
                flowId = state.flowId,
                step = "selfie",
                imageFile = imageFile,
                captureTimestamp = System.currentTimeMillis(),
                deviceModel = Build.MODEL,
                osVersion = Build.VERSION.SDK_INT.toString(),
                appVersion = "1.0.0",
                imageWidth = 1920,
                imageHeight = 1440,
                precheckPassed = result?.canCapture == true,
                precheckReasons = if (result?.canCapture == true) emptyList() else listOf("PRECHECK_FAILED"),
                faceBoundingBox = result?.faces?.firstOrNull()?.let {
                    """{"left": ${it.faceBoundingBox.left}, "top": ${it.faceBoundingBox.top}, "right": ${it.faceBoundingBox.right}, "bottom": ${it.faceBoundingBox.bottom}}"""
                }
            )

            val uploadResult = loanRepository.captureFace(request)

            when (uploadResult) {
                is com.example.easymoney.domain.common.Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            uploadState = UploadState.Success,
                            errorMessage = null,
                            errorRetryCount = 0
                        )
                    }

                    delay(1500)
                    _uiEffect.send(EkycUiEffect.NavigateToNextStep)

                    imageFile.delete()
                }

                is com.example.easymoney.domain.common.Resource.Error -> {
                    // Workflow #68 — backend message stays dynamic; default upload failure uses localised copy.
                    val message: UiText = uploadResult.message
                        .takeIf { it.isNotBlank() }
                        ?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(R.string.ekyc_error_upload_failed)
                    _uiState.update {
                        it.copy(
                            uploadState = UploadState.Error,
                            errorMessage = message,
                            errorRetryCount = it.errorRetryCount + 1
                        )
                    }

                    _uiEffect.send(EkycUiEffect.ShowError(message))
                }

                else -> {}
            }
        }
    }

    fun onRetryUpload() {
        val imageFile = _uiState.value.capturedImageFile
        if (imageFile != null) {
            uploadFaceImage(imageFile)
        }
    }

    fun resetState() {
        _uiState.update {
            EkycUiState(
                sessionId = UUID.randomUUID().toString(),
                flowId = UUID.randomUUID().toString(),
                permissionState = it.permissionState
            )
        }
    }

    fun onEvent(event: EkycUiEvent) {
        when (event) {
            is EkycUiEvent.OnScreenEnter -> {
                if (_uiState.value.permissionState == PermissionState.Granted) {
                    _uiState.update { it.copy(cameraState = CameraState.Previewing) }
                }
            }

            is EkycUiEvent.OnPermissionResult -> {
                val newState = if (event.granted) PermissionState.Granted else PermissionState.Denied
                _uiState.update { it.copy(permissionState = newState) }
            }

            is EkycUiEvent.OnOpenSettings -> {
                viewModelScope.launch {
                    _uiEffect.send(EkycUiEffect.OpenSettings)
                }
            }

            is EkycUiEvent.OnBackClick -> {
                viewModelScope.launch {
                    _uiEffect.send(EkycUiEffect.NavigateBack)
                }
            }

            is EkycUiEvent.OnRetakeClick -> {
                _uiState.update { it.copy(
                    uploadState = UploadState.Idle,
                    cameraState = CameraState.Previewing,
                    capturedImageFile = null,
                    errorMessage = null
                ) }
            }

            is EkycUiEvent.OnCaptureClick -> {
                onCaptureClick()
            }

            is EkycUiEvent.OnRetryUpload -> {
                onRetryUpload()
            }

            else -> {}
        }
    }

    private fun precheckMessageFor(result: FaceDetectionResult): UiText {
        val resId = when {
            !result.hasFace -> R.string.ekyc_precheck_no_face
            result.faceCount > 1 -> R.string.ekyc_precheck_multiple_faces
            result.canCapture -> R.string.ekyc_precheck_ready
            else -> when (result.reason) {
                com.example.easymoney.domain.model.FaceDetectionReason.FACE_TOO_SMALL -> R.string.ekyc_precheck_face_too_small
                com.example.easymoney.domain.model.FaceDetectionReason.FACE_OUT_OF_FRAME -> R.string.ekyc_precheck_face_out_of_frame
                com.example.easymoney.domain.model.FaceDetectionReason.FACE_TILTED -> R.string.ekyc_precheck_face_tilted
                com.example.easymoney.domain.model.FaceDetectionReason.LOW_LIGHT -> R.string.ekyc_precheck_low_light
                com.example.easymoney.domain.model.FaceDetectionReason.MULTIPLE_FACES -> R.string.ekyc_precheck_multiple_faces
                com.example.easymoney.domain.model.FaceDetectionReason.NO_FACE -> R.string.ekyc_precheck_no_face
                com.example.easymoney.domain.model.FaceDetectionReason.READY_TO_CAPTURE -> R.string.ekyc_precheck_ready
                else -> R.string.ekyc_precheck_unknown
            }
        }
        return UiText.StringResource(resId)
    }

    private fun precheckMessageTypeFor(result: FaceDetectionResult): MessageType {
        return if (result.canCapture) MessageType.INFO else MessageType.WARNING
    }
}
