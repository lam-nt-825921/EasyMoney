# Tài liệu 3: eKYC Technical Architecture & Implementation

**Phiên bản**: 1.0  
**Ngày**: 2026-04-06  
**Trạng thái**: Technical Design Phase

---

## 1. Tổng Quan

Tài liệu này định nghĩa:
1. **Dependencies & Libraries**: CameraX, ML Kit Face Detection, Retrofit
2. **Model Layers**: Data/Domain/UI models cho eKYC flow
3. **Repository Pattern**: Interface + Implementation cho camera + face detection
4. **ML Kit Integration**: Cách gọi Face Detection API
5. **Backend Payload**: Định dạng multipart gửi server
6. **Architecture Diagram**: Flow từ camera → ML precheck → upload

---

## 2. Dependencies & Build Configuration

### 2.1 Thêm vào `gradle/libs.versions.toml`

```toml
[versions]
# CameraX
camerax = "1.3.0"
mlkit_face_detection = "16.1.5"

[libraries]
# CameraX
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
androidx-camera-video = { group = "androidx.camera", name = "camera-video", version.ref = "camerax" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }

# ML Kit - Face Detection
google-mlkit-face-detection = { group = "com.google.mlkit", name = "face-detection", version.ref = "mlkit_face_detection" }

[bundles]
camerax = [
    "androidx-camera-core",
    "androidx-camera-camera2",
    "androidx-camera-lifecycle",
    "androidx-camera-video",
    "androidx-camera-view"
]
```

### 2.2 Thêm vào `app/build.gradle.kts`

```kotlin
dependencies {
    // CameraX
    implementation(libs.bundles.camerax)
    
    // ML Kit Face Detection
    implementation(libs.google.mlkit.face.detection)
    
    // Hilt (đã có)
    // Retrofit (đã có)
    // Compose (đã có)
}

android {
    defaultConfig {
        minSdk = 21  // ML Kit Face Detection requires 21+
    }
}
```

### 2.3 Permissions trong `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.CAMERA" />
<!-- Nếu cần lưu file ảnh tạm -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## 3. Model Layers (Data/Domain/UI)

### 3.1 Domain Layer - Core Models

#### **File: `domain/model/FaceDetectionResult.kt`**
```kotlin
package com.example.easymoney.domain.model

import com.google.mlkit.vision.face.Face

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
```

#### **File: `domain/model/EkycCaptureRequest.kt`**
```kotlin
package com.example.easymoney.domain.model

import android.net.Uri
import java.io.File

/**
 * Request model cho face capture API
 * Được dùng để prepare data trước khi upload
 */
data class EkycCaptureRequest(
    val sessionId: String,
    val flowId: String,
    val step: String = "selfie",
    val imageFile: File,           // File ảnh JPEG để upload
    val imageUri: Uri? = null,     // Optional: Uri của ảnh (cache)
    val captureTimestamp: Long,
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val cameraLens: String = "front",
    val imageWidth: Int,
    val imageHeight: Int,
    val precheckPassed: Boolean,
    val precheckReasons: List<String> = emptyList(),
    val faceBoundingBox: String? = null,  // JSON string: "{left, top, right, bottom}"
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
```

#### **File: `domain/model/CameraFrameData.kt`**
```kotlin
package com.example.easymoney.domain.model

import android.graphics.Bitmap

/**
 * Frame data từ CameraX
 * Được dùng để gửi tới ML Kit Face Detection
 */
data class CameraFrameData(
    val bitmap: Bitmap,
    val timestamp: Long,
    val rotationDegrees: Int = 0,
    val width: Int,
    val height: Int
)
```

### 3.2 Data Layer - Repository Interface (Consolidated)

#### **File: `domain/repository/LoanRepository.kt`**
```kotlin
interface LoanRepository {
    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel>
    suspend fun getMyPackage(): Resource<LoanPackageModel>
    suspend fun getMyInfo(): Resource<MyInfoModel>
    suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel>
    
    // ========== eKYC Face Capture ==========
    /**
     * Upload ảnh face + metadata tới backend
     */
    suspend fun captureFace(request: EkycCaptureRequest): Resource<EkycCaptureResponse>
    
    /**
     * Upload ảnh với custom file (fallback)
     */
    suspend fun captureFaceCustom(
        imageFile: File,
        metadataJson: String
    ): Resource<EkycCaptureResponse>
}
```

**Note**: eKYC methods được thêm vào `LoanRepository` để **consolidate** thành 1 repository duy nhất cho cả loan flow + eKYC.

### 3.3 Data Layer - Repository Implementation (Consolidated)

#### **File: `data/repository/LoanRepositoryImpl.kt`**
```kotlin
@Inject
class LoanRepositoryImpl @Inject constructor(
    // Add ekycService: EkycService when actual backend available
) : LoanRepository {
    
    // ... existing loan package methods ...
    
    // ========== eKYC Face Capture ==========
    override suspend fun captureFace(request: EkycCaptureRequest): Resource<EkycCaptureResponse> {
        return try {
            val imageBody = request.imageFile.asRequestBody("image/jpeg".toMediaType())
            val metadataJson = buildMetadataJson(request)
            val metadataBody = metadataJson.toRequestBody("application/json".toMediaType())
            
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("face_image", request.imageFile.name, imageBody)
                .addFormDataPart("meta", metadataJson, metadataBody)
                .build()
            
            // Call actual Retrofit service: ekycService.captureFace(multipartBody)
            // For now, mock success response
            delay(500)
            Resource.Success(
                EkycCaptureResponse(
                    captureId = "capture-${System.currentTimeMillis()}",
                    status = "accepted",
                    nextStep = "face_matching"
                ),
                isFromMock = true
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Capture failed")
        }
    }
    
    override suspend fun captureFaceCustom(
        imageFile: File,
        metadataJson: String
    ): Resource<EkycCaptureResponse> {
        // Similar implementation...
    }
    
    private fun buildMetadataJson(request: EkycCaptureRequest): String {
        // Build JSON from request fields
    }
}
```

**Advantage**: Single repository para sa buong loan flow (package, info, provider, eKYC face capture).

### 3.4 Retrofit Service

#### **File: `data/service/EkycService.kt`**
```kotlin
package com.example.easymoney.data.service

import com.example.easymoney.domain.model.EkycCaptureResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap

interface EkycService {
    @Multipart
    @POST("/ekyc/face/capture")
    suspend fun captureFace(
        @PartMap parts: MultipartBody
    ): Response<EkycCaptureResponse>
}
```

---

## 4. ML Kit Face Detection Integration

### 4.1 ML Kit Processor

#### **File: `data/ml/FaceDetectionProcessor.kt`**
```kotlin
package com.example.easymoney.data.ml

import android.graphics.Bitmap
import com.example.easymoney.domain.model.CameraFrameData
import com.example.easymoney.domain.model.FaceDetectionReason
import com.example.easymoney.domain.model.FaceDetectionResult
import com.example.easymoney.domain.model.FaceInfo
import com.example.easymoney.domain.model.FaceBoundingBox
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

object FaceDetectionProcessor {
    
    private lateinit var faceDetector: FaceDetector
    
    fun init() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        
        faceDetector = FaceDetection.getClient(options)
    }
    
    /**
     * Phát hiện khuôn mặt trong frame từ camera
     * @param frameData - Camera frame data
     * @return FaceDetectionResult với decision: canCapture or reason fail
     */
    fun detectFace(frameData: CameraFrameData): FaceDetectionResult {
        if (!::faceDetector.isInitialized) {
            init()
        }
        
        return try {
            val image = InputImage.fromBitmap(frameData.bitmap, frameData.rotationDegrees)
            val detectedFaces = faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    // Handle success
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
            
            analyzeFaces(detectedFaces.result)
        } catch (e: Exception) {
            FaceDetectionResult(
                hasFace = false,
                reason = FaceDetectionReason.UNKNOWN
            )
        }
    }
    
    /**
     * Phân tích kết quả detection và quyết định canCapture
     */
    private fun analyzeFaces(faces: List<Face>): FaceDetectionResult {
        // No face detected
        if (faces.isEmpty()) {
            return FaceDetectionResult(
                hasFace = false,
                faceCount = 0,
                reason = FaceDetectionReason.NO_FACE,
                canCapture = false
            )
        }
        
        // Multiple faces
        if (faces.size > 1) {
            return FaceDetectionResult(
                hasFace = true,
                faceCount = faces.size,
                reason = FaceDetectionReason.MULTIPLE_FACES,
                canCapture = false
            )
        }
        
        val face = faces[0]
        val faceInfo = convertToFaceInfo(face)
        
        // Check conditions
        val checks = mutableListOf<FaceDetectionReason>()
        
        // 1. Check face size (bounding box occupies 20-60% of frame)
        val boxArea = (face.boundingBox.width() * face.boundingBox.height()) / (1280 * 720) // example frame size
        if (boxArea < 0.20f) {
            checks.add(FaceDetectionReason.FACE_TOO_SMALL)
        } else if (boxArea > 0.60f) {
            checks.add(FaceDetectionReason.FACE_OUT_OF_FRAME)
        }
        
        // 2. Check angle (yaw, pitch, roll)
        if (Math.abs(face.headEulerAngleY) > 15f || 
            Math.abs(face.headEulerAngleX) > 15f ||
            Math.abs(face.headEulerAngleZ) > 15f) {
            checks.add(FaceDetectionReason.FACE_TILTED)
        }
        
        // 3. Check if face is out of frame bounds
        if (face.boundingBox.left < 0 || face.boundingBox.top < 0) {
            checks.add(FaceDetectionReason.FACE_OUT_OF_FRAME)
        }
        
        // If any checks fail
        if (checks.isNotEmpty()) {
            return FaceDetectionResult(
                hasFace = true,
                faceCount = 1,
                faces = listOf(faceInfo),
                reason = checks.first(),
                canCapture = false
            )
        }
        
        // All checks passed
        return FaceDetectionResult(
            hasFace = true,
            faceCount = 1,
            faces = listOf(faceInfo),
            reason = FaceDetectionReason.READY_TO_CAPTURE,
            canCapture = true
        )
    }
    
    private fun convertToFaceInfo(face: Face): FaceInfo {
        val bbox = FaceBoundingBox(
            left = face.boundingBox.left,
            top = face.boundingBox.top,
            right = face.boundingBox.right,
            bottom = face.boundingBox.bottom,
            width = face.boundingBox.width(),
            height = face.boundingBox.height()
        )
        
        return FaceInfo(
            faceBoundingBox = bbox,
            headEulerAngleY = face.headEulerAngleY,
            headEulerAngleZ = face.headEulerAngleZ,
            headEulerAngleX = face.headEulerAngleX,
            leftEyeOpen = face.leftEyeOpenProbability > 0.5f,
            rightEyeOpen = face.rightEyeOpenProbability > 0.5f,
            smiling = face.smilingProbability > 0.5f
        )
    }
    
    fun cleanup() {
        if (::faceDetector.isInitialized) {
            faceDetector.close()
        }
    }
}
```

---

## 5. Backend Payload Format (Final)

### 5.1 Multipart Form Data

**Endpoint**: `POST /ekyc/face/capture`

**Request Body**:
```
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="face_image"; filename="face_capture.jpg"
Content-Type: image/jpeg

[BINARY JPEG DATA]
------WebKitFormBoundary
Content-Disposition: form-data; name="meta"; filename="metadata.json"
Content-Type: application/json

{
  "session_id": "abc123-def456",
  "flow_id": "loan-flow-001",
  "step": "selfie",
  "capture_ts": 1712430600000,
  "device_model": "SM-A515F",
  "os_version": "12",
  "app_version": "1.0.0",
  "camera_lens": "front",
  "image_width": 1920,
  "image_height": 1440,
  "precheck_passed": true,
  "precheck_reasons": [],
  "face_bbox": "{\"left\": 150, \"top\": 200, \"right\": 850, \"bottom\": 1000}",
  "quality_score": 0.95
}
------WebKitFormBoundary--
```

### 5.2 Response (Success)
```json
{
  "capture_id": "capture-xyz-789",
  "status": "accepted",
  "next_step": "face_matching",
  "message": "Ảnh chân dung được nhận"
}
```

### 5.3 Response (Failure)
```json
{
  "capture_id": "capture-xyz-789",
  "status": "rejected",
  "reason": "image_quality_too_low",
  "message": "Ảnh không đạt yêu cầu chất lượng"
}
```

---

## 6. File Structure & Organization

```
domain/
├── model/
│   ├── LoanPackageModel.kt (existing)
│   ├── MyInfoModel.kt (existing)
│   ├── LoanProviderInfoModel.kt (existing)
│   ├── FaceDetectionResult.kt ✨ NEW
│   ├── EkycCaptureRequest.kt ✨ NEW
│   └── CameraFrameData.kt ✨ NEW
└── repository/
    └── LoanRepository.kt (✏️ updated - added eKYC methods)

data/
├── repository/
│   └── LoanRepositoryImpl.kt (✏️ updated - added eKYC implementation)
├── service/
│   └── (EkycService.kt - create when actual backend available)
└── ml/
    └── FaceDetectionProcessor.kt ✨ NEW

ui/
└── loan/
    └── information/
        └── ekyc/
            ├── EkycFaceCaptureScreen.kt (will use LoanRepository)
            ├── EkycCameraViewModel.kt (will use LoanRepository)
            └── ...
```

**Consolidated Pattern**: All loan flow operations (packages, info, provider, eKYC) через `LoanRepository`

---

## 7. Dependency Injection (Hilt)

**No additional Hilt module needed** - `LoanRepository` is already provided by existing setup.

When implementing `EkycService` (actual backend), add to existing module:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object LoanModule {  // or update existing module
    
    @Provides
    @Singleton
    fun provideEkycService(retrofit: Retrofit): EkycService {
        return retrofit.create(EkycService::class.java)
    }
    
    // LoanRepositoryImpl will inject EkycService when backend ready
}
```

---

## 8. ViewModel State (Preview)

### 8.1 EkycUiState Structure (for next ViewModel)

```kotlin
data class EkycFaceCaptureUiState(
    val permissionState: PermissionState = PermissionState.NotAsked,
    val cameraState: CameraState = CameraState.Idle,
    val faceDetectionResult: FaceDetectionResult? = null,
    val precheckMessage: String? = null,
    val precheckMessageColor: Color = Color.Gray,
    val capturedImageFile: File? = null,
    val uploadState: UploadState = UploadState.Idle,
    val errorMessage: String? = null
)

enum class PermissionState {
    NotAsked, Granted, Denied, PermanentlyDenied
}

enum class CameraState {
    Idle, Previewing, Capturing, Processing
}

enum class UploadState {
    Idle, Uploading, Success, Error
}
```

---

## 9. Integration Checklist

- [ ] Add dependencies to `libs.versions.toml`
- [ ] Add to `app/build.gradle.kts`
- [ ] Create domain models (3 files)
- [ ] Create repository interface
- [ ] Create repository implementation
- [ ] Create Retrofit service
- [ ] Create ML Kit processor
- [ ] Create Hilt module
- [ ] Create ViewModel (in Tài liệu 4)
- [ ] Create Screen composables (in Tài liệu 4)
- [ ] Test with real device (camera + ML Kit)

---

## 10. Security & Privacy Considerations

- ✅ **Ảnh cục bộ**: Xóa sau upload thành công (không lưu permanent)
- ✅ **Encryption**: Dùng HTTPS cho upload
- ✅ **Data minimization**: Không log ảnh, chỉ log metadata
- ✅ **User consent**: Ask permission rõ ràng trước capture
- ✅ **GDPR**: Compliance nếu có EU users

---

## 11. Testing Notes

### ML Kit Test
```kotlin
// Test face detection với bitmap mock
val testBitmap = createTestBitmap(width = 1280, height = 720)
val frameData = CameraFrameData(
    bitmap = testBitmap,
    timestamp = System.currentTimeMillis(),
    width = 1280,
    height = 720
)
val result = FaceDetectionProcessor.detectFace(frameData)
assert(result.canCapture) // or check reason
```

### Repository Test
```kotlin
// Mock EkycService + test upload
val mockResponse = EkycCaptureResponse(
    captureId = "test-123",
    status = "accepted"
)
coEvery { ekycService.captureFace(any()) } returns Response.success(mockResponse)
val result = ekycRepository.captureFace(testRequest)
assert(result is Resource.Success)
```

---

## 12. Performance Considerations

- **ML Kit**: Fast mode (vs Accurate) cho real-time frame analysis
- **Camera**: 30 FPS vs higher FPS tradeoff
- **Memory**: Clear bitmaps after processing
- **Threading**: ML Kit + camera on background coroutine, UI on main

---

**Document Version**: 1.0  
**Scope**: Technical architecture + integration guide  
**Dependencies**: Tài liệu 1 (NavBar) + Tài liệu 2 (Camera UI)  
**Ready for**: Screen implementation (Tài liệu 4)





