# PROJECT_STRUCTURE - EasyMoney

Cập nhật: 2026-05-27.

## 1. Tổng quan module

```text
EasyMoney/
├── app/                         Android application module
├── documents/                   SRS, API spec, project structure, task list
├── gradle/                      Gradle wrapper/catalog
├── build.gradle.kts             Root Gradle config
├── settings.gradle.kts          Includes :app
└── gradle.properties
```

App module:

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/easymoney/
└── res/
```

## 2. Package ownership

```text
com.example.easymoney/
├── EasyMoneyApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── AppPreferences.kt
│   │   ├── dao/
│   │   ├── database/AppDatabase.kt
│   │   └── entity/
│   ├── remote/
│   │   ├── *ApiService.kt
│   │   ├── *RemoteDataSource.kt
│   │   ├── LoanApiFactory.kt
│   │   ├── RemoteCall.kt
│   │   └── dto/
│   └── sample/
├── di/
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── common/Resource.kt
│   ├── model/
│   └── repository/
├── domain.model/
│   └── AuthModels.kt
├── messaging/
│   └── EasyMoneyMessagingService.kt
├── navigation/
│   ├── AppDestination.kt
│   ├── AppNavHost.kt
│   └── AppState.kt
├── ui/
│   ├── AppRoot.kt
│   ├── account/
│   │   ├── changepassword/
│   │   └── profile/
│   ├── chatbot/
│   ├── common/
│   │   ├── components/
│   │   ├── error/
│   │   ├── identity/
│   │   └── loading/
│   ├── components/
│   ├── confirmation/
│   ├── esign/
│   ├── guide/
│   ├── history/
│   ├── home/
│   │   └── components/
│   ├── loan/
│   │   ├── components/
│   │   ├── configuration/
│   │   ├── discovery/
│   │   ├── flow/
│   │   ├── information/
│   │   │   ├── confirm/
│   │   │   ├── ekyc/
│   │   │   └── form/
│   │   └── management/
│   ├── login/
│   ├── notification/
│   │   ├── components/
│   │   ├── manager/
│   │   ├── model/
│   │   └── viewmodel/
│   ├── onboarding/
│   ├── payment/
│   ├── reward/
│   ├── sandbox/
│   ├── security/
│   ├── terms/
│   ├── theme/
│   └── web/
└── utils/
```

Ghi chú: `domain.model/AuthModels.kt` đang nằm trong folder có dấu chấm trong tên thư mục. Nên giữ nguyên khi chưa có refactor riêng để tránh churn.

## 3. Resource ownership

```text
app/src/main/res/
├── drawable/        Ảnh PNG/WebP và vector drawable
├── layout/          activity_main, guide_*.xml, item XML
├── mipmap-*/        Launcher icons
├── values/          strings, colors, themes
├── values-en/       English strings
└── xml/             backup, data extraction, locale config
```

Guide XML nằm trong `res/layout/guide_*.xml`. Destination có `showHelpButton=true` cần có `guideXmlName` hợp lệ hoặc phải tắt help button.

## 4. Data flow rules

- UI state và event handling nằm trong `ui/**`.
- Business/data access nằm trong `domain/repository/**`.
- Retrofit contract và remote wrapper nằm trong `data/remote/**`.
- DTO nằm trong `data/remote/dto/**`.
- Room/AppPreferences nằm trong `data/local/**`.
- Mock/sample nằm trong `data/sample/**` hoặc repository mock branch. Không đặt mock data trong UI.
- Route tập trung trong `navigation/AppDestination.kt`; binding route -> screen trong `navigation/AppNavHost.kt`.

## 5. Dependency injection

Tất cả repository bind trong `di/RepositoryModule.kt`:
- `LoanRepository`
- `NotificationRepository`
- `AccountRepository`
- `HomeRepository`
- `PaymentRepository`
- `UserRepository`
- `RewardRepository`
- `EventRepository`
- `TransactionHistoryRepository`
- `ChatBotRepository`

Remote services đang provide trong `di/NetworkModule.kt`:
- `LoanApiService`
- `UserApiService`
- `HomeApiService`
- `EventApiService`
- `RewardApiService`
- `TransactionHistoryApiService`
- `PaymentApiService`
- `ChatApiService`

`OkHttpClient` hiện chỉ có logging interceptor. Các task liên quan token cần kiểm tra bổ sung auth interceptor cho endpoint cần tài khoản.

## 6. Remote/data source ownership

```text
data/remote/
├── ChatApiService.kt / ChatRemoteDataSource.kt
├── EventApiService.kt / EventRemoteDataSource.kt
├── HomeApiService.kt / HomeRemoteDataSource.kt
├── LoanApiService.kt / LoanRemoteDataSource.kt
├── PaymentApiService.kt / PaymentRemoteDataSource.kt
├── RewardApiService.kt / RewardRemoteDataSource.kt
├── TransactionHistoryApiService.kt / TransactionHistoryRemoteDataSource.kt
├── UserApiService.kt / UserRemoteDataSource.kt
├── LoanApiFactory.kt
├── RemoteCall.kt
└── dto/
```

Repository có backend data phải branch theo `AppPreferences.dataSourceMode` nếu có cả MOCK và REMOTE. Ở REMOTE không được trả mock success khi remote lỗi.

## 7. Route ownership theo code hiện tại

| Route | Destination | Screen | Repository |
|---|---|---|---|
| `welcome` | `Welcome` | `ui/login/WelcomeScreen.kt` | - |
| `login_1` | `Login1` | `ui/login/LoginScreen1.kt` | `LoanRepository` |
| `register_1` | `Register1` | `ui/login/RegisterScreen1.kt` | `LoanRepository` |
| `quick_login_1` | `QuickLogin1` | `ui/login/QuickLoginScreen1.kt` | `LoanRepository`, local remembered account |
| `onboarding?...` | `Onboarding` | `ui/onboarding/OnboardingScreen.kt` | `LoanRepository` |
| `confirm_information?...` | `ConfirmInformation` | `ui/confirmation/ConfirmInfoScreen.kt` | `ConfirmInfoViewModel`, loan flow state |
| `loan_information?...` | `LoanFlow` | `ui/loan/flow/LoanFlowScreen.kt` | `LoanRepository` |
| `home` | `Home` | `ui/home/HomeScreen.kt` | `HomeRepository` |
| `history` | `TransactionHistory` | `ui/history/TransactionHistoryScreen.kt` | `TransactionHistoryRepository` |
| `notifications` | `Notifications` | `ui/notification/NotificationScreen.kt` | `NotificationRepository` |
| `account` | `Account` | `ui/account/AccountScreen.kt` | `AccountRepository`, `UserRepository` |
| `profile` | `Profile` | `ui/account/profile/ProfileScreen.kt` | `UserRepository` |
| `identity_verification` | `IdentityVerification` | `ui/account/profile/ProfileCompletionScreen.kt` | `UserRepository`, identity modules |
| `edit_personal_info` | `EditPersonalInfo` | `ui/account/profile/EditPersonalInfoScreen.kt` | `UserRepository`, `LoanRepository` master data |
| `edit_job_info` | `EditJobInfo` | `ui/account/profile/EditJobInfoScreen.kt` | `UserRepository`, `LoanRepository` master data |
| `edit_contact_info` | `EditContactInfo` | `ui/account/profile/EditContactInfoScreen.kt` | `UserRepository`, `LoanRepository` master data |
| `general_settings` | `GeneralSettings` | `ui/account/GeneralSettingsScreen.kt` | `AppPreferences` |
| `security_settings` | `SecuritySettings` | `ui/security/SecuritySettingsScreen.kt` | security flow/UserRepository |
| `change_password` | `ChangePassword` | `ui/account/changepassword/ChangePasswordScreen.kt` | `UserRepository` |
| `loan_list` | `LoanList` | `ui/loan/discovery/LoanListScreen.kt` | `LoanRepository` |
| `loan_detail/{id}` | `LoanDetail` | `ui/loan/discovery/LoanDetailScreen.kt` | `LoanRepository` |
| `loan_management` | `LoanManagement` | `ui/loan/management/LoanManagementScreen.kt` | `LoanRepository` |
| `contract?...` | `Contract` | `ui/esign/ContractScreen.kt` | `LoanRepository` |
| `esign_success` | `EsignSuccess` | `ui/esign/EsignSuccessScreen.kt` | - |
| `money_management` | `MoneyManagement` | `ui/payment/MoneyManagementScreen.kt` | `PaymentRepository` |
| `payment_cards` | `PaymentCards` | `ui/payment/PaymentCardsScreen.kt` | `PaymentRepository` |
| `top_up` | `TopUp` | `ui/payment/TopUpScreen.kt` | `PaymentRepository` |
| `withdraw` | `Withdraw` | `ui/payment/WithdrawScreen.kt` | `PaymentRepository` |
| `event_detail/{id}` | `EventDetail` | `ui/home/EventDetailScreen.kt` | `EventRepository` |
| `web_content?...` | `WebContent` | `ui/web/WebContentScreen.kt` | URL/content web |
| `rewards` | `Rewards` | `ui/reward/RewardScreen.kt` | `RewardRepository` |
| `chatbot` | `ChatBot` | `ui/chatbot/ChatBotScreen.kt` | `ChatBotRepository` |
| `terms` | `Terms` | `ui/terms/TermsScreen.kt` | local resources/future legal API |
| `sandbox` | `Sandbox` | `ui/sandbox/SandBoxScreen.kt` | `AppPreferences`, `NotificationRepository` |
| `page_guide?...` | `PageGuide` | `ui/guide/PageGuideScreen.kt` | guide XML |

## 8. Build stack

- Android Gradle Plugin/Kotlin qua version catalog.
- Kotlin JVM toolchain 17.
- Hilt DI.
- Jetpack Compose Material3.
- Room + KSP.
- Retrofit + Gson + OkHttp logging.
- DataStore Preferences.
- Firebase Analytics/Messaging.
- CameraX + ML Kit Face Detection.
- AndroidX Biometric.

Build kiểm tra sau mỗi task:

```powershell
.\gradlew.bat build
```

## 9. Quy trình thêm endpoint

1. Cập nhật `documents/API_SPEC.md`.
2. Thêm Retrofit function vào service đúng domain hoặc tạo service mới nếu domain lớn.
3. Thêm DTO và mapping.
4. Wrap trong remote data source.
5. Thêm/đổi repository interface và implementation.
6. Cập nhật UI state/ViewModel nếu cần.
7. Thêm mapping test cho DTO quan trọng khi rủi ro cao.
8. Cập nhật `documents/API_SPEC.md` và file này nếu có package/route/service mới.

## 10. Anti-patterns

- UI/ViewModel inject `*ApiService` trực tiếp.
- Mock data inline trong Composable.
- Text production hard-code trong `Text("...")`.
- Màu UI hard-code khi có thể dùng `MaterialTheme.colorScheme`.
- Route string tự ghép tùy tiện khi `AppDestination` đã có `createRoute`.
- REMOTE branch trả mock success làm QA hiểu nhầm đã kết nối backend.
- Hiển thị raw HTTP status/exception text cho user cuối ngoài sandbox/debug.
