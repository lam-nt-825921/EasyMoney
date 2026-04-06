# Tài liệu 1: NavBar & App Chrome Mode (System Bar + Top Bar Control)

**Phiên bản**: 1.0  
**Ngày**: 2026-04-06  
**Trạng thái**: Design Phase

---

## 1. Tổng Quan

Hiện tại `AppRoot.kt` quản lý top bar theo hardcode logic:
- Home: không hiển thị top bar
- Các route khác: hiển thị top bar với title + back/help buttons

**Yêu cầu**: Hỗ trợ các màn hình đặc biệt (camera eKYC, thất bại, ...) có:
- Top bar ẩn hoàn toàn
- Top bar không title (nhưng vẫn có back)
- System bars (status/nav) đen/transparent (immersive)
- Nền màn hình cố định (không theo theme)

**Giải pháp**: Cơ chế **3 modes** quản lý "App Chrome" (một cách gọi chung cho system bars + top bar):
1. **TopBarMode**: STANDARD, NO_TITLE, HIDDEN
2. **SystemBarMode**: THEME_DEFAULT, CAMERA_DARK_IMMERSIVE
3. **ScreenColorMode**: THEME_AWARE, FIXED_CAMERA_BLACK

---

## 2. Kiến Trúc & Thay Đổi Cần Làm

### 2.1 File cần tạo/sửa

| File | Thay Đổi | Chi tiết |
|------|----------|---------|
| `ui/components/AppChromeMode.kt` | ✨ Tạo mới | Enums & data classes cho 3 modes |
| `ui/components/AppTopBarController.kt` | 🔧 Sửa | Mở rộng `AppTopBarOverride` thêm mode metadata |
| `ui/AppRoot.kt` | 🔧 Sửa | Đọc mode từ override, apply system bars, skip top bar nếu HIDDEN |
| `navigation/AppDestination.kt` | 🔧 Sửa | Thêm default mode metadata vào sealed class |

### 2.2 Chi tiết từng bước

#### Bước 1: Tạo `AppChromeMode.kt` (file mới)
```kotlin
// File: ui/components/AppChromeMode.kt
enum class TopBarMode {
    STANDARD,      // Hiện title + back/help theo destination
    NO_TITLE,      // Ẩn title, vẫn có back/help
    HIDDEN         // Ẩn top bar hoàn toàn
}

enum class SystemBarMode {
    THEME_DEFAULT,          // Status/nav bar theo theme hiện tại
    CAMERA_DARK_IMMERSIVE   // Nền đen/transparent, icon sáng, edge-to-edge
}

enum class ScreenColorMode {
    THEME_AWARE,           // Dùng MaterialTheme.colorScheme.background
    FIXED_CAMERA_BLACK     // Nền cố định #000000
}

data class AppChromeConfig(
    val topBarMode: TopBarMode = TopBarMode.STANDARD,
    val systemBarMode: SystemBarMode = SystemBarMode.THEME_DEFAULT,
    val screenColorMode: ScreenColorMode = ScreenColorMode.THEME_AWARE
)
```

#### Bước 2: Mở rộng `AppTopBarOverride` 
Sửa `AppTopBarController.kt`:
```kotlin
data class AppTopBarOverride(
    val title: String? = null,
    val showBackButton: Boolean? = null,
    val showHelpButton: Boolean? = null,
    val onBackClick: (() -> Unit)? = null,
    val onHelpClick: (() -> Unit)? = null,
    val backgroundColor: Color? = null,
    val contentColor: Color? = null,
    // ✨ Thêm 3 dòng này:
    val topBarMode: TopBarMode? = null,
    val systemBarMode: SystemBarMode? = null,
    val screenColorMode: ScreenColorMode? = null
)
```

#### Bước 3: Sửa `AppDestination.kt`
Thêm default modes vào sealed class:
```kotlin
sealed class AppDestination(
    val route: String,
    val title: String,
    val showBackButton: Boolean,
    val showHelpButton: Boolean = true,
    val guideXmlName: String? = null,
    val topBarBackgroundColor: Color? = null,
    val topBarContentColor: Color? = null,
    // ✨ Thêm 3 dòng này:
    val defaultTopBarMode: TopBarMode = TopBarMode.STANDARD,
    val defaultSystemBarMode: SystemBarMode = SystemBarMode.THEME_DEFAULT,
    val defaultScreenColorMode: ScreenColorMode = ScreenColorMode.THEME_AWARE
) {
    // ...existing data objects...
}
```

#### Bước 4: Sửa `AppRoot.kt`
Logic chính: đọc modes từ override hoặc destination default, rồi apply.

---

## 3. Cách Sử Dụng ở Các Screen

### 3.1 Screen Camera eKYC (ví dụ điển hình)
```kotlin
// Trong EkycFaceCaptureScreen (hoặc composite function):
@Composable
fun EkycFaceCaptureScreen(
    navController: NavHostController,
    // ...
) {
    val controller = LocalAppTopBarController.current
    
    RegisterTopBarOverride(
        ownerRoute = "ekyc_face_capture",
        override = AppTopBarOverride(
            topBarMode = TopBarMode.HIDDEN,
            systemBarMode = SystemBarMode.CAMERA_DARK_IMMERSIVE,
            screenColorMode = ScreenColorMode.FIXED_CAMERA_BLACK
        )
    )
    
    // ... camera UI code...
}
```

### 3.2 Screen thông báo thất bại
```kotlin
// Trong thông báo thất bại (vẫn theo theme chuẩn):
RegisterTopBarOverride(
    ownerRoute = "ekyc_result_failure",
    override = AppTopBarOverride(
        topBarMode = TopBarMode.HIDDEN,
        systemBarMode = SystemBarMode.THEME_DEFAULT,
        screenColorMode = ScreenColorMode.THEME_AWARE
    )
)
```

### 3.3 Screen chuẩn (không override)
Tự động dùng default từ `AppDestination` → không cần gọi `RegisterTopBarOverride`.

---

## 4. Checklist Test Các Scene Cũ

Sau khi cập nhật, test các route hiện tại:

- [ ] **Home**
  - Top bar: không hiển thị ✓
  - Bottom bar: vẫn hiển thị ✓
  - Back button: không có (N/A) ✓

- [ ] **Onboarding**
  - Top bar: hiển thị "Vay tổ chức tài chính" ✓
  - Back button: có ✓
  - Help button: có ✓

- [ ] **ConfirmInformation**
  - Top bar: hiển thị "Xác nhận thông tin" ✓
  - Back button: có ✓

- [ ] **LoanFlow** (các step bên trong)
  - Top bar: hiển thị "Thông tin khoản vay" ✓
  - Back button: có ✓
  - Help button: không có (showHelpButton = false) ✓

- [ ] **PageGuide**
  - Top bar: hiển thị "Hướng dẫn" ✓
  - Back button: có ✓

---

## 5. Status Bar & Nav Bar Detail (Implementation Note)

### Để apply System Bar color
Cần dùng:
```kotlin
// Trong AppRoot, khi có systemBarMode == CAMERA_DARK_IMMERSIVE:
SideEffect {
    val window = (context as? Activity)?.window
    window?.statusBarColor = Color.BLACK.toArgb()
    window?.navigationBarColor = Color.BLACK.toArgb()
    // Hoặc transparent nếu muốn edge-to-edge
    // window?.setDecorFitsSystemWindows(false)
    
    // Tùy API level, dùng WindowInsetsControllerCompat để set icon color:
    WindowInsetsControllerCompat(window!!, view).isAppearanceLightStatusBars = false
}
```

---

## 6. Mapping Mode theo Screen

| Screen | TopBarMode | SystemBarMode | ScreenColorMode | Ghi chú |
|--------|-----------|--------------|-----------------|---------|
| Home | N/A (không render top bar) | THEME_DEFAULT | THEME_AWARE | Không override |
| Onboarding | STANDARD | THEME_DEFAULT | THEME_AWARE | Default |
| ConfirmInformation | STANDARD | THEME_DEFAULT | THEME_AWARE | Default |
| LoanFlow | STANDARD | THEME_DEFAULT | THEME_AWARE | Default |
| PageGuide | STANDARD | THEME_DEFAULT | THEME_AWARE | Default |
| **eKYC Camera** | **HIDDEN** | **CAMERA_DARK_IMMERSIVE** | **FIXED_CAMERA_BLACK** | ✨ Override |
| **eKYC Result Fail** | **HIDDEN** | **THEME_DEFAULT** | **THEME_AWARE** | ✨ Override |
| **eKYC Permission** | **NO_TITLE** (hoặc HIDDEN) | **CAMERA_DARK_IMMERSIVE** | **FIXED_CAMERA_BLACK** | ✨ Override |

---

## 7. Code Changes Detailed

### 7.1 Di chuyển logic `topBar` conditional từ `AppRoot.kt`

**Trước** (hardcode):
```kotlin
topBar = {
    if (destination != AppDestination.Home) {
        val topBarOverride = if (topBarController.ownerRoute == destination.route) {
            topBarController.topBarOverride
        } else {
            null
        }
        val resolvedTitle = topBarOverride?.title ?: destination.title
        // ... render AppNavigationBar
    }
}
```

**Sau** (sử dụng mode):
```kotlin
val chromeConfig = resolveChromeConfig(
    destination = destination,
    override = topBarOverride,
    canNavigateBack = canNavigateBack
)

topBar = {
    if (chromeConfig.topBarMode != TopBarMode.HIDDEN) {
        // Render AppNavigationBar
    }
}
```

---

## 8. Quy Trình Triển Khai Chi Tiết

1. **Tạo file** `AppChromeMode.kt` (enums + data classes)
2. **Cập nhật** `AppTopBarController.kt` (thêm field vào AppTopBarOverride)
3. **Cập nhật** `AppDestination.kt` (thêm default modes)
4. **Cập nhật** `AppNavigationBar.kt` (dùng TopBarMode để quyết định render title)
5. **Cập nhật** `AppRoot.kt` (logic áp dụng modes)
6. **Test** các route hiện tại (xem checklist ở mục 4)
7. **Sẵn sàng** cho các screen eKYC dùng override modes mới

---

## 9. Notes & Q&A

**Q**: Tại sao không hardcode mode vào route string?  
**A**: Hardcode khó maintain, dễ sai. Dùng data class rõ ràng hơn.

**Q**: Có cần handle rotation?  
**A**: CameraX sẽ handle. System bar color sẽ giữ qua rotation.

**Q**: Test trên API level thấp (< 21)?  
**A**: Status/nav bar color cần check, có thể fallback sang default theme color.

---

Tài liệu này đủ để dev bắt tay implement. Bước tiếp theo: **Tài liệu 2: Camera UI Colors & Components**.

