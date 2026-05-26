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
| `LoanRepository` | Có | Auth, loan package, master data, face capture, OTP, contract content qua `LoanRemoteDataSource`; một số hàm vẫn mock/local | `/auth/*`, `/loan/package*`, `/master/*`, `/ekyc/session`, `/ekyc/capture/face`, `/ekyc/document/upload`, `/ekyc/document/nfc`, `/ekyc/status`, `/otp/*`, `/contracts/*`, cần thêm application/eligibility/contracts approved/cancel/provider info/my info | Một phần |
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
  "reasonCode": "MISSING_EKYC",
  "message": "Bạn cần hoàn tất xác thực khuôn mặt và căn cước công dân.",
  "action": "NAVIGATE_IDENTITY_VERIFICATION",
  "mismatchedFields": []
}
```

`reasonCode` đề xuất: `MISSING_PROFILE`, `MISSING_EKYC`, `PROFILE_EKYC_MISMATCH`, `LOW_CREDIT_SCORE`, `INCOME_NOT_ELIGIBLE`, `HAS_OVERDUE_CONTRACT`, `PACKAGE_UNAVAILABLE`.

`action` đề xuất: `NAVIGATE_PROFILE`, `NAVIGATE_IDENTITY_VERIFICATION`, `NAVIGATE_LOAN_INFORMATION`, `SHOW_REJECT`.

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

Mục tiêu backend: lưu vết định danh, so khớp dữ liệu giấy tờ với hồ sơ người dùng, cấp trạng thái `identityDocumentVerified`, và chặn luồng vay nếu dữ liệu eKYC không khớp. Phần này cần làm free/self-hosted tối đa; không phụ thuộc dịch vụ OCR/eKYC trả phí nếu chưa có ngân sách.

### 4.0. Trạng thái định danh chuẩn

Mobile hiện có `IdentityVerificationStatus`:

```json
{
  "isFaceVerified": true,
  "isNfcVerified": false,
  "isDocumentUploadVerified": true,
  "isIdentityDocumentVerified": true,
  "isBiometricEnabled": false
}
```

Rule:
- `isIdentityDocumentVerified = isNfcVerified OR isDocumentUploadVerified`.
- `isFaceVerified` là kết quả selfie/liveness/face quality pass.
- `isBiometricEnabled` là 2FA thiết bị, không phải điều kiện bắt buộc để hoàn thiện giấy tờ.
- Hồ sơ đủ điều kiện định danh tối thiểu cho vay khi `isFaceVerified = true`, `isIdentityDocumentVerified = true`, và thông tin giấy tờ khớp profile.

### 4.1. Quy trình eKYC đề xuất

1. Tạo phiên eKYC: backend trả `sessionId`, danh sách step cần làm, trạng thái hiện tại.
2. User chụp selfie: frontend dùng Google ML Kit để precheck mặt, backend nhận ảnh + metadata.
3. User xác thực căn cước công dân bằng 1 trong 2 cách:
   - NFC: đọc chip CCCD nếu thiết bị hỗ trợ.
   - Upload document: chụp/upload ảnh mặt trước/mặt sau CCCD.
4. Backend trích xuất dữ liệu giấy tờ, normalize, so khớp với `UserProfile.personalInfo`.
5. Backend so khớp mặt selfie với ảnh chân dung từ NFC/document nếu có dữ liệu ảnh.
6. Backend lưu `EkycSession`, `EkycCapture`, `IdentityDocument`, `IdentityVerificationResult`, và cập nhật `UserProfile.identityStatus`.
7. Luồng vay gọi eligibility/application API; backend chỉ cho qua khi identity pass.

Không nên lưu ảnh thô vĩnh viễn nếu không cần. Nên lưu object storage private + hash/checksum + metadata, có TTL/retention rõ. Không log base64 ảnh, số CCCD đầy đủ, MRZ/chip raw data, hoặc face embedding.

### 4.2. POST `/api/v1/ekyc/session`

Tạo hoặc resume phiên eKYC.

Request:

```json
{
  "flow": "PROFILE_COMPLETION",
  "lang": "vi",
  "device": {
    "deviceModel": "Pixel 7",
    "osVersion": "Android 14",
    "appVersion": "1.0.0",
    "supportsNfc": true
  }
}
```

Response:

```json
{
  "sessionId": "ekyc_sess_123",
  "status": "IN_PROGRESS",
  "requiredSteps": ["FACE", "IDENTITY_DOCUMENT"],
  "completedSteps": [],
  "availableDocumentMethods": ["NFC", "DOCUMENT_UPLOAD"]
}
```

`availableDocumentMethods` phải bỏ hoặc disable `NFC` nếu thiết bị/app báo không hỗ trợ.

### 4.3. POST `/api/v1/ekyc/capture/face`

Multipart:
- `image`: ảnh selfie, field name nên thống nhất là `image`.
- `meta`: JSON metadata.

Metadata theo `EkycCaptureRequest` hiện tại:

```json
{
  "sessionId": "ekyc_sess_123",
  "flowId": "loan_application",
  "step": "selfie",
  "captureTimestamp": 1770000000000,
  "deviceModel": "Pixel 7",
  "osVersion": "Android 14",
  "appVersion": "1.0.0",
  "cameraLens": "front",
  "imageWidth": 1080,
  "imageHeight": 1920,
  "precheckPassed": true,
  "precheckReasons": ["READY_TO_CAPTURE"],
  "faceBoundingBox": { "left": 120, "top": 240, "right": 820, "bottom": 1040 },
  "qualityScore": 0.86
}
```

Response: `ApiResponse<EkycCaptureResponse>`.

```json
{
  "captureId": "face_cap_123",
  "status": "accepted",
  "reason": null,
  "nextStep": "IDENTITY_DOCUMENT",
  "message": "Ảnh hợp lệ"
}
```

Backend validation tối thiểu:
- Chỉ 1 mặt trong ảnh.
- Face bounding box đủ lớn, không lệch khung quá nhiều.
- Ảnh không quá tối/nhòe.
- Không tin tuyệt đối `precheckPassed`; frontend precheck chỉ là UX. Backend phải tự detect lại.

### 4.4. POST `/api/v1/ekyc/document/upload`

Upload ảnh CCCD khi không dùng NFC.

Multipart:
- `frontImage`: ảnh mặt trước CCCD.
- `backImage`: ảnh mặt sau CCCD, bắt buộc nếu backend cần kiểm tra ngày cấp/nơi cấp/QR.
- `selfieCaptureId`: `face_cap_123`.
- `meta`: JSON `{ "sessionId": "...", "documentType": "VN_CCCD", "captureTimestamp": 1770000000000 }`.

Response:

```json
{
  "documentId": "doc_123",
  "method": "DOCUMENT_UPLOAD",
  "status": "PENDING_REVIEW",
  "extracted": {
    "nationalId": "001*********",
    "fullName": "NGUYEN VAN A",
    "dateOfBirth": "1998-01-20",
    "gender": "MALE",
    "issueDate": "2022-05-10"
  },
  "matchResult": {
    "profileMatched": true,
    "faceMatched": true,
    "mismatchedFields": []
  }
}
```

Free/self-hosted hướng triển khai:
- OCR: dùng Google ML Kit trên client nếu đã có dữ liệu trích xuất, hoặc backend tự host OCR open-source như PaddleOCR/Tesseract. Dịch vụ như Google Vision/AWS Textract/FPT.AI/VNPT eKYC thường mất tiền.
- QR trên CCCD: có thể decode bằng ZXing/ZBar nếu ảnh rõ; backend vẫn phải đối chiếu với OCR/profile.
- Face match: dùng OpenCV DNN/SFace hoặc model self-hosted tương đương để tạo embedding và cosine similarity. Không dùng InsightFace mặc định cho commercial nếu chưa kiểm tra license model; nhiều model phổ biến của InsightFace ghi non-commercial/research.

### 4.5. POST `/api/v1/ekyc/document/nfc`

Submit dữ liệu đọc từ chip CCCD. Nếu app chỉ đọc được payload đã parse, gửi field đã parse; nếu đọc được DG/raw, không log raw data và chỉ lưu phần cần thiết.

Request:

```json
{
  "sessionId": "ekyc_sess_123",
  "selfieCaptureId": "face_cap_123",
  "documentType": "VN_CCCD",
  "nfc": {
    "nationalId": "001234567890",
    "fullName": "NGUYEN VAN A",
    "dateOfBirth": "1998-01-20",
    "gender": "MALE",
    "issueDate": "2022-05-10",
    "expiryDate": "2037-01-20",
    "portraitImageRef": "optional_object_ref",
    "signatureValid": true,
    "chipReadAt": 1770000000000
  }
}
```

Response tương tự document upload nhưng `method = "NFC"` và `status` nên là `VERIFIED` nếu chữ ký chip hợp lệ, dữ liệu khớp profile, mặt khớp selfie.

Rule NFC:
- Nếu `signatureValid = false`, không cấp verified tự động.
- Nếu NFC pass thì `isNfcVerified = true` và `isIdentityDocumentVerified = true`.
- NFC và upload document là 2 phương thức thay thế nhau; không bắt user làm cả hai.

### 4.6. So khớp thông tin giấy tờ với profile

Backend normalize trước khi so khớp:
- `fullName`: bỏ dấu, uppercase, trim khoảng trắng thừa.
- `dateOfBirth`: ISO `yyyy-MM-dd`.
- `nationalId`: chỉ số, đúng độ dài CCCD.
- `gender`: enum `MALE|FEMALE|OTHER`.
- `issueDate`: ISO `yyyy-MM-dd`.

Các field bắt buộc khớp để cho vay:

| Field | Nguồn profile | Nguồn eKYC | Rule |
|---|---|---|---|
| Số CCCD | `personalInfo.nationalId` | NFC/OCR/QR | Phải khớp tuyệt đối |
| Họ tên | `personalInfo.fullName` | NFC/OCR/QR | Khớp sau normalize |
| Ngày sinh | `personalInfo.dateOfBirth` | NFC/OCR/QR | Phải khớp |
| Giới tính | `personalInfo.gender` | NFC/OCR/QR | Nên khớp; nếu thiếu từ OCR thì manual review |
| Ngày cấp | `personalInfo.issueDate` | NFC/OCR/QR | Nên khớp nếu profile có nhập |
| Khuôn mặt | selfie capture | portrait từ NFC/document | `faceMatched = true` nếu có ảnh chân dung giấy tờ |

Nếu mismatch, response phải trả `mismatchedFields` để frontend hiển thị/điều hướng sửa profile.

### 4.7. GET `/api/v1/ekyc/status`

Response:

```json
{
  "sessionId": "ekyc_sess_123",
  "status": "VERIFIED",
  "isFaceVerified": true,
  "isNfcVerified": false,
  "isDocumentUploadVerified": true,
  "isIdentityDocumentVerified": true,
  "isBiometricEnabled": false,
  "documentMethod": "DOCUMENT_UPLOAD",
  "profileMatched": true,
  "faceMatched": true,
  "mismatchedFields": [],
  "missingDocuments": [],
  "message": null,
  "verifiedAt": "2026-05-26T00:00:00Z"
}
```

Status enum đề xuất: `NOT_STARTED`, `IN_PROGRESS`, `PENDING_REVIEW`, `VERIFIED`, `REJECTED`, `EXPIRED`.

### 4.8. eKYC trong luồng vay

Eligibility và submit application phải kiểm tra lại backend-side:

- `POST /api/v1/loan/package/{packageId}/eligibility`: nếu thiếu eKYC trả `isEligible=false`, `reasonCode=MISSING_EKYC`, `action=NAVIGATE_IDENTITY_VERIFICATION`.
- Nếu profile mismatch trả `reasonCode=PROFILE_EKYC_MISMATCH`, `mismatchedFields=[...]`, `action=NAVIGATE_PROFILE`.
- `POST /api/v1/loan/applications`: reject với HTTP 409/422 nếu eKYC bị sửa/expire/mismatch sau bước eligibility.
- Không dựa vào flag frontend để cho qua hồ sơ vay.

Ví dụ response eligibility thiếu eKYC:

```json
{
  "isEligible": false,
  "reasonCode": "MISSING_EKYC",
  "message": "Bạn cần hoàn tất xác thực khuôn mặt và căn cước công dân.",
  "action": "NAVIGATE_IDENTITY_VERIFICATION"
}
```

### 4.9. Thư viện backend free/self-hosted cho face/OCR

Ưu tiên free:
- Face detect/face embedding: OpenCV DNN + SFace là lựa chọn tự host, có tài liệu chính thức cho detect và extract face feature. Tham khảo: https://docs.opencv.org/4.x/d0/dd4/tutorial_dnn_face.html
- OCR document: PaddleOCR hoặc Tesseract self-host; cần tự tune cho CCCD Việt Nam và kiểm thử chất lượng ảnh.
- Barcode/QR: ZXing/ZBar.

Cần chú ý license/chi phí:
- InsightFace mạnh nhưng model/package phổ biến có ghi license non-commercial/research; không dùng commercial trước khi xác nhận license. Tham khảo repo chính: https://github.com/deepinsight/insightface
- Các SaaS eKYC/OCR/face verification như Google Vision, AWS Rekognition/Textract, FPT.AI, VNPT eKYC thường phát sinh chi phí theo lượt.
- Backend tự host sẽ miễn phí tiền API nhưng tốn máy chủ/CPU/GPU, công tune model và trách nhiệm bảo mật dữ liệu sinh trắc học.

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
  "isBiometricEnabled": false
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

### 12.1. Nguyên tắc chatbot backend

Frontend hiện chưa có chatbot thật, chỉ mock. Backend nên trả response theo intent + action để app dễ xử lý, không trả text tự do kèm route tùy tiện.

Chatbot dùng cho hỗ trợ nghiệp vụ chính:
- Gợi ý gói vay.
- Giải thích điều kiện vay/eKYC/hồ sơ.
- Điều hướng user tới màn phù hợp.
- Hỗ trợ ví/thanh toán/giao dịch/hợp đồng/thưởng/sự kiện.
- Gọi hotline khi cần người hỗ trợ.

Không dùng chatbot để tự duyệt vay, sửa số dư ví, ký hợp đồng, hoặc bỏ qua eKYC. Các hành động nhạy cảm chỉ điều hướng tới flow chính; flow chính vẫn gọi API nghiệp vụ và kiểm tra quyền.

### 12.2. Chat request đề xuất

```json
{
  "conversationId": "conv_123",
  "message": "Tôi muốn vay tiền",
  "lang": "vi",
  "clientContext": {
    "currentRoute": "home",
    "dataSourceMode": "REMOTE",
    "appVersion": "1.0.0"
  }
}
```

`conversationId` optional cho tin nhắn đầu tiên. Backend tự gắn `userId` từ access token, không nhận `userId` từ client.

### 12.3. Chat response DTO

```json
{
  "conversationId": "conv_123",
  "message": {
    "id": "msg_001",
    "role": "BOT",
    "type": "CARD",
    "intent": "LOAN_DISCOVERY",
    "text": "Bạn có thể xem các gói vay phù hợp.",
    "title": "Gói vay đề xuất",
    "body": "Lọc theo hạn mức, kỳ hạn và điều kiện hồ sơ.",
    "actions": [
      {
        "label": "Xem gói vay",
        "type": "NAVIGATE_ROUTE",
        "routeKey": "LOAN_LIST",
        "target": "loan_list",
        "params": {}
      }
    ]
  },
  "suggestedReplies": ["Điều kiện vay là gì?", "Tôi cần xác thực CCCD", "Quản lý khoản vay"]
}
```

Enum `message.type`: `TEXT`, `CARD`, `ACTION`.

Enum `action.type`:
- `NAVIGATE_ROUTE`: mở route nội bộ.
- `DIAL_PHONE`: mở dialer.
- `OPEN_URL`: chỉ dùng cho link chính thức, terms/support ngoài app; frontend hiện chưa xử lý, cần bổ sung trước khi backend trả.
- `SHOW_TEXT`: trả lời nhanh không điều hướng.

Frontend hiện có `NavigateRoute` và `DialPhone`; nếu backend trả `OPEN_URL` thì mobile cần thêm `ChatActionTarget.OpenUrl`.

### 12.4. Whitelist route chatbot được phép điều hướng

Backend chỉ trả các route dưới đây. Không trả route login/register/welcome/sandbox/page_guide vì không phù hợp chatbot production.

| routeKey | `target` route | Màn đích | Khi nào dùng |
|---|---|---|---|
| `HOME` | `home` | Home | User muốn về trang chủ/tổng quan |
| `LOAN_LIST` | `loan_list` | Danh sách gói vay | "muốn vay", "gói vay", "lãi suất", "hạn mức" |
| `LOAN_DETAIL` | `loan_detail/{id}` | Chi tiết gói vay | Backend có `packageId` cụ thể để tư vấn |
| `LOAN_MANAGEMENT` | `loan_management` | Quản lý khoản vay/hợp đồng | User hỏi khoản vay đã duyệt, ký/hủy hợp đồng |
| `LOAN_INFORMATION` | `loan_information` | Flow nhập thông tin vay | Chỉ dùng khi eligibility pass hoặc user đang resume hồ sơ |
| `IDENTITY_VERIFICATION` | `identity_verification` | Xác thực eKYC/CCCD/biometric | Thiếu face, thiếu CCCD, NFC/upload document |
| `PROFILE` | `profile` | Hồ sơ cá nhân | Tổng quan hồ sơ hoặc cần sửa thông tin |
| `EDIT_PERSONAL_INFO` | `edit_personal_info` | Sửa thông tin cá nhân | Sai họ tên/CCCD/ngày sinh/ngày cấp |
| `EDIT_JOB_INFO` | `edit_job_info` | Sửa nghề nghiệp/thu nhập | Thiếu thu nhập/nghề nghiệp khi check vay |
| `EDIT_CONTACT_INFO` | `edit_contact_info` | Sửa người liên hệ | Thiếu liên hệ khẩn cấp khi submit hồ sơ |
| `MONEY_MANAGEMENT` | `money_management` | Ví/quản lý tiền | User hỏi số dư, nạp/rút tiền |
| `PAYMENT_CARDS` | `payment_cards` | Thẻ thanh toán | Thêm/xác thực/xóa thẻ |
| `TOP_UP` | `top_up` | Nạp tiền | User muốn nạp tiền |
| `WITHDRAW` | `withdraw` | Rút tiền | User muốn rút tiền; flow chính có thể yêu cầu 2FA |
| `TRANSACTION_HISTORY` | `history` | Lịch sử giao dịch | User hỏi giao dịch/biến động số dư |
| `NOTIFICATIONS` | `notifications` | Thông báo | User hỏi thông báo/trạng thái mới |
| `REWARDS` | `rewards` | Ưu đãi/đổi điểm | User hỏi điểm thưởng/quà/voucher |
| `EVENT_DETAIL` | `event_detail/{id}` | Chi tiết sự kiện | Backend có `eventId` từ campaign/banner |
| `CHATBOT` | `chatbot` | Chatbot | Hiếm dùng; chỉ dùng khi deep link cần mở lại chat |
| `TERMS` | `terms` | Điều khoản | User hỏi điều khoản/chính sách |
| `CONTRACT` | `contract?contractId={contractId}` | Nội dung/ký hợp đồng | User muốn xem/ký hợp đồng cụ thể |

Route có tham số bắt buộc:
- `LOAN_DETAIL`: cần `params.packageId`, backend build `target = "loan_detail/{packageId}"`.
- `EVENT_DETAIL`: cần `params.eventId`, backend build `target = "event_detail/{eventId}"`.
- `CONTRACT`: cần `params.contractId`, backend build `target = "contract?contractId={contractId}"`.

Nếu thiếu tham số, backend không trả route dynamic; trả `LOAN_LIST`, `LOAN_MANAGEMENT`, hoặc text giải thích.

### 12.5. Intent và action mapping chatbot

| intent | Ví dụ user hỏi | Response/action nên trả |
|---|---|---|
| `LOAN_DISCOVERY` | "Tôi muốn vay", "có gói nào lãi thấp" | Card + `NAVIGATE_ROUTE` `LOAN_LIST` |
| `LOAN_DETAIL` | "Gói Easy Cash thế nào" | Card gói vay + `LOAN_DETAIL` nếu tìm được package |
| `LOAN_ELIGIBILITY` | "Tôi có vay được không" | Gọi/tra eligibility summary; thiếu hồ sơ thì `IDENTITY_VERIFICATION`/`PROFILE`, đủ thì `LOAN_INFORMATION` |
| `EKYC_HELP` | "xác thực khuôn mặt", "NFC", "CCCD" | Text hướng dẫn + `IDENTITY_VERIFICATION` |
| `PROFILE_MISMATCH` | "sai thông tin CCCD" | Card mismatch + `EDIT_PERSONAL_INFO` |
| `LOAN_MANAGEMENT` | "khoản vay của tôi", "ký hợp đồng" | `LOAN_MANAGEMENT`, hoặc `CONTRACT` nếu có contractId |
| `PAYMENT_WALLET` | "số dư", "ví" | `MONEY_MANAGEMENT` |
| `TOP_UP` | "nạp tiền" | `TOP_UP` |
| `WITHDRAW` | "rút tiền" | `WITHDRAW` |
| `PAYMENT_CARD` | "thêm thẻ", "xác thực thẻ" | `PAYMENT_CARDS` |
| `TRANSACTION_HISTORY` | "lịch sử giao dịch" | `TRANSACTION_HISTORY` |
| `REWARD` | "đổi điểm", "voucher" | `REWARDS` |
| `EVENT` | "khuyến mãi", "sự kiện" | `EVENT_DETAIL` nếu có eventId, nếu không trả text/card danh sách sau khi có endpoint event list |
| `NOTIFICATION` | "thông báo" | `NOTIFICATIONS` |
| `LEGAL_TERMS` | "điều khoản", "hợp đồng mẫu" | `TERMS` |
| `HUMAN_SUPPORT` | "gặp tư vấn viên", "hotline" | `DIAL_PHONE` với hotline cấu hình backend |
| `UNKNOWN` | Không hiểu intent | Text fallback + suggested replies |

### 12.6. Ví dụ chatbot action

Điều hướng list vay:

```json
{
  "label": "Xem gói vay",
  "type": "NAVIGATE_ROUTE",
  "routeKey": "LOAN_LIST",
  "target": "loan_list",
  "params": {}
}
```

Điều hướng chi tiết gói vay:

```json
{
  "label": "Xem chi tiết",
  "type": "NAVIGATE_ROUTE",
  "routeKey": "LOAN_DETAIL",
  "target": "loan_detail/loan_easy_cash",
  "params": { "packageId": "loan_easy_cash" }
}
```

Điều hướng eKYC:

```json
{
  "label": "Xác thực ngay",
  "type": "NAVIGATE_ROUTE",
  "routeKey": "IDENTITY_VERIFICATION",
  "target": "identity_verification",
  "params": { "reason": "MISSING_EKYC" }
}
```

Gọi hotline:

```json
{
  "label": "Gọi tư vấn viên",
  "type": "DIAL_PHONE",
  "phone": "19001234"
}
```

### 12.7. Backend triển khai free cho chatbot

Để làm free hoàn toàn:
- Giai đoạn 1: rule-based intent bằng keyword + trạng thái user/profile/loan. Không cần LLM.
- Giai đoạn 2: dùng embedding/local model open-source nếu cần hiểu câu tốt hơn, nhưng phải tự host.
- Không gọi OpenAI/Gemini/Claude API nếu mục tiêu là miễn phí hoàn toàn vì sẽ phát sinh chi phí.

Backend nên có bảng/cấu hình:
- `chat_intents`: keyword/pattern theo `lang`.
- `chat_responses`: template text/card theo intent.
- `chat_route_actions`: route whitelist + điều kiện cần param.
- `support_config`: hotline, URL chính thức nếu sau này mở `OPEN_URL`.

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
