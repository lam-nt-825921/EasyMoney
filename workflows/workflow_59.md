# Workflow #59 — P6: Backend/frontend DTO mismatch audit & fixes

## Goal
Sửa các mismatch DTO high-risk giữa backend live response và frontend Retrofit/domain models để REMOTE mode không crash do parse error.

## Requirements

### Functional
- `ApiResponse<T>` (`data/remote/dto/MetadataDto.kt`): đổi `data: T` → `data: T? = null`; mỗi data source check null rõ ràng cho non-unit endpoints; tạo helper `safeUnitApiCall` cho endpoints trả `{}` / `null`.
- `LoanApiService.getMyPackage`: đổi response từ `ApiResponse<LoanPackageModel>` sang `ApiResponse<List<LoanPackageDto>>`. Map list → domain.
- `NotificationDto.amount` / `balanceAfter` → `Double?` (xử lý chính ở workflow #54; ở đây verify consistency).
- `PaymentCard.balance`: tạo `PaymentCardDto` riêng với `balance: Double`, map sang domain.
- `WalletInfoDto`: tạo DTO riêng với money là `Double`, map sang domain.
- `AuthTokenDto`: thêm nested `user: UserProfileDto?` để cache profile sau login.
- `EkycStatusDto`: tạo DTO đầy đủ với `status`, `session_id`, match fields, document method, verified timestamp; map sang domain `EKycStatus` mở rộng nếu UI cần.
- Tách domain models khỏi vai trò Retrofit response cho mọi endpoint money-heavy.
- Backend `Debt.due_date` ignored là OK.

### Files likely involved
- `app/src/main/java/com/example/easymoney/data/remote/dto/MetadataDto.kt`
- `app/src/main/java/com/example/easymoney/data/remote/dto/` (new: `PaymentCardDto.kt`, `WalletInfoDto.kt`, `LoanPackageDto.kt`, `EkycStatusDto.kt`)
- `app/src/main/java/com/example/easymoney/data/remote/LoanApiService.kt`
- `app/src/main/java/com/example/easymoney/data/remote/PaymentRemoteDataSource.kt`
- `app/src/main/java/com/example/easymoney/data/remote/LoanRemoteDataSource.kt`
- `app/src/main/java/com/example/easymoney/data/remote/dto/AuthTokenDto.kt`
- `app/src/main/java/com/example/easymoney/domain/model/PaymentCard.kt`, `WalletInfo.kt`, `LoanPackage.kt`, `EKycStatus.kt`
- `app/src/test/java/com/example/easymoney/data/remote/dto/` — mapping tests

### Acceptance criteria
- [ ] `ApiResponse<T>.data` nullable; data sources check null rõ ràng.
- [ ] `getMyPackage()` parse list trả về.
- [ ] PaymentCard/Wallet parse decimal balance không lỗi.
- [ ] `AuthTokenDto.user` được parse và có thể cache.
- [ ] Có mapping test dùng sample backend live cho: notification, payment card/wallet, loan package my, rewards user, eKYC status.
- [ ] Build passes: `./gradlew assembleDebug`
- [ ] Unit tests pass: `./gradlew test`

## Notes
- Tham chiếu: AGENT_TASKS.md Problem 6 và `documents/backend_contract.yaml`.
- Money strategy: parse Double rồi round/format tập trung ở UI util.
