# CLAUDE.md — EasyMoney

Android app mô phỏng luồng vay vốn tổ chức tài chính. Viết bằng Kotlin, Jetpack Compose, single-Activity architecture.

---

## Tech Stack

| Thành phần | Thư viện |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose + NavHost |
| DI | Hilt (`hilt-android:2.51.1`) |
| Networking | Retrofit + OkHttp + Gson |
| State | StateFlow / ViewModel |
| Storage | DataStore Preferences |
| Min SDK | 24 / Target SDK 35 |

---

## Cấu trúc Package

```
com.example.easymoney/
├── data/
│   ├── remote/          # Retrofit service, API factory, remote data source
│   └── repository/      # LoanRepositoryImpl — cài đặt interface từ domain
├── domain/
│   ├── common/          # Resource<T> — wrapper kết quả (Success/Error/Loading)
│   ├── model/           # LoanPackageModel, MyInfoModel — domain model thuần Kotlin
│   └── repository/      # LoanRepository (interface), LoanRepositoryImpl, LoanRepositoryModule
├── navigation/          # AppDestination, AppNavHost, AppState
├── ui/
│   ├── components/      # AppNavigationBar, HomeBottomBar — dùng chung toàn app
│   ├── theme/           # Color, Type, Theme
│   ├── onboarding/      # OnboardingScreen + OnboardingViewModel
│   ├── home/            # HomeScreen + components
│   ├── history/         # TransactionHistoryScreen — tab "Lịch sử giao dịch"
│   ├── notification/    # NotificationScreen (tabs: Biến động số dư / Khuyến mại / Nhắc nhở)
│   ├── account/         # AccountScreen — tab "Tài khoản"
│   ├── loan/            # LoanViewModel, LoanUiState, LoanUtils
│   │   └── configuration/  # LoanConfigurationScreen, TenorBottomSheet, LoanBreakdownBottomSheet
│   ├── confirmation/    # ConfirmInfoScreen, ConfirmInfoViewModel, ConfirmInfoUiState
│   └── guide/           # PageGuideScreen
├── EasyMoneyApplication.kt
└── MainActivity.kt
```

---

## Quy ước quan trọng

### Model theo Layer
- `<Tên>Dto` → `data/` — dùng cho Retrofit/Room
- `<Tên>Model` → `domain/model/` — thuần Kotlin, không phụ thuộc Android
- `<Tên>UiState` → trong package của từng UI — chỉ tạo khi domain model chưa đủ thông tin cho UI

### Repository
- Khai báo hàm mới ở `LoanRepository` (interface)
- Cài đặt ở `LoanRepositoryImpl`
- Không trả về mock trực tiếp ở ViewModel — mock data phải nằm trong `LoanRepositoryImpl`

### ViewModel + Hilt
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel()

// Tại Composable:
val viewModel: MyViewModel = hiltViewModel()
```

### Navigation
- Tất cả route khai báo tập trung ở `AppDestination` (sealed class)
- Mỗi `AppDestination` có: `route`, `title`, `showBackButton`, cấu hình TopBar
- Đăng ký màn hình mới trong `AppNavHost`

### Bottom Tab Navigation
- 4 tab chính: `Home`, `TransactionHistory`, `Notifications`, `Account`
- Đánh dấu `isMainTab = true` trên `AppDestination` để hiện `HomeBottomBar`
- Tab chính KHÔNG có back button (`showBackButton = false`) và KHÔNG có help button (`showHelpButton = false`)
- `AppRoot` dùng `destination.isMainTab` để quyết định hiện/ẩn bottom bar
- Navigation giữa các tab dùng `popUpTo(Home) { saveState = true }` + `launchSingleTop = true` để giữ state

---

## Màu sắc chính (Theme)

| Tên | Hex | Dùng cho |
|---|---|---|
| TealPrimary | `#137A91` | Primary action, selected state, icon tint, button, tab indicator |
| TealSecondary | `#E8F4F6` | Icon background (teal), tab indicator background, card accent |
| OrangeWarning | `#FDB022` | Warning, badge |
| TextPrimary | `#1D2939` | Tiêu đề, nội dung chính |
| TextSecondary | `#667085` | Nhãn phụ, placeholder, timestamp |
| BorderColor | `#EAECF0` | Divider, border |
| Background | `#F5F7FA` | Nền màn hình (screen background) |
| White | `#FFFFFF` | Card, bottom bar, top bar |
| SuccessGreen | `#2E7D32` | Số tiền dương (credit) |
| ErrorRed | `#C62828` | Số tiền âm (debit), lỗi |
| UnselectedNav | `#98A2B3` | Icon/label bottom bar chưa chọn |

### Quy tắc màu bottom bar
- **Selected**: `TealPrimary` (#137A91) — icon + label + indicator background = TealSecondary
- **Unselected**: `#98A2B3`
- **KHÔNG dùng màu đỏ** cho bất kỳ trạng thái active nào — màu chủ đạo là teal

---

## Luồng màn hình hiện tại

```
Bottom Tab Bar
├── Home → Onboarding → LoanInformation (LoanConfiguration) → ConfirmInformation
│                                                           → PageGuide (hướng dẫn)
├── TransactionHistory   (lịch sử giao dịch — mock data)
├── Notifications        (3 tabs: Biến động số dư / Khuyến mại / Nhắc nhở — mock data)
└── Account              (thông tin tài khoản, menu cài đặt — mock data)
```

---

## Quy tắc Kotlin trong project

- Ưu tiên `val` hơn `var`; dùng `data class` + `.copy()` cho cập nhật state
- Không dùng `!!` — dùng `?.`, `?:`, hoặc `requireNotNull()`
- Sealed class/interface cho state: `Loading`, `Success`, `Error`
- Tránh lồng scope function quá 2 cấp
- Không hardcode key/token — dùng `local.properties` hoặc `BuildConfig`

---

## Quy trình thêm màn hình mới

1. Thêm `data object <Tên>` vào `AppDestination`
2. Tạo package `ui/<tên>/` với:
   - `<Tên>Screen.kt` — Composable UI
   - `<Tên>ViewModel.kt` — `@HiltViewModel`
   - `<Tên>UiState.kt` — nếu cần
3. Đăng ký route trong `AppNavHost`
4. Nếu cần dữ liệu: khai báo hàm ở `LoanRepository` → cài đặt ở `LoanRepositoryImpl`

---

## Quy trình Build & Test

```bash
# Build
./gradlew assembleDebug

# Unit test
./gradlew test

# Instrumented test
./gradlew connectedAndroidTest
```

### Skills phù hợp
- `/kotlin-review` — review code Kotlin/Compose
- `/kotlin-test` — viết test theo TDD
- `/kotlin-build` — sửa lỗi build/Gradle
- `/gradle-build` — sửa lỗi Gradle/dependency
- `/android-clean-architecture` — pattern Clean Architecture cho Android

---

## Chiến lược tiết kiệm token

### Nguyên tắc đọc file
- Đọc file cụ thể bằng đường dẫn trực tiếp (không dùng agent Explore cho file đã biết)
- Dùng `offset` + `limit` khi chỉ cần một phần file lớn
- Dùng `Grep` thay vì đọc toàn bộ file khi chỉ cần tìm symbol/pattern
- Không đọc lại file đã có trong context của conversation hiện tại

### Nguyên tắc tool call
- Gộp các tool call **độc lập** vào một lượt (parallel) — ví dụ: đọc 3 file cùng lúc
- Chỉ dùng `Agent(Explore)` khi không biết file nào cần đọc (open-ended search)
- Tránh dùng agent cho task đơn giản có thể xử lý trực tiếp

### Nguyên tắc implement
- Với UI screen đơn giản (static/mock data): implement trực tiếp, không cần planner agent
- Với feature phức tạp (repository, usecase, async): dùng planner trước
- Chỉ đọc file liên quan trực tiếp đến task — không đọc toàn bộ codebase

### Cung cấp context rõ ràng khi prompt
Để giảm token tìm kiếm, khi yêu cầu thay đổi hãy chỉ rõ:
- **File cụ thể** cần sửa (nếu biết)
- **Màn hình/feature** nào bị ảnh hưởng
- **Behavior mong muốn** vs behavior hiện tại

---

## Tài liệu nội bộ

| File | Nội dung |
|---|---|
| `documents/CREATE_REPOSITORY_PLAN.md` | Quy ước Repository + Hilt ViewModel |
| `documents/CLEAN_ARCHITECTURE.md` | Phân chia model 3 layer |
| `documents/ONBOARDING_README.md` | Chi tiết màn hình Onboarding |
