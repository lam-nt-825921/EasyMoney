# API_SPEC - EasyMoney

Cập nhật: 2026-05-26.

File này là tài liệu backend duy nhất: gồm API contract, endpoint -> DTO -> repository -> screen mapping, và trạng thái remote theo code hiện tại.

Base URL mặc định trong app: `https://easymoney.lamgd.dev/`. Các path bên dưới dùng prefix `/api/v1` nếu không ghi chú khác. Response hiện dùng wrapper `ApiResponse<T>` trong mobile.

## 0. Endpoint đã có trong code

| HTTP | Path | Request | Response | Repository | Screen/ViewModel |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/login` | `LoginRequestDto` | `ApiResponse<AuthTokenDto>` | `LoanRepository.login` | `LoginViewModel` |
| POST | `/api/v1/auth/register` | `RegisterRequestDto` | `ApiResponse<AuthTokenDto>` | `LoanRepository.register` | `LoginViewModel` |
| GET | `/api/v1/loan/package/my` | - | `ApiResponse<LoanPackageModel>` | `LoanRepository.getMyPackage` | loan/onboarding flows |
| GET | `/api/v1/loan/package` | query filter | `ApiResponse<List<LoanPackageModel>>` | `LoanRepository.getLoanPackages` | `LoanDiscoveryViewModel` |
| GET | `/api/v1/master/metadata?lang=` | - | `ApiResponse<MasterDataMetadataDto>` | `LoanRepository.getMasterDataMetadata` | edit profile, loan information form |
| GET | `/api/v1/master/districts/{provinceId}?lang=` | - | `ApiResponse<List<MasterDataItemDto>>` | `LoanRepository.getDistricts` | address selectors |
| GET | `/api/v1/master/wards/{districtId}?lang=` | - | `ApiResponse<List<MasterDataItemDto>>` | `LoanRepository.getWards` | address selectors |
| POST | `/api/v1/ekyc/capture/face` | multipart image/meta | `ApiResponse<EkycCaptureResponse>` | `LoanRepository.captureFace` | `EkycCameraViewModel` |
| POST | `/api/v1/otp/send` | `OtpRequest` | `ApiResponse<Unit>` | `LoanRepository.sendOtp` | contract/eSign OTP |
| POST | `/api/v1/otp/verify` | `OtpVerifyRequest` | `ApiResponse<Unit>` | `LoanRepository.verifyOtp` | contract/eSign OTP |
| GET | `/api/v1/contracts/{contractId}?lang=` | - | `ApiResponse<ContractContentDto>` | `LoanRepository.getContractContent` | `ContractViewModel` |
| GET | `/api/v1/notifications` | - | `ApiResponse<List<NotificationDto>>` | `NotificationRepository.refreshNotifications` | `NotificationViewModel` |
| POST | `/api/v1/test/fcm/trigger` | `FcmTestRequest` | `ApiResponse<Unit>` | `NotificationRepository.triggerFcmTest` | `SandBoxViewModel` |

## 0.1. Repository coverage

| Repository | MOCK | REMOTE hiện tại | Endpoint cần có | Trạng thái |
|---|---:|---|---|---|
| `LoanRepository` | Có | Auth, loan package, master data, face capture, OTP, contract content qua `LoanRemoteDataSource`; một số hàm vẫn mock/local | `/auth/*`, `/loan/package*`, `/master/*`, `/ekyc/capture/face`, `/otp/*`, `/contracts/*`, cần thêm application/eligibility/contracts approved/cancel/provider info/my info | Một phần |
| `NotificationRepository` | Room/local | `GET /notifications`, `POST /test/fcm/trigger`; mark-read/token local/TODO | `/notifications/fcm-token`, `/notifications/{id}/read`, `/notifications/read-all` | Một phần |
| `HomeRepository` | `data/sample` | Trả `REMOTE_NOT_READY` | `/home/banners`, `/home/hot-loans`, `/ekyc/status` | Stub |
| `EventRepository` | `data/sample` | Trả `REMOTE_NOT_READY` | `/events/{id}`, `POST /events/{id}/join` | Stub |
| `RewardRepository` | `data/sample` | Trả `REMOTE_NOT_READY` | `/rewards/catalog`, `/rewards/user`, `/rewards/{itemId}/redeem` | Stub |
| `UserRepository` | `data/sample`/in-memory | Trả `REMOTE_NOT_READY` | `/user/profile`, `/user/profile/avatar`, `/user/notification-settings` | Stub |
| `PaymentRepository` | `data/sample`/in-memory | Trả `REMOTE_NOT_READY` | `/payment/wallet`, `/payment/cards`, `/payment/cards/verify`, `/payment/topup`, `/payment/withdraw`, `/payment/auto-deduction`, `/payments/qr`, `/payments/qr/{id}/status` | Stub |
| `TransactionHistoryRepository` | `data/sample` | Trả `REMOTE_NOT_READY` | `/transactions` | Stub |
| `ChatBotRepository` | `data/sample` rule-based | Trả `REMOTE_NOT_READY` | `/chat/message` | Stub |
| `AccountRepository` | Room | Local-only | sync backend nếu cần sau này | Local |

## 0.2. DTO/domain mapping đang có

### `AuthTokenDto -> AuthToken`

| DTO | JSON | Domain |
|---|---|---|
| `accessToken` | `accessToken` | `accessToken` |
| `refreshToken` | `refreshToken` | `refreshToken` |
| `expiresIn` | `expiresIn` | `expiresIn` |

### `MasterDataItemDto -> MasterDataItem`

| DTO | Domain |
|---|---|
| `id` | `id` |
| `name` | `name` |
| `parentId` | `parentId` |

### `MasterDataMetadataDto -> MasterDataMetadata`

`version`, `expiredAt`, và `masterData` được flatten thành: `provinces`, `professions`, `positions`, `educationLevels`, `maritalStatuses`, `relationships`.

### `NotificationDto -> NotificationEntity`

`NotificationRepositoryImpl.refreshNotifications()` map DTO về Room entity với `userId = "user_123"`, fallback title/type và timestamp nếu cần.

### `ContractContentDto -> String`

Mobile hiện chỉ dùng `content` làm domain result. `contractId/lang` dùng để debug/audit nếu cần.

## 1. Auth và account security

### POST `/api/v1/auth/login`

Request `LoginRequestDto`:

```json
{ "phone": "0900000000", "password": "123456" }
```

Response `AuthTokenDto`:

```json
{ "accessToken": "...", "refreshToken": "...", "expiresIn": 3600 }
```

### POST `/api/v1/auth/register`

Request `RegisterRequestDto`:

```json
{ "phone": "0900000000", "fullName": "Nguyen Van A", "password": "123456" }
```

### POST `/api/v1/auth/change-password` (cần bổ sung)

Request:

```json
{
  "oldPassword": "old",
  "newPassword": "new"
}
```

Response: `ApiResponse<Unit>`.

## 2. Loan

### GET `/api/v1/loan/package`

Query:
- `keyword`
- `minAmount`
- `maxAmount`
- `tenor`
- `minInterest`
- `maxInterest`
- `eligibleOnly`
- `hotOnly`
- `newOnly`
- `promotionalOnly`
- `lang=vi|en`

Response: `ApiResponse<List<LoanPackageModel>>`.

### GET `/api/v1/loan/package/my`

Response theo code hiện tại: `ApiResponse<LoanPackageModel>`.

### Eligibility (cần bổ sung remote)

Đề xuất: `POST /api/v1/loan/package/{packageId}/eligibility`.

Response:

```json
{
  "isEligible": false,
  "reasonCode": "MISSING_PROFILE",
  "message": "Cần hoàn thiện hồ sơ",
  "action": "NAVIGATE_PROFILE"
}
```

### Loan application/contract management (cần bổ sung remote)

- `POST /api/v1/loan/applications`
- `GET /api/v1/loan/contracts/approved`
- `POST /api/v1/loan/contracts/{contractId}/cancel`

## 3. Master data

Tất cả endpoint master data nhận `lang=vi|en`.

- `GET /api/v1/master/metadata?lang=vi`
- `GET /api/v1/master/districts/{provinceId}?lang=vi`
- `GET /api/v1/master/wards/{districtId}?lang=vi`

`/metadata` trả `MasterDataMetadataDto`, gồm provinces, professions, positions, educationLevels, maritalStatuses, relationships. Mobile submit id/code ổn định.

## 4. eKYC và identity

### POST `/api/v1/ekyc/capture/face`

Multipart:
- `image` hoặc field ảnh theo contract backend.
- `meta`: JSON metadata.

Response: `ApiResponse<EkycCaptureResponse>`.

Cần bổ sung cho identity document:
- `POST /api/v1/ekyc/document/upload`
- `POST /api/v1/ekyc/document/nfc`
- `GET /api/v1/ekyc/status`

Rule backend: document verified khi NFC hoặc upload document được verify thành công.

## 5. User profile

Repository hiện đang đề xuất `/users/me`, cần chuẩn hóa về `/api/v1/user/profile` hoặc `/api/v1/users/me` trước khi cài remote.

Contract đề xuất:
- `GET /api/v1/user/profile`
- `PATCH /api/v1/user/profile`
- `POST /api/v1/user/profile/avatar`
- `PATCH /api/v1/user/notification-settings`

Identity status trong profile:

```json
{
  "isFaceVerified": true,
  "isNfcVerified": false,
  "isDocumentUploadVerified": true,
  "isIdentityDocumentVerified": true,
  "isBiometric2faEnabled": false
}
```

## 6. OTP, contract, legal

- `POST /api/v1/otp/send` body `{ "purpose": "SIGN_CONTRACT" }`
- `POST /api/v1/otp/verify` body `{ "otp": "123456", "purpose": "SIGN_CONTRACT" }`
- `GET /api/v1/contracts/{contractId}?lang=vi|en`

`ContractContentDto` hiện có `contractId`, `content`, `lang`. Nên bổ sung `version`, `effectiveAt` khi backend sẵn sàng.

Legal CMS tương lai:
- `GET /api/v1/legal/terms?lang=vi&type=terms_of_use`

## 7. Notifications

Đã có:
- `GET /api/v1/notifications`
- `POST /api/v1/test/fcm/trigger`

Cần bổ sung:
- `POST /api/v1/notifications/fcm-token`
- `POST /api/v1/notifications/{id}/read`
- `POST /api/v1/notifications/read-all`

## 8. Home

Cần bổ sung remote cho `HomeRepository`:
- `GET /api/v1/home/banners?lang=vi`
- `GET /api/v1/home/hot-loans?lang=vi`
- `GET /api/v1/ekyc/status`

## 9. Event

Cần bổ sung remote cho `EventRepository`:
- `GET /api/v1/events/{id}?lang=vi`
- `POST /api/v1/events/{id}/join`

## 10. Rewards

Cần bổ sung remote cho `RewardRepository`:
- `GET /api/v1/rewards/catalog?lang=vi`
- `GET /api/v1/rewards/user?lang=vi`
- `POST /api/v1/rewards/{itemId}/redeem`

Redeem response nên trả điểm còn lại, status và payload quà/voucher/code.

## 11. Payment, wallet, transactions

Cần bổ sung remote cho `PaymentRepository`:
- `GET /api/v1/payment/wallet`
- `GET /api/v1/payment/cards`
- `POST /api/v1/payment/cards/verify`
- `POST /api/v1/payment/cards`
- `DELETE /api/v1/payment/cards/{cardId}`
- `POST /api/v1/payment/topup`
- `POST /api/v1/payment/withdraw`
- `PATCH /api/v1/payment/auto-deduction`
- `POST /api/v1/payments/qr`
- `GET /api/v1/payments/qr/{id}/status`

Cần bổ sung remote cho transaction history:
- `GET /api/v1/transactions`

QR status: `PENDING`, `SUCCESS`, `FAILED`, `EXPIRED`, `CANCELLED`.

## 12. Chatbot

Cần bổ sung remote cho `ChatBotRepository`:

### POST `/api/v1/chat/message`

Request:

```json
{
  "conversationId": "optional",
  "message": "Tôi muốn vay tiền",
  "lang": "vi"
}
```

Response hỗ trợ text/card/action:

```json
{
  "conversationId": "conv_123",
  "message": {
    "id": "msg_1",
    "type": "card",
    "text": "Bạn có thể xem các gói vay phù hợp.",
    "title": "Gói vay đề xuất",
    "actions": [
      { "label": "Xem gói vay", "type": "NAVIGATE_ROUTE", "target": "loan_list" }
    ]
  }
}
```

## 13. Mode switching

Mode lưu trong `AppPreferences.dataSourceMode`.

- `MOCK`: repository lấy từ sample/in-memory/Room.
- `REMOTE`: repository phải gọi remote service. Nếu chưa có endpoint, code hiện trả `Resource.Error("Endpoint REMOTE chưa sẵn sàng...")`.

Sandbox:
1. Mở `SandBoxScreen`.
2. Chọn `MOCK` hoặc `REMOTE`.
3. Đổi base URL nếu cần.
4. Lần gọi repository tiếp theo sẽ đọc mode mới.

## 14. Endpoint cần thêm ưu tiên theo `AGENT_TASKS.md`

| Priority | Endpoint | DTO cần thêm | Repository |
|---|---|---|---|
| P0 | `POST /api/v1/auth/change-password` | `ChangePasswordRequestDto` | `UserRepository` hoặc `AuthRepository` mới |
| P0 | `GET /api/v1/user/profile`, `PATCH /api/v1/user/profile` | `UserProfileDto` | `UserRepository` |
| P1 | `POST /api/v1/notifications/fcm-token`, read APIs | `FcmTokenRequestDto` | `NotificationRepository` |
| P1 | Home endpoints | `BannerDto`, `LoanProductDto`, `EkycStatusDto` | `HomeRepository` |
| P1 | Event endpoints | `EventDto` | `EventRepository` |
| P1 | Reward endpoints | `RewardCatalogItemDto`, `UserRewardsDto`, `RedeemRewardDto` | `RewardRepository` |
| P1 | Payment endpoints | wallet/card/QR DTOs | `PaymentRepository` |
| P1 | `GET /api/v1/transactions` | `TransactionGroupDto` | `TransactionHistoryRepository` |
| P1 | `POST /api/v1/chat/message` | `ChatMessageDto`, action DTO | `ChatBotRepository` |
| P2 | loan application/eligibility/contracts approved/cancel | loan application/contract DTOs | `LoanRepository` |
