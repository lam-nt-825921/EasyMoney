# Project Structure - Agent Map

Version: 2026-05-30. Verified against current frontend files.

## Roots

All paths are relative to the frontend repository root.

```text
.
├── app/                  Android application module
├── documents/            Agent context, backend contract snapshots, and task docs
├── gradle/               Gradle wrapper/catalog
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

Backend source is not part of this frontend repo and should not be assumed available. Use `documents/backend_contract.yaml` and `documents/API_SPEC.md` for backend HTTP contract details.

## Architecture Rule

Required flow:

```text
Composable Screen -> ViewModel -> Repository -> RemoteDataSource/Room/AppPreferences -> Retrofit/DAO
```

Do not inject Retrofit services directly into UI or ViewModel. Do not place production mock data inside Composables.

## Main Packages

```text
app/src/main/java/com/example/easymoney/
├── data/
│   ├── local/                 AppPreferences, Room DAO/entity/database
│   ├── remote/                Retrofit services, remote data sources, network helpers
│   └── sample/                MOCK-mode sample data
├── di/                        Hilt modules
├── domain/
│   ├── common/Resource.kt
│   ├── model/                 Domain models
│   └── repository/            Repository interfaces and implementations
├── domain.model/AuthModels.kt Folder name intentionally contains a dot
├── messaging/                 Firebase messaging service
├── navigation/                AppDestination, AppNavHost, AppState
├── ui/                        Compose screens/components/themes
└── utils/                     LinkHandler, LocaleUtils, UiText
```

## Dependency Injection

- `di/NetworkModule.kt`
  - Provides OkHttp with `AuthInterceptor` and logging.
  - Provides Retrofit using `AppPreferences.apiBaseUrl`.
  - Gson uses `FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES`.
  - Provides `LoanApiService`, `UserApiService`, `HomeApiService`, `EventApiService`, `RewardApiService`, `TransactionHistoryApiService`, `PaymentApiService`, `ChatApiService`.
- `di/RepositoryModule.kt`
  - Binds all repository interfaces to implementations.

Important: Retrofit is singleton and reads base URL when created. If a task changes runtime base URL behavior, check whether app restart is currently required.

## Data Source Mode

- `data/local/AppPreferences.kt`
  - `dataSourceMode`: `MOCK` or `REMOTE`.
  - `apiBaseUrl`: default `https://easymoney.lamgd.dev/`; for local Android emulator backend use `http://10.0.2.2:8000/`.
  - `accessToken`, `refreshToken`.
- Repositories branch on `AppPreferences.dataSourceMode`.
- In `REMOTE`, failed backend calls must surface errors, not fake successful mock data.
- Current product work targets `REMOTE` only. Do not add new MOCK-specific UI or behavior unless explicitly requested.
- `ui/sandbox/SandBoxScreen.kt` may remain as a debug screen, but production Home must not expose a sandbox/developer shortcut.

## Navigation Map

Routes are owned by `navigation/AppDestination.kt`; binding is in `navigation/AppNavHost.kt`.

| Business Area | Route | Screen | Primary ViewModel/Repository |
|---|---|---|---|
| Auth | `welcome` | `ui/login/WelcomeScreen.kt` | `LoginViewModel` |
| Auth | `login_1` | `ui/login/LoginScreen1.kt` | `LoanRepository.login` |
| Auth | `register_1` | `ui/login/RegisterScreen1.kt` | `LoanRepository.register` |
| Auth | `quick_login_1` | `ui/login/QuickLoginScreen1.kt` | `LoginViewModel`, Room remembered accounts |
| Home | `home` | `ui/home/HomeScreen.kt` | `HomeViewModel`, `HomeRepository`, `RewardRepository`, `UserRepository` |
| Transactions | `history` | `ui/history/TransactionHistoryScreen.kt` | `TransactionHistoryRepository` |
| Notifications | `notifications` | `ui/notification/NotificationScreen.kt` | `NotificationRepository` |
| Account | `account` | `ui/account/AccountScreen.kt` | `AccountViewModel`, `UserRepository`, `HomeRepository` |
| Profile | `profile` | `ui/account/profile/ProfileScreen.kt` | `ProfileCompletionViewModel`, `UserRepository` |
| Identity | `identity_verification` | `ui/account/profile/ProfileCompletionScreen.kt` | `UserRepository`, identity modules |
| Profile Edit | `edit_personal_info` | `ui/account/profile/EditPersonalInfoScreen.kt` | `EditProfileViewModel`, `LoanRepository` master data |
| Profile Edit | `edit_job_info` | `ui/account/profile/EditJobInfoScreen.kt` | `EditProfileViewModel`, `LoanRepository` master data |
| Profile Edit | `edit_contact_info` | `ui/account/profile/EditContactInfoScreen.kt` | `EditProfileViewModel`, `LoanRepository` master data |
| Settings | `general_settings` | `ui/account/GeneralSettingsScreen.kt` | `AppPreferences` |
| Security | `security_settings` | `ui/security/SecuritySettingsScreen.kt` | `SecurityViewModel` |
| Password | `change_password` | `ui/account/changepassword/ChangePasswordScreen.kt` | `UserRepository` |
| Loan Discovery | `loan_list` | `ui/loan/discovery/LoanListScreen.kt` | `LoanDiscoveryViewModel`, `LoanRepository` |
| Loan Discovery | `loan_detail/{id}` | `ui/loan/discovery/LoanDetailScreen.kt` | `LoanDiscoveryViewModel`, `LoanRepository` |
| Loan Pre-flow | `onboarding?...` | `ui/onboarding/OnboardingScreen.kt` | `OnboardingViewModel`, `LoanRepository` |
| Loan Pre-flow | `confirm_information?...` | `ui/confirmation/ConfirmInfoScreen.kt` | `ConfirmInfoViewModel` |
| Loan Flow | `loan_information?...` | `ui/loan/flow/LoanFlowScreen.kt` | `LoanFlowViewModel`, `LoanRepository` |
| Loan Flow | internal step | `ui/loan/information/form/LoanInformationFormScreen.kt` | `LoanInformationFormViewModel` |
| Loan Flow | internal step | `ui/loan/information/ekyc/*` | `EkycCameraViewModel`, identity modules |
| Loan Flow | internal success | `ui/loan/flow/LoanRegistrationSuccessScreen.kt` | Flow state |
| Loan Management | `loan_management?debtId=` | `ui/loan/management/LoanManagementScreen.kt` | `LoanManagementViewModel`, `LoanRepository` |
| Contract | `contract?contractId=` | `ui/esign/ContractScreen.kt` | `ContractViewModel`, `LoanRepository` |
| Wallet | `money_management` | `ui/payment/MoneyManagementScreen.kt` | `PaymentViewModel`, `PaymentRepository` |
| Cards | `payment_cards` | `ui/payment/PaymentCardsScreen.kt` | `PaymentViewModel`, `PaymentRepository` |
| Top Up | `top_up` | `ui/payment/TopUpScreen.kt` | `TopUpViewModel`, `PaymentRepository` |
| Withdraw | `withdraw` | `ui/payment/WithdrawScreen.kt` | `WithdrawViewModel`, `PaymentRepository` |
| Events | `event_detail/{id}` | `ui/home/EventDetailScreen.kt` | `EventRepository` |
| WebView | `web_content?...` | `ui/web/WebContentScreen.kt` | URL input |
| Rewards | `rewards` | `ui/reward/RewardScreen.kt` | `RewardViewModel`, `RewardRepository` |
| Chatbot | `chatbot` | `ui/chatbot/ChatBotScreen.kt` | `ChatBotViewModel`, `ChatBotRepository` |
| Terms | `terms` | `ui/terms/TermsScreen.kt` | Local strings |
| Sandbox/debug only | `sandbox` | `ui/sandbox/SandBoxScreen.kt` | `AppPreferences`, notification testing; do not expose from production Home |
| Guide | `page_guide?...` | `ui/guide/PageGuideScreen.kt` | XML guide resources |

## Remote Ownership

```text
data/remote/
├── LoanApiService.kt              Auth, loan, master data, eKYC, OTP, contract, notifications
├── UserApiService.kt              User profile/account security
├── HomeApiService.kt              Home/support
├── EventApiService.kt             Events
├── RewardApiService.kt            Reward catalog/user/redeem
├── PaymentApiService.kt           Wallet/cards/topup/withdraw/QR
├── TransactionHistoryApiService.kt
├── ChatApiService.kt
├── *RemoteDataSource.kt
└── dto/
```

Current smell: notification endpoints and `NotificationDto` are inside `LoanApiService.kt`. A cleanup can split them, but avoid wide refactor unless a task needs it.

## Local Persistence

- `data/local/database/AppDatabase.kt`
- `data/local/dao/NotificationDao.kt`
- `data/local/entity/NotificationEntity.kt`
- `data/local/dao/RememberedAccountDao.kt`
- `data/local/entity/RememberedAccountEntity.kt`
- `data/local/AppPreferences.kt`

Notifications use Room as a display cache. In `REMOTE`, refresh from backend then cache.

## Resources

```text
app/src/main/res/
├── values/strings.xml
├── values-en/strings.xml
├── values/colors.xml
├── values/themes.xml
├── drawable/
└── layout/guide_*.xml
```

Production text should come from string resources. Existing code still has hard-coded Vietnamese strings in several screens; when touching a screen, prefer moving newly edited text to resources if scope allows.

## Build And Test

Use from frontend root:

```powershell
.\gradlew.bat build
```

Focused tests live in:

```text
app/src/test/java/com/example/easymoney/data/remote/
```

DTO mapping changes should get focused unit tests when they affect backend integration.
