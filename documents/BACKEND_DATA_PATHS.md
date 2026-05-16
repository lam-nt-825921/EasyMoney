# Backend Data Paths — EasyMoney

> Endpoint → DTO → Domain → Screen mapping. Cập nhật mỗi khi backend đổi contract hoặc thêm endpoint.

---

## 1. Endpoint table

| HTTP | Path | Request DTO | Response DTO | Repository function | Layer triggers |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/login` | `LoginRequestDto` | `ApiResponse<AuthTokenDto>` | `LoanRepository.login()` | `Login1ViewModel` |
| POST | `/api/v1/auth/register` | `RegisterRequestDto` | `ApiResponse<AuthTokenDto>` | `LoanRepository.register()` | `Register1ViewModel` |
| GET  | `/api/v1/loan/package/my` | — | `ApiResponse<List<LoanPackageDto>>` | `LoanRepository.getMyLoanPackages()` | `LoanListViewModel`, `LoanDetailViewModel` |
| GET  | `/api/v1/master/metadata` | — | `ApiResponse<MasterDataMetadataDto>` | `LoanRepository.getMasterMetadata()` | `IdentityVerificationViewModel`, `EditPersonalInfoViewModel` |
| GET  | `/api/v1/master/districts/{provinceId}` | — | `ApiResponse<List<MasterDataItemDto>>` | `LoanRepository.getDistricts(provinceId)` | địa chỉ form |
| GET  | `/api/v1/master/wards/{districtId}` | — | `ApiResponse<List<MasterDataItemDto>>` | `LoanRepository.getWards(districtId)` | địa chỉ form |
| POST | `/api/v1/ekyc/capture/face` | multipart `image`, `frontIdImage`, `backIdImage` | `ApiResponse<EkycResultDto>` | `LoanRepository.submitEkyc(...)` | `EkycCameraViewModel` |
| POST | `/api/v1/otp/send` | `OtpSendRequest(phone, purpose)` | `ApiResponse<Unit>` | `LoanRepository.sendOtp(...)` | login/register flow |
| POST | `/api/v1/otp/verify` | `OtpVerifyRequest(otp, purpose)` | `ApiResponse<Unit>` | `LoanRepository.verifyOtp(...)` | login/register flow |
| GET  | `/api/v1/notifications` | — | `ApiResponse<List<NotificationDto>>` | `NotificationRepository.refreshNotifications()` | `NotificationViewModel` |
| GET  | `/api/v1/loan/provider-info` | — | `ApiResponse<LoanProviderInfoModel>` | `LoanRepository.getLoanProviderInfo()` | `OnboardingViewModel` |
| POST | `/api/v1/test/fcm/trigger` | `FcmTestRequest` | `ApiResponse<Unit>` | `NotificationRepository.triggerFcmTest()` | `SandBoxViewModel` |

---

## 2. DTO ↔ Domain mapping

### `AuthTokenDto` ↔ `AuthToken`
| DTO field (Kotlin) | JSON key (`@SerializedName`) | Domain field |
|---|---|---|
| `accessToken: String` | `"accessToken"` | `accessToken` |
| `refreshToken: String` | `"refreshToken"` | `refreshToken` |
| `expiresIn: Int` | `"expiresIn"` | `expiresIn` |

> Backend dùng camelCase. `@SerializedName` ép Gson bỏ qua naming policy `LOWER_CASE_WITH_UNDERSCORES`.

### `NotificationDto` ↔ `NotificationEntity`
| DTO field | JSON key (snake_case via policy) | Entity field | Ghi chú |
|---|---|---|---|
| `id: Int` | `id` | `id: Long` (cast) | id local Long |
| `title: String` | `title` | `title: String` | nullable fallback "Thông báo" |
| `content: String` | `content` | `content: String` | |
| `type: String` | `type` | `type: String` | nullable fallback "transaction" |
| `amount: Long?` | `amount` | `amount: Long?` | |
| `balanceAfter: Long?` | `balance_after` | `balanceAfter: Long?` | |
| `transactionCode: String?` | `transaction_code` | `transactionCode: String?` | |
| `timestamp: Long` | `timestamp` | `timestamp: Long` | nếu ≤ 0 dùng `System.currentTimeMillis()` |
| `isRead: Boolean` | `is_read` | `isRead: Boolean` | |

### `MasterDataItemDto` ↔ `MasterDataItem`
| DTO field | JSON key | Domain field |
|---|---|---|
| `id: String` | `id` | `id` |
| `name: String` | `name` | `name` |
| `parentId: String?` | `parent_id` | `parentId` |

### `MasterDataMetadataDto` ↔ `MasterDataMetadata`
- `version` ↔ `version`
- `expiredAt` (JSON `expired_at`) ↔ `expiredAt`
- `masterData` (JSON `master_data`) ↔ flatten thành các list trong `MasterDataMetadata` (provinces, professions, positions, educationLevels, maritalStatuses, relationships)

---

## 3. Screen ↔ Repository

| Screen / Route | ViewModel | Repository | Mode hỗ trợ |
|---|---|---|---|
| `welcome` | — | — | n/a |
| `login_1` | `Login1ViewModel` | `LoanRepository` | MOCK + REMOTE |
| `register_1` | `Register1ViewModel` | `LoanRepository` | MOCK + REMOTE |
| `quick_login_1` | `QuickLogin1ViewModel` | `LoanRepository` | MOCK + REMOTE |
| `home` | `HomeViewModel` | `HomeRepository` | MOCK + REMOTE stub |
| `history` | `TransactionHistoryViewModel` | `LoanRepository` | MOCK + REMOTE stub |
| `notifications` | `NotificationViewModel` | `NotificationRepository` | MOCK + REMOTE |
| `account` | `AccountViewModel` | `AccountRepository`, `UserRepository` | MOCK + REMOTE stub |
| `loan_list` | `LoanListViewModel` | `LoanRepository.getMyLoanPackages()` | MOCK + REMOTE |
| `loan_detail/{id}` | `LoanDetailViewModel` | `LoanRepository` | MOCK + REMOTE |
| `loan_information` | `LoanFlowViewModel` | `LoanRepository` | MOCK + REMOTE |
| `confirm_information` | `ConfirmInformationViewModel` | `LoanRepository` | MOCK + REMOTE |
| `identity_verification` | `IdentityVerificationViewModel` | `LoanRepository` (master + ekyc) | MOCK + REMOTE |
| `event_detail/{id}` | `EventDetailViewModel` | `EventRepository` | MOCK + REMOTE stub |
| `rewards` | `RewardViewModel` | `RewardRepository` | MOCK + REMOTE stub |
| `profile` | `ProfileViewModel` | `UserRepository` | MOCK + REMOTE stub |
| `money_management` | `MoneyManagementViewModel` | `PaymentRepository` | MOCK + REMOTE stub |
| `payment_cards` | `PaymentCardsViewModel` | `PaymentRepository` | MOCK + REMOTE stub |
| `general_settings` | `GeneralSettingsViewModel` | `AppPreferences` | n/a |
| `security_settings` | `SecuritySettingsViewModel` | `UserRepository` | MOCK + REMOTE stub |
| `chatbot` | `ChatBotViewModel` (chưa tồn tại) | `ChatBotRepository` (chưa tồn tại) | MOCK + REMOTE stub |
| `sandbox` | `SandBoxViewModel` | `NotificationRepository`, `AppPreferences` | n/a (dev tool) |
| `contract` | `ContractViewModel` | `LoanRepository` | MOCK + REMOTE stub |
| `esign_success` | — | — | static |

> "MOCK + REMOTE stub" nghĩa là repository hiện trả mock cứng — chưa có nhánh REMOTE. Khi backend có endpoint sẽ thêm nhánh tương ứng.

---

## 4. Mode switching guide

Mode lưu ở `AppPreferences.dataSourceMode: DataSourceMode` (`MOCK` hoặc `REMOTE`). Reactive qua `dataSourceModeFlow: StateFlow<DataSourceMode>`.

### Bật/tắt từ app
1. Mở app → tab Account → "Sandbox Developer" (hoặc deep link `sandbox`)
2. Toggle giữa MOCK và REMOTE
3. Đổi `apiBaseUrl` nếu cần (mặc định `https://easymoney.lamgd.dev/`)
4. Thay đổi áp dụng ngay — mọi repository check `appPreferences.dataSourceMode` lần gọi tiếp theo

### Logging
- Repos branch theo mode log `Log.d("DataSource", "<repo> mode=<MOCK|REMOTE>")` — filter logcat `tag:DataSource` để theo dõi

### Repos đã hỗ trợ branch
- ✅ `LoanRepositoryImpl.isRemote()` → toàn bộ auth, loan, ekyc, otp
- ✅ `NotificationRepositoryImpl.refreshNotifications()`
- ✅ `HomeRepositoryImpl`, `EventRepositoryImpl`, `RewardRepositoryImpl`, `UserRepositoryImpl`, `PaymentRepositoryImpl` (workflow #20) — branch MOCK/REMOTE bằng `appPreferences.dataSourceMode`; nhánh REMOTE hiện trả `Resource.Error("Endpoint REMOTE chưa sẵn sàng")` với TODO chờ backend
- 🔒 `AccountRepositoryImpl` — backed by Room (local-only), không thuộc trục MOCK/REMOTE; thông tin tài khoản sẽ sync từ backend trong workflow tương lai

---

## Workflow #38 — Repository API audit (cập nhật 2026-05-16)

Bảng tổng hợp trạng thái endpoint của mỗi repository sau khi hoàn thành P0..P3.

| Repository | Endpoint expected | Endpoint implemented (REMOTE) | MOCK | Status |
|---|---|---|---|---|
| `LoanRepository` | `/api/v1/auth/*`, `/api/v1/loan/*`, `/api/v1/master/*`, `/api/v1/ekyc/*`, `/api/v1/otp/*` | ✅ Tất cả qua `LoanApiService` | ✅ | **Hoàn thiện** |
| `NotificationRepository` | `/api/v1/notifications`, `register-fcm`, `mark-read` | 🟡 Một phần (`getNotifications`, `triggerFcmTest`); `registerFcmToken` + `markAsRead` còn TODO | ✅ Room DB | **Một phần** |
| `HomeRepository` | `/home/banners`, `/home/hot-loans`, `/ekyc/status` | ❌ Chưa có; REMOTE branch trả `Resource.Error` | ✅ `data/sample/SampleHome.kt` | **MOCK only** |
| `EventRepository` | `/events/{id}`, `POST /events/{id}/join` | ❌ Chưa có | ✅ `data/sample/SampleEvents.kt` | **MOCK only** |
| `RewardRepository` | `/rewards/catalog`, `/rewards/user`, `POST /rewards/redeem` | ❌ Chưa có | ✅ `data/sample/SampleRewardCatalog.kt`, `SampleUserRewards.kt` | **MOCK only** |
| `UserRepository` | `GET/PUT /users/me`, `PATCH /users/me/notification-settings` | ❌ Chưa có | ✅ `data/sample/SampleUserProfile.kt` | **MOCK only** |
| `PaymentRepository` | `/payment/wallet`, `/payment/cards`, `/payment/topup`, `/payment/withdraw`, `POST /payment/cards/verify`, `POST /payments/qr`, `GET /payments/qr/{id}/status` | ❌ Chưa có; QR/verify được thêm contract ở workflow #36 | ✅ `data/sample/SamplePayment.kt` + in-memory state | **MOCK only** |
| `AccountRepository` | (Local-only, không có endpoint) | — | ✅ Room | **Local** |
| `TransactionHistoryRepository` | `GET /transactions` | ❌ Chưa có | ✅ `data/sample/SampleTransactionHistory.kt` | **MOCK only** |
| `ChatBotRepository` | `POST /chat/message` (text + card + action component) | ❌ Chưa có | ✅ `data/sample/SampleChatResponses.kt` (rule-based) | **MOCK only** |

### Backend endpoint còn cần bổ sung
- Master data `?lang=vi|en` query (workflow #30) — `LoanApiService` đã thêm `@Query("lang")`; backend cần serve label theo locale.
- QR payment polling — workflow #36 contract đã định nghĩa, đợi backend.
- Notification mark-read API — workflow #14 + #38 audit (`markAsRead` đang local-only).
- ChatBot streaming/component response — workflow #32.

### Không còn UI mock disguise
Sau workflow #22 + #27, các màn UI sau đã không tự dựng mock; đều consume qua repository → ViewModel:
`AccountScreen`, `TransactionHistoryScreen`, `EventDetailScreen`, `LoanListScreen`, `LoanDetailScreen`, `HomeScreen`, `ChatBotScreen`, `RewardScreen`, `LoanManagementScreen`.
