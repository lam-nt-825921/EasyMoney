# Agent Tasks - Verified Context And Fix Direction

Version: 2026-05-30.5.

Purpose: this file converts the draft problems into implementation context for a coding agent. Before implementing, read:

- `documents/CLAUDE.md`
- `documents/PROJECT_STRUCTURE.md`
- `documents/API_SPEC.md`
- `documents/backend_contract.yaml`

Assume the agent may only have the GitHub frontend repo. Use `documents/backend_contract.yaml` and `documents/API_SPEC.md` as the backend contract. Do not assume backend source can be opened or changed from frontend tasks.

## Current Product Mission

The current goal is to make the Android app feel complete as a commercial product in `REMOTE` mode against `https://easymoney.lamgd.dev/`.

Implementation priorities:

- Treat `REMOTE` as production. Do not invest in MOCK mode unless explicitly requested.
- Do not hide backend integration failures by returning mock/sample success in `REMOTE`.
- Remove sandbox/developer UI from production user-facing screens, especially Home.
- Make UI/UX polished enough for customer demo: no debug labels, no raw exception/status-code text, no controls that belong to engineering workflows.
- Sort time-based data newest first: notifications, transaction history, repayments/contracts/events where relevant.
- Use `documents/backend_contract.yaml` as the backend contract and keep frontend changes compatible with it.

## Problem 0 - Remove sandbox/developer entry point from Home

Verified code:

- `ui/home/HomeScreen.kt`
  - `HomeScreen` accepts `onToggleSandbox`.
  - `HeaderSection` receives `onDevClick = onToggleSandbox`.
  - `HeaderSection` renders a `Surface` with `R.string.home_developer_mode`; tapping it opens sandbox.
- `navigation/AppNavHost.kt`
  - Home route passes `onToggleSandbox = { navController.navigate(AppDestination.Sandbox.route) }`.
- `navigation/AppDestination.kt`
  - Sandbox route can remain for debug-only access if needed, but it must not be visible on Home.

Fix direction:

- Remove the developer/sandbox `Surface` from `HeaderSection`.
- Remove `onToggleSandbox` from `HomeScreen` parameters and Home call sites if no longer needed.
- Keep theme toggle if product wants it; it is user-facing, unlike sandbox.
- Do not delete `SandBoxScreen` unless explicitly asked. The requirement is to remove it from production Home UX.
- Remove or stop using `home_developer_mode` string if it becomes unused.

## Problem 1 - Notifications still use hard-coded userId and stale backend model mapping

Verified code:

- `domain/repository/NotificationRepositoryImpl.kt`
  - Has `private val currentUserId = "user_123"`.
  - `refreshNotifications()` maps backend DTO into Room using this hard-coded user id.
- `ui/notification/NotificationViewModel.kt`
  - Also has `private val currentUserId = "user_123"`.
  - This is the ViewModel used by `ui/notification/NotificationScreen.kt` because the screen is in the same package and its default parameter is `viewModel: NotificationViewModel = hiltViewModel()`.
- `ui/notification/viewmodel/NotificationViewModel.kt`
  - Second notification ViewModel also has `user_123`; appears stale/alternate unless another screen imports `com.example.easymoney.ui.notification.viewmodel.NotificationViewModel`.
- `messaging/EasyMoneyMessagingService.kt`
  - FCM data messages are saved and displayed with `userId = "user_123"`.
- `data/remote/LoanApiService.kt`
  - Notification endpoints currently live inside `LoanApiService`.
  - `NotificationDto` is declared in this file, not in `data/remote/dto`.
  - DTO currently lacks backend `category` and may rely on Gson naming policy for `balance_after`, `transaction_code`, `target_id`, `target_type`, `is_read`.
  - `NotificationDto.amount` and `balanceAfter` are `Long?`, but backend returns numbers as floats, sometimes with decimals, e.g. `-983333.3333333334`.

Backend contract:

- `GET /api/v1/notifications`
- `POST|PATCH /api/v1/notifications/{id}/read`
- `POST /api/v1/notifications/read-all`
- `DELETE /api/v1/notifications/clear`
- `POST /api/v1/notifications/fcm-token`
- Response model: `ApiResponse[List[NotificationDto]]`
- Backend `NotificationDto` fields: `id`, `title`, `content`, `type`, `category`, `amount`, `balance_after`, `transaction_code`, `timestamp`, `is_read`, `target_id`, `target_type`.
  - Current live sample confirms `amount` and `balance_after` are JSON numbers that can be fractional because repayment splits can produce decimals.

Fix direction:

- Introduce an auth/session user id source instead of local constants. If no user id is persisted yet, derive it from `mock_access_token_{id}` in `AppPreferences.accessToken` in a small helper.
- Make repository and notification ViewModel use that source.
- Add `category` to the frontend notification model if the UI needs category tabs; otherwise still parse and preserve it in Room/UI model for future filters.
- Change notification transport/domain/cache money fields to `Double` or parse through a DTO `Double?` and round explicitly before saving/displaying. Do not parse backend fractional money into `Long`.
- Move `NotificationDto` into a dedicated DTO file or at least add explicit `@SerializedName` to all snake_case fields.
- Ensure `clearAll()` syncs backend in `REMOTE` mode with `DELETE /api/v1/notifications/clear`.
- Keep Room as display cache, but backend remains source of truth in `REMOTE`.
- Guarantee display order newest first. `NotificationDao` currently uses `ORDER BY timestamp DESC`; preserve that and make sure remote refresh writes correct backend timestamps, uses the authenticated user id, and every tab/filter observes the sorted DAO query.

## Problem 2 - Company names and success UI colors

Verified code:

- Search target: `Viettel`, `ViettelPay`, old company/provider strings, and hard-coded display text.
- `ui/loan/information/form/LoanInformationFormUiState.kt`
  - `bankName` default is `ViettelPay`; should become an EasyMoney-appropriate value if shown to users.
- `data/sample/SampleRewardCatalog.kt`
  - Contains `Mã thẻ Viettel/Vinaphone`; this can stay only if it means telecom voucher brands, but if the task means all company labels must be EasyMoney, change copy carefully.
- `ui/loan/flow/LoanRegistrationSuccessScreen.kt`
  - Button container is hard-coded `Color(0xFFE60023)`.
  - Should use Material3 `MaterialTheme.colorScheme.primary` or an app semantic token.
- Strings:
  - `values/strings.xml` has `loan_success_title` and `loan_success_message`.
  - Verify message text does not mention any non-EasyMoney company.

Fix direction:

- Run `rg -n "Viettel|ViettelPay|Vinaphone|company|công ty|tổ chức" app/src/main`.
- Replace non-domain company labels with EasyMoney only when they represent the app/provider. Do not blindly rename telecom voucher brands if they are real voucher products unless product wants no third-party names anywhere.
- Replace hard-coded success button color with `ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)`.
- Audit other success screens for hard-coded red/green that bypass Material3.

## Problem 3 - Loan information form bottom button covers content

Verified code:

- `ui/loan/information/form/LoanInformationFormScreen.kt`
  - Uses root `Box`.
  - Scrollable `Column` fills max size and has only `.padding(16.dp)`.
  - Bottom CTA is a `Surface` aligned to `Alignment.BottomCenter`.
  - There is only `Spacer(modifier = Modifier.height(16.dp))` at the end of form content, so last fields can be hidden behind the CTA.

Fix direction:

- Add bottom content padding/spacer equal to the bottom action height plus safe area, for example `Spacer(Modifier.height(96.dp))`, or refactor into `Scaffold(bottomBar = ...)` and consume `paddingValues`.
- Prefer the smallest change: keep current Box + Surface, increase final spacer to `96.dp` or derive from button container height.
- Check the same pattern in other loan flow screens if the component is copied.

## Problem 4 - Reward points shown on Home/Account are not the real backend points

Verified code:

- `ui/home/HomeViewModel.kt`
  - Fetches `rewardRepository.getRewardsCatalog()` and uses `UserRewards.totalPoints` for Home.
  - This is correct in concept.
- `domain/repository/RewardRepositoryImpl.kt`
  - `REMOTE` calls `RewardRemoteDataSource.getRewardsCatalog()`.
  - `MOCK` uses `sampleUserRewards()`.
- `ui/account/AccountScreen.kt`
  - `PointsBanner()` hard-codes `1.250 điểm`.
  - `AccountUiState` has no `rewardPoints`.
  - `AccountViewModel` does not inject `RewardRepository`.
- Backend:
  - `GET /api/v1/rewards/user` returns `ApiResponse[UserRewardsDto]` with `total_points`, `history`, `vouchers`.
  - Login/register also return `AuthTokenDto.user`, but current mobile `AuthTokenDto` ignores `user`; reward points should come from rewards endpoint for consistency.

Fix direction:

- Add `RewardRepository` to `AccountViewModel`.
- Add `rewardPoints` and possibly `isRewardLoading` to `AccountUiState`.
- Load user profile and user rewards together or sequentially in `loadProfile()`.
- Change `PointsBanner(points: Int, onClick: () -> Unit)` and render `accountState.rewardPoints`.
- Keep Home as is, but confirm DTO mapping with backend `total_points` via Gson naming policy or explicit `@SerializedName("total_points")`.

## Problem 5 - Onboarding ProductInfoSection image removal and provider layout

Verified code:

- `ui/onboarding/OnboardingScreen.kt`
  - `ProductInfoSection()` renders a trailing `Image(R.drawable.img_4)` in a Row.
  - `ProviderInfoSection()` currently lays label and value as separate `Text` blocks, creating empty space when labels are short and address is long.

Fix direction:

- Remove the image from `ProductInfoSection()` and let information lines use full width.
- In `ProviderInfoSection()`, use one-line rows for short items:
  - `Tổ chức cho vay: EasyMoney`
  - `Tổng đài CSKH: <hotline>` as clickable text.
- Keep address as a label plus wrapped value, or use a row with `value` weighted and wrapping.
- Preserve click behavior: if `supportUrl` exists, open URL; otherwise dial hotline.
- Validate on narrow screens that long address wraps without leaving a broken two-column layout.

## Problem 6 - Backend/frontend DTO mismatch audit

Status: frontend and backend are not fully fit. I called representative backend endpoints through FastAPI `TestClient` with `Authorization: Bearer mock_access_token_1` and compared the returned JSON with current Kotlin Retrofit/domain models.

High-risk mismatches:

| Area | Backend live response | Current frontend expectation | Risk |
|---|---|---|---|
| Generic API envelope | `data` can be `null` for `ApiResponse[None]`; many mutating endpoints return `{}` or `null` payloads | `data/remote/dto/MetadataDto.kt` has `data: T` non-null; `safeApiCall` blindly returns `response.data` | Unit/mutation endpoints can produce null success data or parsing edge cases |
| Loan package my | `GET /api/v1/loan/package/my` returns `data: [LoanPackageDto, ...]` | `LoanApiService.getMyPackage(): ApiResponse<LoanPackageModel>` | Remote onboarding/configuration can fail with array-vs-object parse error |
| Notifications money fields | `amount` and `balance_after` can be floats with decimals | `NotificationDto.amount: Long?`, `balanceAfter: Long?`; Room/UI model also `Long?` | Gson parse failure or value loss; likely root cause of failed notification backend usage |
| Notifications category | Backend includes `category` | Frontend DTO/Room/UI do not preserve it | Backend grouping cannot be used correctly; future filters can drift |
| Payment cards | `balance` is float, e.g. `14265851.0` | `PaymentCard.balance: Long` | Gson can fail parsing decimal JSON into `Long` |
| Auth response | Backend `AuthTokenDto` includes nested `user` | Frontend `AuthTokenDto` ignores `user` and only stores tokens | Not a parse blocker, but user id/profile cache cannot be initialized from login response |
| eKYC status | Backend returns rich `EkycStatusDto` with `status`, `session_id`, match fields, document method, verified timestamp | Frontend `EKycStatus` only has `isIdentified`, `missingDocuments`, `message` | Parse is likely okay because extra fields are ignored, but UI cannot show exact missing/mismatch state |
| Event join and other Unit endpoints | Backend returns `ApiResponse[dict]` or `ApiResponse[None]` depending endpoint | Some frontend services declare `ApiResponse<Unit>` and use generic `safeApiCall` | Success handling should not depend on `data` for Unit endpoints |

Mostly fit or low-risk areas:

- `UserProfileDto`, `ProfileCompletionDto`, and master data mapping fit the backend snake_case shape through Gson `LOWER_CASE_WITH_UNDERSCORES`.
- Rewards user/catalog mostly fit: `total_points`, voucher fields, and `image_url` map to camelCase fields; still add explicit DTO tests with live backend samples.
- Home banners, hot loans, support link, chat response, transaction groups, loan quote/application response mostly fit by naming policy.
- Loan contract/debt models mostly fit; backend has extra `due_date` on debt which frontend ignores.

Fix direction:

- Stop using domain models directly as Retrofit response DTOs for money-heavy endpoints. Add DTOs for `NotificationDto`, `PaymentCardDto`, `WalletInfoDto`, `LoanPackageDto`, `EkycStatusDto`.
- Make `ApiResponse<T>` nullable: `val data: T? = null`, then enforce non-null per call in data sources with clear errors. Use dedicated unit helpers for no-payload success endpoints.
- Add mapping tests using backend live samples for notification, payment card/wallet, loan package my, rewards, and eKYC status.
- Decide one frontend money strategy:
  - If UI displays integer VND only, parse backend numeric values as `Double` and round/format centrally.
  - If backend should never return fractional VND, fix backend repayment math to round to int before storing notifications/card/account values. Until then, frontend must tolerate decimals.
- After DTO fixes, retest REMOTE mode flows: login, notifications refresh/mark read, payment cards/wallet, reward points, onboarding package load, and loan management repayment.

## Problem 6.1 - Transaction history order and timestamp model are incomplete

Observed issue:

- Product owner saw transactions not ordered correctly.
- Required behavior: newest transactions first, oldest after.

Verified code:

- `domain/model/TransactionHistory.kt`
  - `TransactionItem` has `description`, `transactionCode`, `amount`, `balance`, `time`.
  - `TransactionGroup` has `date`, `items`.
  - There is no timestamp field in the frontend transaction domain model.
- `data/remote/TransactionHistoryApiService.kt`
  - Uses backend response directly as `ApiResponse<List<TransactionGroup>>`.
- `data/remote/TransactionHistoryRemoteDataSource.kt`
  - Calls `safeApiCall` and returns the backend list as-is.
- `ui/history/TransactionHistoryViewModel.kt`
  - Assigns `groups = result.data` without sorting.
- Backend contract includes transaction `timestamp` at item level in `TransactionItemDto`.

Fix direction:

- Stop using domain `TransactionGroup` directly as Retrofit DTO for `/api/v1/transactions`.
- Add remote DTOs that include backend `timestamp`:
  - `TransactionItemDto(description, transaction_code, amount, balance, time, timestamp)`
  - `TransactionGroupDto(date, items)`
- Map DTOs to domain models. Prefer adding `timestamp: Long` to `TransactionItem` if UI or sorting needs it; otherwise sort before dropping timestamp.
- Sort groups/items by timestamp descending before exposing state:
  - Overall newest transaction appears at the top.
  - Within each date group, newest item first.
  - Date groups ordered by the max timestamp of their items, descending.
- If a timestamp is missing or zero, fall back to parsing `date` + `time`, but backend `timestamp` should be primary.
- Add a focused mapper/unit test with out-of-order backend sample data.

## Problem 6.2 - Production REMOTE cleanup and UI polish sweep

Scope:

- This is a cross-cutting cleanup pass after DTO fixes.
- Goal is not to remove every MOCK branch from the codebase. Goal is to ensure product UX in `REMOTE` mode is complete and not polluted by mock/debug behavior.

Audit targets:

- Any repository `REMOTE` branch that returns sample/mock data after network/backend failure.
- Any user-facing screen that shows debug labels, sandbox controls, raw HTTP status, raw exception text, or backend stack text.
- Home, Account, Notification, Transaction History, Loan Management, Payment, Rewards, eKYC/profile completion.

Fix direction:

- In `REMOTE`, show user-friendly error states with retry actions where appropriate.
- Keep loading/empty/error states polished and specific:
  - Empty notifications: say there are no notifications yet.
  - Empty transactions: say no transactions have been recorded yet.
  - Empty cards: show add-card action.
- Do not block production fixes on MOCK behavior.

## Problem 7 - Newly registered users must not show a default payment card

Observed issue:

- Product owner previously saw a payment card appear immediately after registering a new account.
- Backend bug was found and fixed: `AuthService.register()` used to create a default `Card`; backend now creates only `User` and EasyMoney wallet `Account`.
- Canonical backend behavior now verified with FastAPI `TestClient`:
  - `POST /api/v1/auth/register` returns `mock_access_token_{new_user_id}`.
  - `GET /api/v1/payment/cards` for that token returns `ApiResponse<List<PaymentCardDto>>` with `data: []`.
  - `GET /api/v1/payment/wallet` returns a zero-balance wallet with no recent flows.

Frontend audit targets:

- `PaymentViewModel`, `PaymentRepositoryImpl`, `PaymentRemoteDataSource`, and `PaymentCardsScreen`.
- `TopUpViewModel`, `WithdrawViewModel`, and `LoanManagementViewModel`, because they should surface add-card/choose-card state when backend returns no cards.
- Any MOCK/sample fallback path that can be accidentally used in `REMOTE` mode after register.
- Local persistence/cache, if cards are cached anywhere. Do not reuse cards from a previous remembered account after switching/registering users.

Fix direction:

- In `REMOTE` mode, an empty backend card list is a valid state and should be rendered as empty/add-card UI, not replaced with sample cards.
- After register/login/account switch, clear user-scoped payment/card state before loading the new user's remote cards.
- Keep backend `CARD_REQUIRED` errors actionable: topup/withdraw/repay should navigate to Payment Cards when no card exists.
- Do not change frontend models to require a card. Payment cards are optional linked external instruments.

Backend contract for Claude without backend source:

- Public base URL: `https://easymoney.lamgd.dev/`.
- Card list endpoint: `GET /api/v1/payment/cards`.
- Add-card endpoint: `POST /api/v1/payment/cards`.
- Empty card response is correct:

```json
{
  "status": "success",
  "message": null,
  "data": []
}
```

## Final Phase Gate - Do not start before previous tasks are committed

The tasks below are final hardening work. They must be done after the main REMOTE/backend-fit tasks are completed and committed:

- Problem 0: remove Home sandbox/developer entry.
- Problem 1: notification user id, DTO, backend sync, newest-first order.
- Problem 2-5: company names/UI polish/onboarding/form overlap/reward points.
- Problem 6 and 6.1: DTO mismatch audit and transaction newest-first ordering.
- Problem 6.2: production REMOTE cleanup.
- Problem 7: newly registered user has no default card.

Only start the final phase if the app can already run in `REMOTE` mode for login, Home, notifications, transaction history, rewards, payment cards/wallet, loan management, and contract flows. If you start the final phase, you must verify the changed flows before committing.

## Final Problem A - Remove hard-coded user-facing text and complete VI/EN resources

Why this is last:

- It touches many UI files and string resources. Doing it before remote/DTO fixes creates noisy conflicts.
- It must preserve behavior while making the UI locale-correct.

Goal:

- Every user-facing production string must come from `app/src/main/res/values/strings.xml` and `app/src/main/res/values-en/strings.xml`.
- Both files must contain the same key set.
- Avoid hard-coded Vietnamese/English in composables, ViewModels, and repository errors that surface to users.
- Do not localize backend content fields such as banner titles, event content, contract HTML/text, reward names, transaction descriptions, or notification content. Those are backend-owned.

Known hard-code hotspots from current code audit:

| Area | Files | Examples to replace |
|---|---|---|
| Profile/contact edit | `app/src/main/java/com/example/easymoney/ui/account/profile/EditContactInfoScreen.kt`, `EditJobInfoScreen.kt`, `EditPersonalInfoScreen.kt`, `ProfileScreen.kt`, `ProfileCompletionViewModel.kt` | `Danh bạ`, `Contacts`, `Tóm tắt`, NFC error messages |
| Confirm information | `app/src/main/java/com/example/easymoney/ui/confirmation/ConfirmInfoScreen.kt` | `Họ và tên`, `Giới tính`, `Ngày sinh`, `Số điện thoại`, `N/A` |
| Rewards | `app/src/main/java/com/example/easymoney/ui/reward/RewardScreen.kt`, `RewardViewModel.kt`, `domain/repository/RewardRepositoryImpl.kt` | `Đổi quà`, `Đã đổi`, `Bạn chưa đổi quà nào`, `Không đủ điểm để đổi`, `Quà đã đổi` |
| Payment | `app/src/main/java/com/example/easymoney/ui/payment/MoneyManagementScreen.kt`, `TopUpViewModel.kt`, `TopUpScreen.kt`, `WithdrawViewModel.kt`, `WithdrawScreen.kt`, `PaymentCardsScreen.kt` | `Nạp tiền`, `Rút tiền`, `Số tiền không hợp lệ`, `Chưa chọn thẻ nguồn`, `Bạn chưa thêm thẻ ngân hàng`, `Thêm thẻ` |
| Loan configuration | `app/src/main/java/com/example/easymoney/ui/loan/configuration/LoanConfigurationContent.kt`, `LoanBreakdownBottomSheet.kt`, `TenorBottomSheet.kt` | `Chọn kỳ hạn vay`, `Voucher ưu đãi`, `Không dùng voucher`, `Tổng tiền phải trả`, `tháng` |
| Loan information/confirmation | `app/src/main/java/com/example/easymoney/ui/loan/information/form/LoanInformationFormScreen.kt`, `LoanSelectionBottomSheet.kt`, `confirm/ConfirmLoanInformationScreen.kt` | Form section titles, labels, contact button, submit error dialog |
| eKYC camera/error | `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycFaceCaptureScreen.kt`, `EkycErrorScreen.kt`, `EkycIntroScreen.kt` | `Back`, `Xác thực không thành công`, `Cho phep truy cap`, `Mo Cai dat`, camera permission copy |
| Loan management | `app/src/main/java/com/example/easymoney/ui/loan/management/LoanManagementScreen.kt`, `LoanManagementViewModel.kt` | `Hợp đồng chờ ký`, `Khoản nợ hiện tại`, `Thông báo`, `Ví EasyMoney`, `Thẻ ngân hàng`, `Trả kỳ này`, `Tất toán`, `Thanh toán khoản nợ thành công.` |
| Contract/eSign previews | `app/src/main/java/com/example/easymoney/ui/esign/ContractScreen.kt`, `EsignSuccessScreen.kt` | Preview-only strings can remain if preview-only, but production strings must be resources |
| Chat/common components | `app/src/main/java/com/example/easymoney/ui/chatbot/ChatBotScreen.kt`, `ui/components/AppTextField.kt` | `Send`, `Toggle password visibility` |

Implementation plan:

1. Run focused hard-code audit:
   - `rg -n 'Text\(\"|contentDescription = \"|Toast\\.makeText\\([^,]+,\\s*\"|errorMessage = \"|successMessage = \"|title = \"|label = \"' app/src/main/java/com/example/easymoney`
   - Manually exclude backend-owned dynamic fields and debug/preview-only text.
2. Add string keys in both resource files:
   - Vietnamese: `app/src/main/res/values/strings.xml`
   - English: `app/src/main/res/values-en/strings.xml`
3. Naming convention:
   - Screen prefix first, e.g. `withdraw_empty_cards`, `loan_mgmt_tab_contracts`, `loan_form_section_address`, `confirm_info_full_name`.
   - Shared actions use existing `action_*` keys where possible.
4. Replace composable strings with `stringResource(R.string.key)`.
5. For ViewModels/repositories that need localized messages:
   - Prefer returning error codes/state and mapping to strings in UI.
   - If scope must stay smaller, use an existing `UiText` pattern if available; do not inject Android `Context` into repositories just for strings.
6. Format values with placeholders in resources:
   - Use `%1$s`, `%1$d`, `%1$.1f` as needed.
   - Keep currency/month formatting locale-aware in Kotlin, but labels/units come from strings.
7. Verify key parity:
   - Every new key in `values/strings.xml` must exist in `values-en/strings.xml`.
   - Build must pass after resource changes.

Acceptance criteria:

- Switching VI/EN no longer leaves hard-coded Vietnamese on the audited production screens.
- No missing resource key in either locale.
- No raw debug text appears in production flows.

## Final Problem B - Make device biometric 2FA real and local-only

Why this is last:

- It cuts across settings, identity/profile display, withdraw, loan repayment, and contract signing.
- It must be tested end to end on a device/emulator with enrolled biometric or supported credential.
- It should not be mixed with backend contract fixes.

Current state:

- `data/local/AppPreferences.kt`
  - Has `isBiometric2FAEnabled`, local SharedPreferences flag.
- `ui/security/SecuritySettingsScreen.kt`
  - Shows biometric toggle.
  - Toggle currently calls `viewModel.toggleBiometric(it)` directly; enabling does not require successful biometric prompt.
- `ui/security/SecurityViewModel.kt`
  - Checks support and saves `isBiometric2FAEnabled`.
  - Comment says UI should call after `BiometricPrompt`, but current UI does not.
- `ui/common/identity/BiometricModule.kt`
  - Wraps `BiometricPrompt` and returns `BiometricResult`.
- `ui/account/profile/ProfileCompletionScreen.kt`
  - Shows Device Biometrics as part of identity modules.
- `ui/account/profile/ProfileCompletionViewModel.kt`
  - `onBiometricResult()` updates `IdentityStatus.isBiometricEnabled`.
  - This is local UI/profile state, not backend eKYC truth.
- `ui/payment/WithdrawViewModel.kt`
  - Calls `paymentRepository.withdraw(..., biometricToken = null)`.
- `ui/loan/management/LoanManagementViewModel.kt`
  - Repay action does not check local biometric.
- `ui/esign/ContractViewModel.kt`
  - `signContract()` immediately shows OTP; no local biometric gate.

Required product behavior:

- No backend changes.
- Biometric is only device-local 2FA.
- The setting controls whether the biometric gate is required.
- If setting is off, sensitive actions behave exactly as they do now.
- If setting is on, sensitive actions require successful `BiometricPrompt` before proceeding.
- Sensitive actions for this phase:
  - Withdraw money.
  - Repay debt or early settlement.
  - Start contract signing/OTP flow.
- Optional future actions, only if scope remains safe: delete card, change password submit, logout-all-devices if implemented.

Recommended structure:

- Add a reusable controller/composable:
  - `app/src/main/java/com/example/easymoney/ui/common/security/BiometricGate.kt`
  - Responsibility: if `AppPreferences.isBiometric2FAEnabled` is false, immediately run the pending action; if true, show `BiometricModule` and run the action only on success.
  - Keep all prompt display in UI layer because `BiometricPrompt` needs a `FragmentActivity`.
- Add/update strings in both locale files:
  - `biometric_gate_title`
  - `biometric_gate_subtitle_sensitive_action`
  - `biometric_gate_cancelled`
  - `biometric_gate_failed`
  - `security_biometric_enable_prompt`
  - `security_biometric_enabled`
  - `security_biometric_disabled`

Implementation plan by file:

1. `ui/security/SecuritySettingsScreen.kt`
   - When user turns toggle ON, render `BiometricModule`.
   - Only call `viewModel.toggleBiometric(true)` after `BiometricResult.success == true`.
   - If prompt fails/cancels, keep switch OFF and show localized error.
   - Turning OFF can call `viewModel.toggleBiometric(false)` directly, or require biometric before disabling if product wants stricter security. Default: allow direct off for this demo.
2. `ui/security/SecurityViewModel.kt`
   - Keep support check and local preference write.
   - Add transient `errorMessage`/`successMessage` if needed, but prefer UI-local snackbar if smaller.
3. `ui/common/identity/BiometricModule.kt`
   - Make prompt title/subtitle configurable parameters with defaults.
   - Continue returning `BiometricResult`.
   - Ensure cancel/failure messages are localized and not raw system text unless useful.
4. `ui/account/profile/ProfileCompletionScreen.kt` and `ProfileCompletionViewModel.kt`
   - Do not treat device biometrics as backend eKYC/document verification.
   - Either remove the Device Biometrics CTA from required identity completion, or make it clearly open/use the same local device biometric setup flow.
   - Its completion state should read from `AppPreferences.isBiometric2FAEnabled` or profile display state, not imply loan identity verification is complete.
5. `ui/payment/WithdrawScreen.kt` and `WithdrawViewModel.kt`
   - Move the final submit trigger behind the biometric gate.
   - UI captures submit click:
     - Validate through ViewModel.
     - If validation passes and `isBiometric2FAEnabled`, show `BiometricModule`.
     - On success, call ViewModel method that actually performs withdraw.
   - Remove `biometricToken = null` as a meaningful concept for local-only 2FA; pass null to backend because backend is unchanged, but only after local auth succeeds.
6. `ui/loan/management/LoanManagementScreen.kt` and `LoanManagementViewModel.kt`
   - Gate `repayDebt()` confirmation.
   - User selects wallet/card and confirms; if 2FA enabled, run biometric before calling `viewModel.repayDebt(...)`.
   - If auth fails/cancels, keep dialog/action state understandable and do not call backend.
7. `ui/esign/ContractScreen.kt` and `ContractViewModel.kt`
   - Gate `viewModel.signContract(...)` before showing OTP.
   - If 2FA enabled and auth succeeds, then call `signContract()` which sends/shows OTP.
   - OTP remains backend/signing verification; biometric is local pre-check only.
8. `data/local/AppPreferences.kt`
   - Keep `isBiometric2FAEnabled` as local source of truth.
   - Consider adding `biometric2FAEnabledFlow` only if multiple screens need live updates while visible; otherwise existing getter is sufficient.

Testing checklist:

- Device unsupported:
  - Security toggle disabled, shows unsupported subtitle.
  - Sensitive flows do not prompt because setting cannot be enabled.
- Enable biometric:
  - Toggle ON opens system prompt.
  - Success persists ON.
  - Cancel/fail keeps OFF.
- Disable biometric:
  - Toggle OFF persists OFF.
- Withdraw:
  - OFF: withdraw flow works as before.
  - ON: cancel/fail does not call backend; success calls backend once.
- Loan repayment:
  - OFF: repay works as before.
  - ON: cancel/fail does not call backend; success calls backend once.
- Contract signing:
  - OFF: sign opens OTP as before.
  - ON: biometric success then OTP; cancel/fail does not send OTP.
- Language:
  - All biometric prompts/errors have VI/EN resources.
