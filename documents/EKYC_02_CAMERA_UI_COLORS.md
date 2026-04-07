# Tài liệu 2: Camera UI & Colors Management

**Phiên bản**: 1.0  
**Ngày**: 2026-04-06  
**Trạng thái**: Design Phase

---

## 1. Tổng Quan

Màn hình camera eKYC cần:
- **Màu cố định**: nền đen (`#000000`), nút/text trắng (`#FFFFFF`)
- **Không phụ thuộc theme**: không thay đổi theo light/dark mode
- **Quản lý màu tập trung**: không hard code mã màu trong UI files

Tài liệu này định nghĩa:
1. File quản lý màu tập trung (`EkycColors.kt`)
2. Các component UI cấu thành camera scene
3. Permission UX flow + message handling
4. Cách sử dụng trong screen code

---

## 2. Kiến Trúc Màu (Centralized Color Management)

### 2.1 File mới: `ui/theme/EkycColors.kt`

```kotlin
// File: ui/theme/EkycColors.kt
package com.example.easymoney.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * eKYC-specific colors (camera & immersive scenes)
 * These are fixed and do NOT follow app theme (light/dark)
 */
object EkycColors {
    // ========== CAMERA SCENE ==========
    // Main backgrounds
    val cameraSceneBackground = Color(0xFF000000)  // Pure black
    val cameraSceneOverlay = Color(0x00000000)     // Transparent (for preview)
    
    // Text & UI elements
    val cameraTextPrimary = Color(0xFFFFFFFF)      // White
    val cameraTextSecondary = Color(0xFFBBBBBB)    // Light gray (secondary text)
    val cameraButtonBackground = Color(0xFFFFFFFF) // White button
    val cameraButtonText = Color(0xFF000000)       // Black text on white button
    val cameraIconColor = Color(0xFFFFFFFF)        // White icons
    
    // Overlays & guides
    val cameraFrameGuide = Color(0xFF4A4A4A)       // Gray frame border
    val cameraFrameGuideFill = Color(0x1A4A4A4A)   // Semi-transparent for guidance
    
    // Messages & feedback
    val cameraErrorText = Color(0xFFFF4444)        // Red for errors
    val cameraWarningText = Color(0xFFFFAA00)      // Orange for warnings
    val cameraSuccessText = Color(0xFF00CC00)      // Green for success
    
    // ========== PERMISSION SCENE ==========
    val permissionBackground = Color(0xFF000000)   // Same as camera
    val permissionText = Color(0xFFFFFFFF)
    val permissionButtonBackground = Color(0xFFFFFFFF)
    val permissionButtonText = Color(0xFF000000)
    
    // ========== RESULT/FAILURE SCENE ==========
    // These follow theme (use MaterialTheme, not fixed)
    // So don't define here - use MaterialTheme.colorScheme in that screen
}

/**
 * Camera scene typography & spacing (can expand as needed)
 */
object EkycDimens {
    val cameraFrameWidthRatio = 0.8f    // Frame occupies 80% of screen width
    val cameraFrameHeightRatio = 0.6f   // Frame occupies 60% of screen height
    val cameraGuideStrokeWidth = 2f     // px
    
    val permissionTitleSize = 20f       // sp
    val permissionMessageSize = 14f     // sp
    val permissionButtonHeight = 52f    // dp
    val permissionButtonWidth = 300f    // dp
}
```

### 2.2 Import & sử dụng ở UI code
```kotlin
import com.example.easymoney.ui.theme.EkycColors
import com.example.easymoney.ui.theme.EkycDimens

// Trong composable:
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(EkycColors.cameraSceneBackground)
) {
    // ...camera preview...
}
```

---

## 3. Camera Scene - Component Breakdown

### 3.1 Hierarchy (tổng thể)

```
CameraSceneContainer (full-screen, black background)
├── CameraPreview (CameraX preview)
├── CameraFrameOverlay (oval/rect guide frame)
├── CameraInstructions (text guidance)
├── PrecheckStatus (real-time feedback)
├── CameraControls (bottom action bar)
│   ├── CaptureButton (large white circle)
│   ├── RetakeButton (optional, after preview)
│   └── CancelButton (top-left, back)
└── Toast/Snackbar (precheck fail messages)
```

### 3.2 Chi tiết từng component

#### **A) CameraSceneContainer**
- Nền: `EkycColors.cameraSceneBackground` (#000000)
- Layout: `fillMaxSize()`, edge-to-edge
- Status bar: dark immersive (handled by AppChromeMode)

#### **B) CameraPreview (CameraX)**
- Preview surface từ CameraX binding
- Aspect ratio: 3:4 hoặc 4:3 (tuỳ device)
- Fill most of screen below frame guide

#### **C) CameraFrameOverlay**
- **Hình dạng**: Oval hoặc rounded rectangle (nên oval cho face matching)
- **Kích thước**: 80% chiều rộng × 60% chiều cao (EkycDimens)
- **Border**: 2px stroke, `EkycColors.cameraFrameGuide`
- **Fill**: `EkycColors.cameraFrameGuideFill` (barely visible, subtle)
- **Position**: Center of screen

```kotlin
// Mô tả UI:
Canvas(modifier = Modifier.fillMaxSize()) {
    // Vẽ frame guide oval
    drawOval(
        color = EkycColors.cameraFrameGuide,
        topLeft = ...,
        size = ...,
        style = Stroke(width = EkycDimens.cameraGuideStrokeWidth)
    )
    // Optional: fill ngoài frame với semi-transparent overlay
}
```

#### **D) CameraInstructions**
- Text: "Chụp ảnh chân dung..." (định center)
- Font size: 16sp
- Color: `EkycColors.cameraTextPrimary`
- Position: Ngay dưới frame guide
- Background: semi-transparent để readable

```kotlin
Text(
    text = "Chụp ảnh chân dung của bạn",
    fontSize = 16.sp,
    color = EkycColors.cameraTextPrimary,
    modifier = Modifier
        .align(Alignment.Center)
        .padding(top = 16.dp)
)
```

#### **E) PrecheckStatus (Real-time feedback)**
- Hiển thị: "Di chuyển gần hơn", "Quay trực tiếp", "Ánh sáng yếu", ...
- Color: theo trạng thái
  - Error: `EkycColors.cameraErrorText` (#FF4444)
  - Warning: `EkycColors.cameraWarningText` (#FFAA00)
  - Normal: `EkycColors.cameraTextSecondary` (#BBBBBB)
- Position: Bottom-center, above button bar
- Animation: Fade in/out khi message thay đổi

```kotlin
AnimatedVisibility(visible = precheckMessage != null) {
    Text(
        text = precheckMessage ?: "",
        color = precheckMessageColor,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
```

#### **F) CameraControls (Bottom action bar)**

**Capture Button** (nút chụp chính):
- Hình dạng: Circle
- Size: 80dp diameter
- Background: `EkycColors.cameraButtonBackground` (#FFFFFF)
- Icon: Camera icon màu `EkycColors.cameraButtonText` (#000000)
- Position: Bottom center
- Enabled only when precheck passes

```kotlin
FloatingActionButton(
    onClick = { onCapture() },
    enabled = precheckPassed,
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp),
    containerColor = EkycColors.cameraButtonBackground,
    contentColor = EkycColors.cameraButtonText
) {
    Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
}
```

**Back Button** (hủy, quay lại):
- Hình dạng: Square or circular
- Size: 48dp
- Background: Transparent (show icon only)
- Icon: Back/X màu `EkycColors.cameraIconColor`
- Position: Top-left
- Always visible

```kotlin
IconButton(
    onClick = { onCancel() },
    modifier = Modifier
        .align(Alignment.TopStart)
        .padding(8.dp),
    colors = IconButtonDefaults.iconButtonColors(
        contentColor = EkycColors.cameraIconColor
    )
) {
    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
}
```

---

## 4. Permission UX Flow & Messages

### 4.1 Permission Request Flow

```
Screen Enter
    ↓
Check CAMERA permission status
    ├── GRANTED → Show camera
    ├── DENIED → Show "Ask again" UI
    ├── PERMANENTLY_DENIED → Show "Open Settings" UI
    └── NOT_ASKED (first time) → Request permission
```

### 4.2 Message & UI Mapping

| State | Message | Action | Color |
|-------|---------|--------|-------|
| `Not Asked` | (システムダイアログ) | Auto-request | N/A |
| `Denied (1st)` | "App cần quyền camera. Cho phép?" | Button: "Cho phép" | Primary |
| `Denied (2nd)` | "Vui lòng cấp quyền camera trong Cài đặt" | Button: "Mở Cài đặt" | Primary |
| `Granted` | (ẩn, show camera) | N/A | N/A |
| `Granted + No Lens` | "Thiết bị không có camera" | Button: "Quay lại" | Error |

### 4.3 Permission UI Screen (khi denied)

```
DeniedPermissionScreen
├── Icon (camera với X đỏ)
├── Title: "Cần quyền camera"
├── Message: "Ứng dụng cần quyền truy cập camera để chụp ảnh."
├── Button: "Cấp quyền" (primary color)
│   └── onClick: requestPermission()
└── Button: "Quay lại" (secondary)
    └── onClick: onCancel()
```

**Code pseudo**:
```kotlin
@Composable
fun CameraPermissionDeniedScreen(
    onRequestPermission: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EkycColors.permissionBackground)
            .padding(16.dp),
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
            text = "Ứng dụng cần quyền truy cập camera để chụp ảnh.",
            fontSize = 14.sp,
            color = EkycColors.cameraTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(EkycDimens.permissionButtonHeight.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EkycColors.permissionButtonBackground,
                contentColor = EkycColors.permissionButtonText
            )
        ) {
            Text("Cấp quyền")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(EkycDimens.permissionButtonHeight.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = EkycColors.permissionText
            ),
            border = BorderStroke(1.dp, EkycColors.permissionText)
        ) {
            Text("Quay lại")
        }
    }
}
```

---

## 5. Precheck Feedback Messages

Khi ML precheck fail, hiển thị message inline (không dialog):

| Reason Code | Message | Icon/Color | Duration |
|-------------|---------|-----------|----------|
| `NO_FACE` | "Không phát hiện khuôn mặt" | ⚠️ Warning | 2s |
| `MULTIPLE_FACES` | "Chỉ một người trong khung" | ⚠️ Warning | 2s |
| `FACE_TOO_SMALL` | "Di chuyển gần hơn" | ℹ️ Info | 2s |
| `FACE_OUT_OF_FRAME` | "Đặt khuôn mặt vào khung" | ℹ️ Info | 2s |
| `FACE_TILTED` | "Quay trực tiếp phía trước" | ℹ️ Info | 2s |
| `LOW_LIGHT` | "Ánh sáng yếu, di chuyển đến nơi sáng hơn" | ⚠️ Warning | 2s |
| `BLURRY_IMAGE` | "Giữ yên, ảnh bị mờ" | ⚠️ Warning | 2s |

---

## 6. File Structure & Organization

```
ui/
├── theme/
│   ├── Color.kt (app theme colors)
│   ├── EkycColors.kt ✨ NEW (camera/ekyc fixed colors)
│   └── Typography.kt
├── loan/
│   └── information/
│       └── ekyc/
│           ├── EkycFaceCaptureScreen.kt (main screen)
│           ├── EkycCameraPreview.kt (camera + preview components)
│           ├── EkycPermissionScreen.kt (denied permission UI)
│           ├── EkycPrecheckStatus.kt (feedback messages)
│           └── EkycCameraViewModel.kt (state + logic)
```

---

## 7. Color Reference Quick Lookup

| Usage | Color Var | Hex | Purpose |
|-------|-----------|-----|---------|
| Main BG | `cameraSceneBackground` | #000000 | Camera scene background |
| Main Text | `cameraTextPrimary` | #FFFFFF | Primary text (white) |
| Secondary Text | `cameraTextSecondary` | #BBBBBB | Hints, secondary messages |
| Frame Guide | `cameraFrameGuide` | #4A4A4A | Face frame border |
| Button BG | `cameraButtonBackground` | #FFFFFF | Capture button |
| Button Text | `cameraButtonText` | #000000 | Button label |
| Error Message | `cameraErrorText` | #FF4444 | Error feedback |
| Warning Message | `cameraWarningText` | #FFAA00 | Precheck warnings |
| Icon Color | `cameraIconColor` | #FFFFFF | UI icons |

---

## 8. Import Statements (Ready to use)

```kotlin
import com.example.easymoney.ui.theme.EkycColors
import com.example.easymoney.ui.theme.EkycDimens
```

---

## 9. Usage Example (Full Screen)

```kotlin
@Composable
fun EkycFaceCaptureScreen() {
    // Register top bar override (from Tài liệu 1)
    RegisterTopBarOverride(
        ownerRoute = "ekyc_face_capture",
        override = AppTopBarOverride(
            topBarMode = TopBarMode.HIDDEN,
            systemBarMode = SystemBarMode.CAMERA_DARK_IMMERSIVE,
            screenColorMode = ScreenColorMode.FIXED_CAMERA_BLACK
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EkycColors.cameraSceneBackground)
    ) {
        // Camera preview
        CameraPreview(
            modifier = Modifier.fillMaxSize()
        )
        
        // Frame guide overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                color = EkycColors.cameraFrameGuide,
                topLeft = Offset(...),
                size = Size(...),
                style = Stroke(width = EkycDimens.cameraGuideStrokeWidth)
            )
        }
        
        // Instructions
        Text(
            text = "Chụp ảnh chân dung của bạn",
            color = EkycColors.cameraTextPrimary,
            // ...
        )
        
        // Precheck feedback
        if (precheckMessage != null) {
            Text(
                text = precheckMessage,
                color = precheckMessageColor, // from EkycColors
                // ...
            )
        }
        
        // Back button
        IconButton(
            onClick = { onCancel() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = EkycColors.cameraIconColor
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        
        // Capture button
        FloatingActionButton(
            onClick = { onCapture() },
            enabled = precheckPassed,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            containerColor = EkycColors.cameraButtonBackground,
            contentColor = EkycColors.cameraButtonText
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
        }
    }
}
```

---

## 10. Next Steps

1. **Create** `EkycColors.kt` + `EkycDimens.kt`
2. **Create** Camera scene components (screens & UI)
3. **Integrate** with permission handling (runtime permissions)
4. **Test** colors + layout on device

---

**Document Version**: 1.0  
**Scope**: Camera UI design + color management  
**Dependencies**: AppChromeMode (Tài liệu 1)  
**Ready for**: Implementation (Tài liệu 3)

