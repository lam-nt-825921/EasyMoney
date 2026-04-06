# Tài liệu 4: eKYC Navigation Flow & ViewModel Implementation

**Phiên bản**: 1.0  
**Ngày**: 2026-04-06  
**Trạng thái**: Final Implementation Guide

---

## 1. Tổng Quan

Tài liệu này định nghĩa:
1. **State Machine**: Các state trong eKYC flow + transitions
2. **ViewModel**: EkycCameraViewModel + state management
3. **Screen Composables**: Camera, Permission, Result screens
4. **Navigation Events**: Cách điều hướng giữa các màn
5. **Integration**: Cách gắn vào LoanFlowScreen

---

## 2. State Machine & UiState

### 2.1 EkycUiState (Complete)

```kotlin
// File: ui/loan/information/ekyc/EkycUiState.kt
package com.example.easymoney.ui.loan.information.ekyc

import android.net.Uri
import com.example.easymoney.domain.model.FaceDetectionResult
import java.io.File

data class EkycUiState(
    // ========== PERMISSION STATE ==========
    val permissionState: PermissionState = PermissionState.NotAsked,
    
    // ========== CAMERA STATE ==========
    val cameraState: CameraState = CameraState.Idle,
    val faceDetectionResult: FaceDetectionResult? = null,
    val precheckMessage: String? = null,
    val precheckMessageType: MessageType = MessageType.INFO,
    
    // ========== CAPTURE & PREVIEW STATE ==========
    val capturedImageFile: File? = null,
    val capturedImageUri: Uri? = null,
    
    // ========== UPLOAD STATE ==========
    val uploadState: UploadState = UploadState.Idle,
    val uploadProgress: Float = 0f,
    
    // ========== ERROR STATE ==========
    val errorMessage: String? = null,
    val errorRetryCount: Int = 0,
    
    // ========== FLOW METADATA ==========
    val sessionId: String = "",
    val flowId: String = "",
    val attemptCount: Int = 0,
    val isInitializing: Boolean = true
)

enum class PermissionState {
    NotAsked,           // Chưa xin quyền
    Granted,            // Đã cấp quyền
    Denied,             // Từ chối lần 1
    PermanentlyDenied   // Từ chối vĩnh viễn (phải mở Settings)
}

enum class CameraState {
    Idle,               // Chưa khởi động camera
    Previewing,         // Camera đang preview
    Capturing,          // Đang chụp ảnh
    Processing          // Đang process ảnh (ML precheck)
}

enum class UploadState {
    Idle,               // Chưa upload
    Uploading,          // Đang upload
    Success,            // Upload thành công
    Error,              // Upload thất bại
    Retry               // Đang retry
}

enum class MessageType {
    INFO,               // Màu xám (hướng dẫn)
    WARNING,            // Màu cam (cảnh báo)
    ERROR               // Màu đỏ (lỗi)
}
```

### 2.2 UiEvent (User Interactions)

```kotlin
// File: ui/loan/information/ekyc/EkycUiEvent.kt
package com.example.easymoney.ui.loan.information.ekyc

sealed class EkycUiEvent {
    // ========== SCREEN LIFECYCLE ==========
    data object OnScreenEnter : EkycUiEvent()
    data object OnScreenExit : EkycUiEvent()
    
    // ========== PERMISSION ==========
    data object OnRequestPermission : EkycUiEvent()
    data class OnPermissionResult(val granted: Boolean) : EkycUiEvent()
    data object OnOpenSettings : EkycUiEvent()
    
    // ========== CAMERA ==========
    data object OnStartCamera : EkycUiEvent()
    data object OnStopCamera : EkycUiEvent()
    data object OnCaptureClick : EkycUiEvent()
    data object OnRetakeClick : EkycUiEvent()
    data class OnFrameAnalyzed(val result: FaceDetectionResult) : EkycUiEvent()
    
    // ========== UPLOAD ==========
    data object OnConfirmCapture : EkycUiEvent()
    data object OnRetryUpload : EkycUiEvent()
    
    // ========== NAVIGATION ==========
    data object OnBackClick : EkycUiEvent()
    data object OnExitFlow : EkycUiEvent()
}
```

### 2.3 UiEffect (One-time Actions)

```kotlin
// File: ui/loan/information/ekyc/EkycUiEffect.kt
package com.example.easymoney.ui.loan.information.ekyc

sealed class EkycUiEffect {
    data object NavigateToNextStep : EkycUiEffect()
    data object NavigateBack : EkycUiEffect()
    data object OpenSettings : EkycUiEffect()
    data class ShowToast(val message: String) : EkycUiEffect()
    data class ShowError(val message: String) : EkycUiEffect()
}
```

---

## 3. ViewModel Architecture

### 3.1 EkycCameraViewModel

```kotlin
// File: ui/loan/information/ekyc/EkycCameraViewModel.kt
package com.example.easymoney.ui.loan.information.ekyc

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.data.ml.FaceDetectionProcessor
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
        FaceDetectionProcessor.init()
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
    fun onFrameAnalyzed(frameData: CameraFrameData) {
        viewModelScope.launch {
            val result = FaceDetectionProcessor.detectFace(frameData)
            
            _uiState.update {
                it.copy(
                    faceDetectionResult = result,
                    cameraState = CameraState.Previewing,
                    precheckMessage = getPrecheckMessage(result),
                    precheckMessageType = getPrecheckMessageType(result)
                )
            }
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
        
        // Trigger capture in CameraX
        // This will call onImageCaptured() callback from screen
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
                appVersion = "1.0.0", // Get from BuildConfig
                imageWidth = 1920,    // Get from actual capture
                imageHeight = 1440,
                precheckPassed = result?.canCapture == true,
                precheckReasons = listOfNotNull(
                    result?.reason?.name?.takeIf { result.reason.name != "READY_TO_CAPTURE" }
                ),
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
                    kotlinx.coroutines.delay(1500)
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
            
            is EkycUiEvent.OnRequestPermission -> {
                // Trigger permission request in screen
                _uiEffect.launch { send(EkycUiEffect.NavigateToNextStep) }
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
                    FaceDetectionProcessor.cleanup()
                    _uiEffect.send(EkycUiEffect.NavigateBack)
                }
            }
            
            is EkycUiEvent.OnExitFlow -> {
                viewModelScope.launch {
                    FaceDetectionProcessor.cleanup()
                    _uiEffect.send(EkycUiEffect.NavigateBack)
                }
            }
            
            else -> {}
        }
    }
    
    // ========== HELPER FUNCTIONS ==========
    private fun getPrecheckMessage(result: FaceDetectionResult): String {
        return when {
            !result.hasFace -> "Không phát hiện khuôn mặt"
            result.faceCount > 1 -> "Chỉ một người trong khung"
            result.reason.name == "FACE_TOO_SMALL" -> "Di chuyển gần hơn"
            result.reason.name == "FACE_OUT_OF_FRAME" -> "Đặt khuôn mặt vào khung"
            result.reason.name == "FACE_TILTED" -> "Quay trực tiếp phía trước"
            result.reason.name == "LOW_LIGHT" -> "Ánh sáng yếu, di chuyển đến nơi sáng"
            result.reason.name == "BLURRY_IMAGE" -> "Giữ yên, ảnh bị mờ"
            result.canCapture -> "Sẵn sàng chụp"
            else -> ""
        }
    }
    
    private fun getPrecheckMessageType(result: FaceDetectionResult): MessageType {
        return when {
            result.canCapture -> MessageType.INFO
            result.reason.name in listOf("LOW_LIGHT", "BLURRY_IMAGE") -> MessageType.WARNING
            else -> MessageType.INFO
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        FaceDetectionProcessor.cleanup()
    }
}
```

---

## 4. Screen Composables

### 4.1 EkycFaceCaptureScreen (Main Route)

```kotlin
// File: ui/loan/information/ekyc/EkycFaceCaptureScreen.kt
package com.example.easymoney.ui.loan.information.ekyc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.easymoney.ui.components.AppTopBarOverride
import com.example.easymoney.ui.components.ScreenColorMode
import com.example.easymoney.ui.components.SystemBarMode
import com.example.easymoney.ui.components.TopBarMode
import com.example.easymoney.ui.components.RegisterTopBarOverride
import com.example.easymoney.ui.theme.EkycColors

@Composable
fun EkycFaceCaptureScreen(
    viewModel: EkycCameraViewModel = hiltViewModel(),
    onNavigateToNextStep: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // ✓ Register chrome mode override (from Tài liệu 1)
    RegisterTopBarOverride(
        ownerRoute = "ekyc_face_capture",
        override = AppTopBarOverride(
            topBarMode = TopBarMode.HIDDEN,
            systemBarMode = SystemBarMode.CAMERA_DARK_IMMERSIVE,
            screenColorMode = ScreenColorMode.FIXED_CAMERA_BLACK
        )
    )
    
    // Handle UI effects (navigation, etc.)
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is EkycUiEffect.NavigateToNextStep -> onNavigateToNextStep()
                is EkycUiEffect.NavigateBack -> onNavigateBack()
                is EkycUiEffect.OpenSettings -> {
                    // Open app settings for camera permission
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    })
                }
                else -> {}
            }
        }
    }
    
    // Handle screen lifecycle
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(EkycUiEvent.OnScreenEnter)
    }
    
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onEvent(EkycUiEvent.OnScreenExit)
    }
    
    // Render content based on permission state
    when (uiState.permissionState) {
        PermissionState.NotAsked, PermissionState.Denied -> {
            EkycPermissionScreen(
                onRequestPermission = { viewModel.onEvent(EkycUiEvent.OnRequestPermission) },
                onOpenSettings = { viewModel.onEvent(EkycUiEvent.OnOpenSettings) },
                isDeniedPermanently = uiState.permissionState == PermissionState.PermanentlyDenied,
                modifier = modifier
            )
        }
        
        PermissionState.Granted -> {
            if (uiState.isInitializing) {
                // Show loading while initializing
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(EkycColors.cameraSceneBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EkycColors.cameraTextPrimary)
                }
            } else {
                EkycCameraContentScreen(
                    uiState = uiState,
                    onFrameAnalyzed = { frame -> viewModel.onFrameAnalyzed(frame) },
                    onCaptureClick = { viewModel.onCaptureClick() },
                    onImageCaptured = { file -> viewModel.onImageCaptured(file) },
                    onRetakeClick = { viewModel.onRetakeClick() },
                    onBackClick = { viewModel.onEvent(EkycUiEvent.OnBackClick) },
                    modifier = modifier
                )
            }
        }
        
        PermissionState.PermanentlyDenied -> {
            EkycPermissionDeniedScreen(
                onOpenSettings = { viewModel.onEvent(EkycUiEvent.OnOpenSettings) },
                onCancel = { viewModel.onEvent(EkycUiEvent.OnBackClick) },
                modifier = modifier
            )
        }
    }
}
```

### 4.2 Permission Screen

```kotlin
// File: ui/loan/information/ekyc/EkycPermissionScreen.kt
@Composable
fun EkycPermissionScreen(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    isDeniedPermanently: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EkycColors.permissionBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera_error),
                contentDescription = null,
                tint = EkycColors.cameraErrorText,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Cần quyền camera",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EkycColors.permissionText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isDeniedPermanently) {
                    "Vui lòng cấp quyền camera trong Cài đặt"
                } else {
                    "Ứng dụng cần quyền truy cập camera để chụp ảnh."
                },
                fontSize = 14.sp,
                color = EkycColors.cameraTextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = if (isDeniedPermanently) onOpenSettings else onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EkycColors.permissionButtonBackground,
                    contentColor = EkycColors.permissionButtonText
                )
            ) {
                Text(if (isDeniedPermanently) "Mở Cài đặt" else "Cấp quyền")
            }
        }
    }
}
```

### 4.3 Camera Content Screen (CameraX Integration)

```kotlin
// File: ui/loan/information/ekyc/EkycCameraContentScreen.kt
@Composable
fun EkycCameraContentScreen(
    uiState: EkycUiState,
    onFrameAnalyzed: (CameraFrameData) -> Unit,
    onCaptureClick: () -> Unit,
    onImageCaptured: (File) -> Unit,
    onRetakeClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EkycColors.cameraSceneBackground)
    ) {
        // CameraX Preview + Frame Analyzer
        EkycCameraPreview(
            onFrameAnalyzed = onFrameAnalyzed,
            onImageCaptured = onImageCaptured
        )
        
        // Frame guide overlay
        EkycFrameGuide()
        
        // Instructions
        EkycCameraInstructions(
            message = uiState.precheckMessage,
            messageType = uiState.precheckMessageType
        )
        
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = EkycColors.cameraIconColor
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        
        // Capture button (only if precheck passed)
        FloatingActionButton(
            onClick = onCaptureClick,
            enabled = uiState.faceDetectionResult?.canCapture == true && 
                      uiState.uploadState == UploadState.Idle,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            containerColor = EkycColors.cameraButtonBackground,
            contentColor = EkycColors.cameraButtonText
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
        }
        
        // Upload progress
        if (uiState.uploadState == UploadState.Uploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = EkycColors.cameraTextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang tải lên...", color = EkycColors.cameraTextPrimary)
                }
            }
        }
    }
}
```

---

## 5. Navigation Integration

### 5.1 Adding to LoanFlowScreen

```kotlin
// In LoanFlowScreen.kt
composable(route = "ekyc_face_capture") {
    EkycFaceCaptureScreen(
        onNavigateToNextStep = {
            navController.navigate("next_step") {
                popUpTo("ekyc_face_capture") { inclusive = true }
            }
        },
        onNavigateBack = {
            navController.popBackStack()
        }
    )
}
```

### 5.2 Navigation Routes

```
LoanFlow
├── step_1: LoanConfigurationScreen
├── step_2: ... (other eKYC steps)
│   ├── ekyc_intro: Introduction
│   └── ekyc_face_capture: Camera capture
├── step_3: Confirmation
└── step_final: Result
```

---

## 6. State Flow Diagram

```
┌─────────────────┐
│  Screen Enter   │
└────────┬────────┘
         │
    ┌────▼─────┐
    │ Check    │
    │ Permission
    └────┬─────┘
         │
    ┌────▼──────────────────┐
    │                       │
┌───▼────────┐      ┌──────▼──────┐
│ NotAsked   │      │ Granted     │
│ / Denied   │      │             │
└────────────┘      └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │ Camera      │
                    │ Previewing  │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │ Face        │
                    │ Detection   │
                    │ (real-time) │
                    └──────┬──────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
    ┌──────▼─────┐  ┌──────▼─────┐  ┌─────▼──────┐
    │ PreCheck   │  │ PreCheck   │  │ Ready to   │
    │ Failed     │  │ Warning    │  │ Capture    │
    └────────────┘  └────────────┘  └─────┬──────┘
                                           │
                                    ┌──────▼──────┐
                                    │ Capturing   │
                                    └──────┬──────┘
                                           │
                                    ┌──────▼──────┐
                                    │ Processing  │
                                    └──────┬──────┘
                                           │
                                    ┌──────▼──────┐
                                    │ Uploading   │
                                    └──────┬──────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
            ┌───────▼──────┐     ┌─────────▼────────┐    ┌────────▼────┐
            │ Success      │     │ Error (Retry)   │    │ Final Error  │
            │              │     │                 │    │              │
            └───────┬──────┘     └─────────┬────────┘    └──────────────┘
                    │                      │
                    └──────────────────────┼──────────────────────┐
                                           │
                                    ┌──────▼──────────┐
                                    │ Navigate Next   │
                                    │ Step            │
                                    └─────────────────┘
```

---

## 7. Checklist Implementation

- [ ] Create `EkycUiState.kt` (state data class)
- [ ] Create `EkycUiEvent.kt` (event sealed class)
- [ ] Create `EkycUiEffect.kt` (effect sealed class)
- [ ] Create `EkycCameraViewModel.kt` (Hilt ViewModel)
- [ ] Create `EkycFaceCaptureScreen.kt` (main screen)
- [ ] Create `EkycPermissionScreen.kt` (permission UI)
- [ ] Create `EkycCameraContentScreen.kt` (camera UI)
- [ ] Create `EkycCameraPreview.kt` (CameraX binding)
- [ ] Create `EkycFrameGuide.kt` (face frame overlay)
- [ ] Create `EkycCameraInstructions.kt` (precheck messages)
- [ ] Add route to LoanFlowScreen
- [ ] Test permission flow
- [ ] Test camera preview + ML Kit integration
- [ ] Test upload flow
- [ ] Test error handling + retry

---

## 8. Testing Scenarios

### Scenario 1: Permission Granted on First Time
```
Screen Enter
  → Check Permission
  → Already Granted
  → Show Camera
  → Detect Faces (real-time)
  → User Captures
  → Upload Success
  → Navigate Next
```

### Scenario 2: Permission Denied, Then Granted
```
Screen Enter
  → Check Permission
  → Not Asked
  → Show Permission Dialog
  → User Denies
  → Show "Ask Again" UI
  → User Clicks "Cấp quyền"
  → Request Permission
  → User Grants
  → Show Camera
  → ...rest same as Scenario 1
```

### Scenario 3: Precheck Fails - Face Too Small
```
Camera Previewing
  → Frame Analyzed
  → FaceDetectionResult.canCapture = false
  → Reason = FACE_TOO_SMALL
  → Message = "Di chuyển gần hơn"
  → MessageType = INFO
  → Display message
  → User moves closer
  → Precheck passed
  → Can capture now
```

### Scenario 4: Upload Failed - Retry
```
Capturing
  → Processing
  → Uploading
  → Network Error
  → uploadState = Error
  → Show "Thử lại" button
  → User clicks retry
  → Upload again
  → Success
```

---

## 9. Key Points

1. **Single ViewModel**: `EkycCameraViewModel` handles toàn bộ logic
2. **State-driven UI**: UI is pure function of state
3. **Separated concerns**: Events → State → Effects
4. **ML Kit integration**: Real-time face detection mỗi frame
5. **Error handling**: Retry logic + clear error messages
6. **Clean up**: `FaceDetectionProcessor.cleanup()` on exit

---

**Document Version**: 1.0  
**Scope**: Complete navigation flow + ViewModel implementation  
**Dependencies**: Tài liệu 1-3  
**Ready for**: Development & Testing

