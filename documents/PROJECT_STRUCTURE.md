# PROJECT_STRUCTURE — EasyMoney

Hướng dẫn cấu trúc dự án cho dev mới: package ownership, route ownership, naming convention, nơi đặt state/UI/business logic, anti-patterns cần tránh.

---

## 1. Cây package + mô tả

```
com.example.easymoney/
├── EasyMoneyApplication.kt   # @HiltAndroidApp — entry application, FCM init
├── MainActivity.kt           # Single Activity, mount AppRoot composable
│
├── data/                     # Layer 1: Data sources
│   ├── remote/               # Retrofit service, DTO, RemoteDataSource
│   │   ├── LoanApiService.kt        # Retrofit interface (toàn bộ endpoint)
│   │   ├── LoanApiFactory.kt        # Factory tạo Retrofit client (đọc baseUrl từ AppPreferences)
│   │   ├── LoanRemoteDataSource.kt  # Wrap apiService, map ApiResponse → Resource
│   │   └── dto/                     # DTO files (MetadataDto, etc.)
│   ├── local/                # Room DB, DAO, Entity, AppPreferences
│   │   ├── AppPreferences.kt        # SharedPrefs wrapper: dataSourceMode, baseUrl, tokens
│   │   ├── EasyMoneyDatabase.kt     # @Database
│   │   ├── dao/                     # NotificationDao, AccountDao, RememberedAccountDao
│   │   └── entity/                  # NotificationEntity, AccountEntity, ...
│   └── sample/               # Mock data tĩnh dùng cho MOCK mode (sample list)
│
├── di/                       # Hilt modules
│   ├── NetworkModule.kt      # Provide Retrofit + OkHttp + Gson
│   ├── DatabaseModule.kt     # Provide Room DB + DAO
│   └── RepositoryModule.kt   # Bind interface → impl (@Binds)
│
├── domain/                   # Layer 2: Business logic
│   ├── common/Resource.kt    # Sealed: Loading | Success(data) | Error(msg)
│   ├── model/                # Domain models thuần Kotlin (KHÔNG depend Android)
│   │   ├── AuthModels.kt, LoanApplicationModels.kt, MasterDataMetadata.kt, ...
│   └── repository/           # Interface + impl
│       ├── LoanRepository.kt       (interface)
│       ├── LoanRepositoryImpl.kt   (mock + remote branching)
│       └── ...
│
├── messaging/                # FCM
│   └── EasyMoneyMessagingService.kt
│
├── navigation/               # Compose Navigation
│   ├── AppDestination.kt     # SEALED CLASS — toàn bộ route + metadata top bar/bottom bar
│   ├── AppNavHost.kt         # NavHost — mapping route → composable
│   └── AppRoot.kt            # Scaffold + TopBar + BottomBar + permissions
│
├── ui/                       # Layer 3: UI Compose
│   ├── components/           # Shared components (TopBar, HomeBottomBar, AppNavigationBar)
│   ├── theme/                # Color.kt, Type.kt, Theme.kt
│   ├── login/                # welcome, login_1, register_1, quick_login_1
│   ├── onboarding/
│   ├── home/
│   ├── history/              # Tab Lịch sử giao dịch
│   ├── notification/         # Tab Thông báo (3 sub-tab) + AppNotificationManager
│   ├── account/              # Tab Tài khoản + sub-screens (profile, settings)
│   ├── loan/                 # Discovery, list, detail, configuration, eKYC
│   ├── confirmation/         # Xác nhận thông tin trước khi gửi
│   ├── esign/                # Luồng ký hợp đồng
│   ├── payment/              # Quản lý nguồn tiền, payment cards
│   ├── reward/               # Đổi điểm
│   ├── security/             # Bảo mật tài khoản
│   ├── sandbox/              # Dev tool — toggle MOCK/REMOTE
│   ├── guide/                # PageGuideScreen (XML overlay)
│   └── common/               # Shared composables phức tạp (FaceCaptureModule, etc.)
│
└── utils/                    # Helper functions, không depend layer khác
```

---

## 2. Route ownership

Tất cả route khai báo trong `navigation/AppDestination.kt`. Mỗi route ↔ 1 `data object` ↔ 1 màn hình.

| Route | Data object | Screen file | ViewModel | Repository |
|---|---|---|---|---|
| `welcome` | `Welcome` | `ui/login/WelcomeScreen.kt` | — | — |
| `login_1` | `Login1` | `ui/login/Login1Screen.kt` | `Login1ViewModel` | `LoanRepository` |
| `register_1` | `Register1` | `ui/login/Register1Screen.kt` | `Register1ViewModel` | `LoanRepository` |
| `quick_login_1` | `QuickLogin1` | `ui/login/QuickLogin1Screen.kt` | `QuickLogin1ViewModel` | `LoanRepository` |
| `onboarding` | `Onboarding` | `ui/onboarding/OnboardingScreen.kt` | `OnboardingViewModel` | — |
| `home` | `Home` (isMainTab) | `ui/home/HomeScreen.kt` | `HomeViewModel` | `HomeRepository` |
| `history` | `TransactionHistory` (isMainTab) | `ui/history/...` | `TransactionHistoryViewModel` | `LoanRepository` |
| `notifications` | `Notifications` (isMainTab) | `ui/notification/NotificationScreen.kt` | `NotificationViewModel` | `NotificationRepository` |
| `account` | `Account` (isMainTab) | `ui/account/AccountScreen.kt` | `AccountViewModel` | `AccountRepository`, `UserRepository` |
| `loan_information` | `LoanFlow` | `ui/loan/information/...` | `LoanFlowViewModel` | `LoanRepository` |
| `loan_list` | `LoanList` | `ui/loan/list/LoanListScreen.kt` | `LoanListViewModel` | `LoanRepository` |
| `loan_detail/{id}` | `LoanDetail` | `ui/loan/detail/LoanDetailScreen.kt` | `LoanDetailViewModel` | `LoanRepository` |
| `confirm_information` | `ConfirmInformation` | `ui/confirmation/...` | `ConfirmInformationViewModel` | `LoanRepository` |
| `identity_verification` | `IdentityVerification` | `ui/loan/information/ekyc/...` | `IdentityVerificationViewModel`, `EkycCameraViewModel` | `LoanRepository` |
| `event_detail/{id}` | `EventDetail` | `ui/home/event/EventDetailScreen.kt` | `EventDetailViewModel` | `EventRepository` |
| `rewards` | `Rewards` | `ui/reward/RewardScreen.kt` | `RewardViewModel` | `RewardRepository` |
| `profile` | `Profile` | `ui/account/profile/...` | `ProfileViewModel` | `UserRepository` |
| `edit_personal_info` | `EditPersonalInfo` | `ui/account/profile/edit/...` | `EditPersonalInfoViewModel` | `UserRepository`, `LoanRepository` (master) |
| `edit_job_info` | `EditJobInfo` | `ui/account/profile/edit/...` | `EditJobInfoViewModel` | `UserRepository` |
| `edit_contact_info` | `EditContactInfo` | `ui/account/profile/edit/...` | `EditContactInfoViewModel` | `UserRepository` |
| `money_management` | `MoneyManagement` | `ui/payment/MoneyManagementScreen.kt` | `MoneyManagementViewModel` | `PaymentRepository` |
| `payment_cards` | `PaymentCards` | `ui/payment/PaymentCardsScreen.kt` | `PaymentCardsViewModel` | `PaymentRepository` |
| `general_settings` | `GeneralSettings` | `ui/account/settings/...` | `GeneralSettingsViewModel` | `AppPreferences` |
| `security_settings` | `SecuritySettings` | `ui/security/...` | `SecuritySettingsViewModel` | `UserRepository` |
| `chatbot` | `ChatBot` | (chưa tồn tại — workflow #16) | (chưa) | (chưa) |
| `sandbox` | `Sandbox` | `ui/sandbox/SandBoxScreen.kt` | `SandBoxViewModel` | `NotificationRepository` |
| `contract` | `Contract` | `ui/esign/ContractScreen.kt` | `ContractViewModel` | `LoanRepository` |
| `esign_success` | `EsignSuccess` | `ui/esign/EsignSuccessScreen.kt` | — | — |
| `page_guide?xml={xml}` | `PageGuide` | `ui/guide/PageGuideScreen.kt` | — (load XML) | — |

---

## 3. Naming convention

### File / Class
- `<X>Screen.kt` — Composable screen entrypoint, function `fun <X>Screen(viewModel: <X>ViewModel = hiltViewModel(), ...)`
- `<X>ViewModel.kt` — `@HiltViewModel class <X>ViewModel @Inject constructor(...) : ViewModel()`
- `<X>UiState.kt` — `data class <X>UiState(...)` (chỉ tạo khi cần state phức tạp)
- `<X>Dto.kt` — `data class <X>Dto(...)` cho Retrofit
- `<X>Entity.kt` — `@Entity data class <X>Entity(...)` cho Room
- `<X>Model.kt` hoặc `<X>.kt` — domain model
- `<X>Repository.kt` (interface) + `<X>RepositoryImpl.kt`
- `<X>Dao.kt` cho Room DAO
- `<X>RemoteDataSource.kt` cho remote layer

### Function
- ViewModel public function: `fun onLoadData()`, `fun onSubmit()` — prefix `on` cho event từ UI
- Composable: PascalCase — `fun LoanCard(...)`, `fun PrimaryButton(...)`
- Repository: `suspend fun getLoanList()`, `fun observeNotifications(): Flow<...>`

### State
- `Loading | Success(data) | Error(message)` — dùng sealed class `Resource<T>` hoặc tự định nghĩa cho UI

### Constant
- `SCREAMING_SNAKE_CASE` cho `const val`
- `private companion object { const val TAG = "..." }` cho tag log

---

## 4. Nơi đặt code

| Loại | Đặt ở |
|---|---|
| State UI | `ui/<screen>/<X>UiState.kt` hoặc inline trong ViewModel |
| Mock/sample data | `data/sample/<Tên>.kt` (KHÔNG inline trong repository) |
| Business logic | `domain/repository/<X>RepositoryImpl.kt` |
| Network call | `data/remote/<X>RemoteDataSource.kt` (qua Retrofit) |
| DB | `data/local/dao/<X>Dao.kt` + `data/local/entity/<X>Entity.kt` |
| Hilt binding | `di/RepositoryModule.kt` |
| Navigation | `navigation/AppDestination.kt` (route) + `AppNavHost.kt` (binding) |
| Theme color | `ui/theme/Color.kt` |
| Theme typography | `ui/theme/Type.kt` |
| Shared composable | `ui/components/` (đơn giản) hoặc `ui/common/` (phức tạp, có state) |
| Util/helper | `utils/` |
| String | `app/src/main/res/values/strings.xml` |
| Drawable | `app/src/main/res/drawable/` |
| Layout XML (cho overlay) | `app/src/main/res/layout/` |

---

## 5. Quy trình thêm feature mới

### Thêm 1 màn hình mới (ví dụ "Wallet")

1. **Domain model** (nếu cần dữ liệu mới)
   - `domain/model/Wallet.kt` — pure Kotlin
2. **Repository**
   - `domain/repository/WalletRepository.kt` — interface định nghĩa hàm
   - `domain/repository/WalletRepositoryImpl.kt` — impl, branch MOCK/REMOTE
   - Bind trong `di/RepositoryModule.kt`
3. **Data source** (nếu cần network)
   - Thêm endpoint vào `data/remote/LoanApiService.kt` (hoặc tạo `WalletApiService.kt` riêng)
   - Thêm DTO + mapping ở `LoanRemoteDataSource.kt`
4. **Route**
   - Thêm `data object Wallet : AppDestination(route = "wallet", ...)` trong `AppDestination.kt`
   - Thêm vào `appDestinationFromRoute(...)` cuối file
5. **UI**
   - `ui/wallet/WalletScreen.kt` — Composable
   - `ui/wallet/WalletViewModel.kt` — `@HiltViewModel`, inject `WalletRepository`
   - `ui/wallet/WalletUiState.kt` — nếu state phức tạp
6. **Đăng ký**
   - Thêm `composable(AppDestination.Wallet.route) { WalletScreen() }` trong `AppNavHost.kt`
7. **Navigate đến**
   - Từ screen khác: `navController.navigate(AppDestination.Wallet.route)`
8. **String + theme**
   - Mọi text → `strings.xml` → `stringResource(R.string.wallet_title)`
   - Mọi color → theme color (`TealPrimary`, `TextPrimary`, ...)

### Thêm 1 endpoint mới

1. Cập nhật `documents/API_SPEC.md` (contract trước)
2. Thêm function vào `LoanApiService.kt` (hoặc service tương ứng)
3. Tạo `*Dto` nếu chưa có
4. Wrap ở `LoanRemoteDataSource.kt` → `Resource<DomainModel>`
5. Thêm function vào `Repository` interface + impl
6. Thêm test mapping ở `app/src/test/...` (xem `NotificationDtoMappingTest`)
7. Cập nhật `documents/BACKEND_DATA_PATHS.md` thêm row vào endpoint table + DTO mapping

---

## 6. Anti-patterns cần tránh

### ❌ Gọi network trực tiếp trong ViewModel
```kotlin
// SAI
class MyViewModel @Inject constructor(
    private val apiService: LoanApiService  // ← VI PHẠM
) : ViewModel()
```
```kotlin
// ĐÚNG
class MyViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel()
```

### ❌ Hardcode string trong Composable
```kotlin
// SAI
Text("Đăng nhập")
```
```kotlin
// ĐÚNG
Text(stringResource(R.string.login_button_label))
```

### ❌ Hardcode hex color
```kotlin
// SAI
Color(0xFF137A91)
```
```kotlin
// ĐÚNG — dùng theme
TealPrimary
```

### ❌ Dùng `!!`
```kotlin
// SAI
val name = user!!.name
```
```kotlin
// ĐÚNG
val name = user?.name ?: "Unknown"
val name = requireNotNull(user) { "user must be set" }.name
```

### ❌ Mock data inline trong UI hoặc ViewModel
```kotlin
// SAI — inline trong ViewModel
private val mockLoans = listOf(LoanPackage("L1", "..."), ...)
```
```kotlin
// ĐÚNG — di dời sang data/sample/SampleLoans.kt
val SAMPLE_LOANS = listOf(LoanPackage("L1", "..."), ...)
```

### ❌ Mutate state, không dùng `.copy()`
```kotlin
// SAI
_uiState.value.isLoading = true  // không hoạt động vì val
```
```kotlin
// ĐÚNG
_uiState.update { it.copy(isLoading = true) }
```

### ❌ Không dùng sealed cho UI state
```kotlin
// SAI — Boolean cờ rời rạc
data class UiState(val isLoading: Boolean, val data: List<X>?, val error: String?)
```
```kotlin
// ĐÚNG — sealed
sealed interface UiState {
    data object Loading : UiState
    data class Success(val data: List<X>) : UiState
    data class Error(val msg: String) : UiState
}
```

### ❌ Catch `CancellationException`
```kotlin
// SAI — nuốt cancellation của coroutine
try { ... } catch (e: Exception) { ... }
```
```kotlin
// ĐÚNG
try { ... } catch (e: CancellationException) { throw e } catch (e: Exception) { ... }
// hoặc dùng runCatching {} (đã safe với CancellationException)
```

### ❌ Lồng scope function quá sâu
```kotlin
// SAI
user?.let { u ->
    u.profile?.let { p ->
        p.address?.let { a ->
            a.city?.let { c -> println(c) }
        }
    }
}
```
```kotlin
// ĐÚNG — dùng safe call chain
println(user?.profile?.address?.city)
```
