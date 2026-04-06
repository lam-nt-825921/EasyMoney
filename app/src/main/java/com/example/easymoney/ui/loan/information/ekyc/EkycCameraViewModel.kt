package com.example.easymoney.ui.loan.information.ekyc

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.model.CameraFrameData
import com.example.easymoney.domain.model.EkycCaptureRequest
import com.example.easymoney.domain.model.FaceDetectionResult
import com.example.easymoney.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay

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
    
    // ========== PERMISSION LOGIC ==========
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
    
    // ========== CAMERA LOGIC ==========
    fun onFrameAnalyzed(result: FaceDetectionResult) {
        _uiState.update {
            it.copy(
                faceDetectionResult = result,
                cameraState = CameraState.Previewing,
                precheckMessage = getPrecheckMessage(result),
                precheckMessageType = getPrecheckMessageType(result)
            )
        }
    }
    
    fun onCaptureClick() {
        val result = _uiState.value.faceDetectionResult
        
        // Check precheck passed
        if (result?.canCapture != true) {
            _uiState.update {
                it.copy(
                    precheckMessage = "Vui lòng thực hiện lại yêu cầu",
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
                precheckMessage = "Đang xử lý ảnh..."
            )
        }
        
        // Start upload
        uploadFaceImage(imageFile)
    }
    
    // ========== UPLOAD LOGIC ==========
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
                    
                    // Navigate to next step after delay
                    delay(1500)
                    _uiEffect.send(EkycUiEffect.NavigateToNextStep)
                    
                    // Clean up
                    imageFile.delete()
                }
                
                is com.example.easymoney.domain.common.Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            uploadState = UploadState.Error,
                            errorMessage = uploadResult.message,
                            errorRetryCount = it.errorRetryCount + 1
                        )
                    }
                    
                    _uiEffect.send(
                        EkycUiEffect.ShowError(uploadResult.message ?: "Upload thất bại")
                    )
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
    
    // ========== NAVIGATION EVENTS ==========
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
    
    // ========== HELPER FUNCTIONS ==========
    private fun getPrecheckMessage(result: FaceDetectionResult): String {
        return when {
            !result.hasFace -> "Không phát hiện khuôn mặt"
            result.faceCount > 1 -> "Chỉ một người trong khung"
            result.canCapture -> "Sẵn sàng chụp"
            else -> result.reason.name.replace("_", " ").lowercase().capitalize()
        }
    }
    
    private fun getPrecheckMessageType(result: FaceDetectionResult): MessageType {
        return if (result.canCapture) MessageType.INFO else MessageType.WARNING
    }
}