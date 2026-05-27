# PROJECT_STRUCTURE - EasyMoney (out of date - cần cập nhật lại)

Cập nhật: 2026-05-26.

## 1. Package ownership

```text
com.example.easymoney/
├── EasyMoneyApplication.kt
├── MainActivity.kt
├── data/
│   ├── remote/
│   │   ├── LoanApiService.kt
│   │   ├── LoanApiFactory.kt
│   │   ├── LoanRemoteDataSource.kt
│   │   └── dto/
│   ├── local/
│   │   ├── AppPreferences.kt
│   │   ├── database/AppDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   └── sample/
├── di/
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── common/Resource.kt
│   ├── model/
│   └── repository/
├── messaging/
├── navigation/
│   ├── AppDestination.kt
│   ├── AppNavHost.kt
│   └── AppState.kt
├── ui/
│   ├── account/
│   ├── chatbot/
│   ├── common/
│   ├── components/
│   ├── confirmation/
│   ├── esign/
│   ├── guide/
│   ├── history/
│   ├── home/
│   ├── loan/
│   ├── login/
│   ├── notification/
│   ├── onboarding/
│   ├── payment/
│   ├── reward/
│   ├── sandbox/
│   ├── security/
│   ├── terms/
│   └── theme/
└── utils/
```

## 2. Data flow rules

- UI state và event handling nằm trong `ui/**`.
- Business/data access nằm trong `domain/repository/**`.
- Retrofit contract nằm trong `data/remote/**`.
- Room/AppPreferences nằm trong `data/local/**`.
- Mock/sample nằm trong `data/sample/**` hoặc repository mock branch. Không đặt mock data trong UI.
- Route tập trung trong `navigation/AppDestination.kt`; binding route -> screen trong `navigation/AppNavHost.kt`.

## 3. Repository hiện có

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

Repository có backend data phải branch theo `AppPreferences.dataSourceMode` nếu có cả MOCK và REMOTE.

## 4. Route ownership theo code hiện tại

| Route | Destination | Screen | Repository |
|---|---|---|---|
| `welcome` | `Welcome` | `ui/login/WelcomeScreen.kt` | - |
| `login_1` | `Login1` | `ui/login/LoginScreen1.kt` | `LoanRepository` |
| `register_1` | `Register1` | `ui/login/RegisterScreen1.kt` | `LoanRepository` |
| `quick_login_1` | `QuickLogin1` | `ui/login/QuickLoginScreen1.kt` | `LoanRepository` |
| `onboarding?...` | `Onboarding` | `ui/onboarding/OnboardingScreen.kt` | `LoanRepository` |
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
| `security_settings` | `SecuritySettings` | `ui/security/SecuritySettingsScreen.kt` | `UserRepository`/security flow |
| `loan_list` | `LoanList` | `ui/loan/discovery/LoanListScreen.kt` | `LoanRepository` |
| `loan_detail/{id}` | `LoanDetail` | `ui/loan/discovery/LoanDetailScreen.kt` | `LoanRepository` |
| `loan_information` | `LoanFlow` | `ui/loan/flow/LoanFlowScreen.kt` | `LoanRepository` |
| `confirm_information` | `ConfirmInformation` | `ui/confirmation/ConfirmInfoScreen.kt` | loan flow state/repository |
| `loan_management` | `LoanManagement` | `ui/loan/management/LoanManagementScreen.kt` | `LoanRepository` |
| `contract?...` | `Contract` | `ui/esign/ContractScreen.kt` | `LoanRepository` |
| `esign_success` | `EsignSuccess` | `ui/esign/EsignSuccessScreen.kt` | - |
| `money_management` | `MoneyManagement` | `ui/payment/MoneyManagementScreen.kt` | `PaymentRepository` |
| `payment_cards` | `PaymentCards` | `ui/payment/PaymentCardsScreen.kt` | `PaymentRepository` |
| `top_up` | `TopUp` | `ui/payment/TopUpScreen.kt` | `PaymentRepository` |
| `withdraw` | `Withdraw` | `ui/payment/WithdrawScreen.kt` | `PaymentRepository` |
| `event_detail/{id}` | `EventDetail` | `ui/home/EventDetailScreen.kt` | `EventRepository` |
| `rewards` | `Rewards` | `ui/reward/RewardScreen.kt` | `RewardRepository` |
| `chatbot` | `ChatBot` | `ui/chatbot/ChatBotScreen.kt` | `ChatBotRepository` |
| `terms` | `Terms` | `ui/terms/TermsScreen.kt` | local resources / future legal API |
| `sandbox` | `Sandbox` | `ui/sandbox/SandBoxScreen.kt` | `AppPreferences`, `NotificationRepository` |
| `page_guide?...` | `PageGuide` | `ui/guide/PageGuideScreen.kt` | guide XML |

## 5. Guide ownership

Destination có `showHelpButton=true` phải có `guideXmlName` trỏ tới file trong `app/src/main/res/layout/guide_*.xml`.

Nếu màn quá đơn giản:
- set `showHelpButton=false`, hoặc
- override top bar qua controller nếu route có điều kiện đặc biệt.

Guide XML không hard-code tiếng Việt; dùng `@string/...` và thêm key ở `values/strings.xml`, `values-en/strings.xml`.

## 6. Quy trình thêm endpoint

1. Cập nhật `documents/API_SPEC.md`.
2. Thêm Retrofit function vào `LoanApiService.kt` hoặc tạo service mới nếu domain lớn.
3. Thêm DTO và mapping.
4. Wrap trong remote data source.
5. Thêm/đổi repository interface và implementation.
6. Cập nhật UI state/ViewModel nếu cần.
7. Thêm mapping test cho DTO quan trọng.
8. Cập nhật `documents/API_SPEC.md` phần endpoint mapping/repository coverage.

## 7. Anti-patterns

- UI/ViewModel inject `LoanApiService` trực tiếp.
- Mock data inline trong Composable.
- Text production hard-code trong `Text("...")`.
- Màu UI hard-code khi có thể dùng `MaterialTheme.colorScheme`.
- Route string tự ghép tùy tiện khi `AppDestination` đã có `createRoute`.
- REMOTE branch trả mock success làm QA hiểu nhầm đã kết nối backend.
