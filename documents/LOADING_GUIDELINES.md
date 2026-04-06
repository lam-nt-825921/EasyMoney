# Hướng Dẫn Loading/Skeleton - Best Practice Cho EasyMoney App

## 1. Nguyên Tắc Cơ Bản

### 1.1. Một Instance, Một Composable
- **Đúng**: Gọi `MainContent()` **một lần duy nhất**, dùng `isLoading` parameter để nó tự render skeleton vs content.
- **Sai**: Dùng `LoadStateRenderer` hay closure wrapper khác nhau tạo **multiple instance** của cùng composable.

**Lý do**: Khi có nhiều instance, Compose coi chúng là các composable khác nhau → local state bị mất (scroll, modal, v.v) khi chuyển giữa loading/content.

### 1.2. Giữ Shell Ổn Định
- `Scaffold` / top bar / bottom bar / layout cơ bản phải **cố định**, không đổi owner.
- Chỉ thay nội dung body giữa loading và content.
- Tránh đổi cấu trúc lớn gây recompose toàn màn.

### 1.3. Loading Phải Xuất Hiện Ngay Frame Đầu
- State mặc định khi vào màn phải là `InitialLoading`.
- Không được chờ data mới render skeleton.
- Skeleton/placeholder dùng tĩnh (không shimmer infinite nặng).

---

## 2. State Definition

### 2.1. Load State Enum (Riêng cho mỗi screen)
Ví dụ `ConfirmInfoLoadState`:
```kotlin
sealed class ConfirmInfoLoadState {
    data object InitialLoading : ConfirmInfoLoadState()   // Lần đầu load
    data object Loading : ConfirmInfoLoadState()          // Reload dữ liệu cũ
    data object Success : ConfirmInfoLoadState()          // Đã có dữ liệu
    data class Error(val message: String) : ConfirmInfoLoadState()
}
```

### 2.2. UiState Default
```kotlin
data class ConfirmInfoUiState(
    val userInfo: MyInfoModel? = null,
    val loadState: ConfirmInfoLoadState = ConfirmInfoLoadState.InitialLoading  // ← Luôn InitialLoading
)
```

### 2.3. ViewModel Logic
Khi load dữ liệu:
```kotlin
fun loadMyInfo() {
    _uiState.update {
        val nextLoadState = if (it.userInfo == null) {
            ConfirmInfoLoadState.InitialLoading    // Lần đầu
        } else {
            ConfirmInfoLoadState.Loading           // Reload
        }
        it.copy(loadState = nextLoadState)
    }
    // ... gọi API
}
```

---

## 3. Screen Composable Pattern

### 3.1. Single-Instance Approach (✓ Recommended)

```kotlin
@Composable
fun MyScreen(
    uiState: MyUiState,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = uiState.loadState != MyLoadState.Success && 
                    uiState.loadState != MyLoadState.Error
    
    // ✓ Gọi một lần duy nhất với isLoading parameter
    MyContent(
        uiState = uiState,
        onAction = onAction,
        isLoading = isLoading,
        modifier = modifier
    )
}

@Composable
fun MyContent(
    uiState: MyUiState,
    onAction: (Action) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(...) {
        Column(...) {
            if (isLoading) {
                // Skeleton tĩnh, giữ layout giống content thật
                MyLoadingSkeleton()
            } else {
                // Content thật
                MyDataBody(uiState, onAction)
            }
        }
    }
}
```

### 3.2. Tránh Multiple-Closure Pattern (✗ Sai)

```kotlin
// ✗ KHÔNG LÀM CÁI NÀY - Tạo multiple instance
LoadStateRenderer(
    initialLoading = { MyContent(..., isLoading=true) },   // Instance 1
    content = { MyContent(..., isLoading=false) },         // Instance 2
)
```

---

## 4. Skeleton/Loading UI Definition

### 4.1. Giữ Layout Giống Content Thật
```kotlin
@Composable
fun MyLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Mỗi row skeleton giống layout content thật
        SkeletonBlock(height = 24.dp)       // Giống title height
        SkeletonBlock(height = 100.dp)      // Giống input field height
        SkeletonBlock(height = 50.dp)       // Giống button height
    }
}
```

### 4.2. Skeleton Phải Tĩnh (Static)
- Dùng `SkeletonBlock` từ `ui/common/loading/SkeletonPrimitives.kt`
- **Không** dùng shimmer infinite nếu không cần.
- Nếu dùng shimmer, **chỉ ở vùng nhỏ**, không phủ full màn.

---

## 5. Triển Khai Từng Màn Hình

### Bước 1: Xác định Load State Class
```kotlin
// data/uistate/MyUiState.kt
sealed class MyLoadState {
    data object InitialLoading : MyLoadState()
    data object Loading : MyLoadState()
    data object Success : MyLoadState()
    data class Error(val message: String) : MyLoadState()
}
```

### Bước 2: ViewModel Logic
```kotlin
// ViewModel
private fun loadData() {
    _uiState.update {
        it.copy(loadState = if (it.data == null) InitialLoading else Loading)
    }
    // Gọi API, cập nhật state
}
```

### Bước 3: Screen Composable
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    MyContent(
        uiState = uiState,
        isLoading = uiState.loadState !in listOf(Success, Error),
        ...
    )
}
```

### Bước 4: Content Composable
```kotlin
@Composable
fun MyContent(
    uiState: MyUiState,
    isLoading: Boolean,
    ...
) {
    Scaffold {
        if (isLoading) MyLoadingSkeleton()
        else MyDataBody(uiState, ...)
    }
}
```

---

## 6. Checklist Triển Khai

- [ ] Load state enum định nghĩa rõ (InitialLoading, Loading, Success, Error)
- [ ] UiState default là `InitialLoading`
- [ ] Screen call content **một lần duy nhất**
- [ ] Content dùng `if (isLoading) skeleton else data`
- [ ] Skeleton layout giống content thật
- [ ] Không dùng `LoadStateRenderer` hay multiple closure wrapper
- [ ] Test: loading → data → kiểm tra state không bị mất
- [ ] Test: scroll, modal, v.v giữ nguyên khi load xong

---

## 7. Tài Liệu Tham Khảo Trong Project

- `ui/common/loading/UiLoadState.kt` - định nghĩa trạng thái chung
- `ui/common/loading/SkeletonPrimitives.kt` - primitives skeleton
- `ui/common/loading/Shimmer.kt` - shimmer effect (dùng có kiểm soát)
- `ui/confirmation/ConfirmInfoUiState.kt` - ví dụ UiState + LoadState
- `ui/loan/LoanUiState.kt` - ví dụ UiState + LoadState

---

## 8. Khi Áp Dụng Cho Màn Khác

Copy template này:
1. Định nghĩa load state enum nếu chưa có
2. Screen -> Content gọi **một lần duy nhất** với `isLoading`
3. Content dùng `if (isLoading) skeleton else data`
4. Kiểm tra compile + test trên device
5. Xong ✓

**Đừng dùng**: `LoadStateRenderer`, closure wrapper, hay bất kỳ cách nào tạo multiple instance.

