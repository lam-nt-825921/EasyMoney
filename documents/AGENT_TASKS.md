# AGENT_TASKS - EasyMoney Claude Workflow

Cập nhật: 2026-05-26.

File này là backlog chuẩn duy nhất cho agent. Đã gộp từ các ghi chú/task/roadmap cũ theo độ ưu tiên:

1. Task mới nhất là nguồn ưu tiên cao nhất.
2. Ghi chú vấn đề bổ sung các vấn đề chung còn hợp lệ.
3. Roadmap cũ chỉ dùng để bổ sung context.

Ba file nguồn cũ đã được xóa sau khi gộp để tránh agent đọc nhầm backlog trùng lặp.

## Context bắt buộc

- Đọc `documents/SRS.md` để hiểu app làm gì.
- Data layer nằm ở `app/src/main/java/com/example/easymoney/data` và repository nằm ở `domain/repository`.
- UI phải là production UI: không đặt mock trực tiếp trong UI/ViewModel.
- Chế độ dữ liệu có `MOCK` và `REMOTE`, điều khiển bởi `AppPreferences.dataSourceMode` và màn `SandBoxScreen`.
- Khi phát hiện thiếu fetch/API: cập nhật `documents/API_SPEC.md` với endpoint, DTO, mục đích, nơi cần cài.

## P0 - Task mới cần làm trước

### 1. Account support: đổi Account Security thành Đổi mật khẩu

Hiện trạng theo code:
- `AccountScreen.kt` vẫn có `supportMenuItems` gồm Support Center và Account Security.
- `AppNavHost.kt` vẫn để `onNavigateToSupport = { /* Open web center */ }`.
- `SecuritySettingsScreen` có callback `onChangePassword = { /* Handle change password */ }`, chưa có flow đổi mật khẩu thật.

Yêu cầu:
- Bỏ item Support Center khỏi section support của `AccountScreen`.
- Đổi item Account Security thành Đổi mật khẩu.
- Khi click, điều hướng tới màn đổi mật khẩu gồm: mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới, nút xác nhận.
- Submit gọi repository/API đổi mật khẩu. Nếu backend chưa có endpoint, thêm contract và data path vào `API_SPEC.md`.
- Có loading/success/error state và validation: không rỗng, confirm khớp, mật khẩu mới khác mật khẩu cũ.

Acceptance:
- Account screen không còn Support Center.
- CTA Đổi mật khẩu có route rõ ràng, không còn callback trống.
- Password change không mock trong UI.

### 2. ProfileCompletion identity: chuẩn hóa 3 action

Hiện trạng theo code:
- `ProfileCompletionScreen.kt` đã có các module `NFC_READER`, `BIOMETRIC`, `DOCUMENT_UPLOAD`.
- UI hiện eKYC, biometric, upload document, NFC theo nhóm identity document alternative.
- Yêu cầu mới muốn 3 button: eKYC, Device Biometrics, Căn cước công dân. Nút CCCD mở 2 option: Upload document hoặc NFC.

Yêu cầu:
- Chỉnh UI identity thành đúng 3 CTA cấp 1:
  - eKYC.
  - Device Biometrics.
  - Căn cước công dân.
- CTA Căn cước công dân mở bottom sheet/dialog có 2 option:
  - Upload document.
  - NFC.
- NFC và upload document là 2 cách thay thế nhau cho identity document verification; chỉ cần 1 trong 2 thành công.
- Nếu thiết bị không hỗ trợ NFC, option NFC disabled/unsupported.
- Biometric là optional 2FA, không tính là điều kiện bắt buộc hoàn thiện hồ sơ.

Acceptance:
- UI không hiện NFC và upload document như 2 checklist bắt buộc riêng.
- `IdentityVerificationStatus.isIdentityDocumentVerified` được tính theo OR.
- Profile và identity workflow cập nhật cùng một `UserProfile` qua `UserRepository`.

### 3. Đóng bàn phím ảo ở màn edit profile

Phạm vi:
- `EditContactInfoScreen.kt`
- `EditJobInfoScreen.kt`
- `EditPersonalInfoScreen.kt`

Hiện trạng theo code:
- Chưa thấy `LocalFocusManager`, `clearFocus`, `imePadding`, hoặc modifier tap ngoài để đóng keyboard.

Yêu cầu:
- Khi user bấm ra ngoài input, clear focus để đóng keyboard.
- Field cuối nên có IME action Done và clear focus.
- Không làm mất khả năng scroll/form interaction.

Acceptance:
- Nhập liệu xong có thể đóng bàn phím bằng tap ngoài hoặc Done.
- Form không bị nút submit che bởi keyboard.

### 4. LoanManagement UI card/CTA contrast

Hiện trạng theo code:
- `LoanManagementScreen.kt` đã dùng `Card` và Material colorScheme, có Cancel/Sign.
- Cần review bằng light/dark và sửa nếu mô tả khoản vay, background, nút ký/hủy chưa đủ contrast.

Yêu cầu:
- Mỗi khoản vay/hợp đồng là card tách bạch với nền.
- CTA Ký hợp đồng là primary action, CTA Hủy là destructive/secondary rõ ràng.
- Có loading/empty/error state không bị một màu với nền.
- Không hard-code màu làm hỏng dark mode.

Acceptance:
- Light/dark đều đọc được nội dung.
- Nút ký/hủy có phân cấp hành động rõ ràng.

### 5. Lập danh sách fetch API còn thiếu

Yêu cầu:
- Audit tất cả screen/ViewModel xem UI lấy dữ liệu từ repository nào.
- Đối chiếu repository implementation với `LoanApiService`/remote data source.
- Cập nhật `API_SPEC.md` thành danh sách endpoint đã có, endpoint còn thiếu, DTO cần thêm, screen/repository bị ảnh hưởng.

Acceptance:
- Có bảng repository coverage rõ ràng.
- Mỗi API thiếu có endpoint đề xuất, DTO/domain mapping, nơi cần cài.

### 6. Cài đặt các fetch API còn thiếu

Yêu cầu:
- Dựa trên mục 5, cài đặt lần lượt remote branch cho các repository đang trả `REMOTE_NOT_READY`.
- Ưu tiên các luồng người dùng chính: password/profile, payment, home, event, reward, transaction history, chatbot, notification mark-read/FCM token.

Acceptance:
- REMOTE mode không trả lời "Endpoint REMOTE chưa sẵn sàng" cho luồng đã làm.
- Mock mode vẫn hoạt động để dev/QA test offline.

## P1 - Backend integration còn nợ

### 7. Remote branch cho repository đang stub

Theo code hiện tại, các repository sau đã branch MOCK/REMOTE nhưng REMOTE chưa gọi API thật:

- `HomeRepositoryImpl`: banners, hot loans, eKYC status.
- `EventRepositoryImpl`: event detail, join event.
- `RewardRepositoryImpl`: reward catalog, user rewards, redeem.
- `UserRepositoryImpl`: profile, update profile, notification settings.
- `PaymentRepositoryImpl`: wallet/cards/topup/withdraw/card verify/QR/auto deduction.
- `TransactionHistoryRepositoryImpl`: transaction history.
- `ChatBotRepositoryImpl`: send message.

Acceptance:
- Thêm service/DTO/remote data source phù hợp.
- `API_SPEC.md` sync với code.

### 8. Notification end-to-end

Yêu cầu:
- Thêm API đăng ký FCM token.
- Thêm API mark read / mark all read nếu backend hỗ trợ.
- Notification list vẫn dùng Room cache, nhưng REMOTE mode phải sync và update read status về backend.

### 9. Payment và QR

Yêu cầu:
- Wallet info, cards, add/delete/verify card, top-up, withdraw, auto deduction đều gọi backend ở REMOTE.
- QR payment có create intent và poll status.
- Frontend chỉ hiển thị trạng thái backend; không tự xử lý tiền thật.

### 10. Chatbot backend

Yêu cầu:
- `ChatBotRepository.sendMessage` gọi backend.
- Response hỗ trợ text/card/action component.
- Action có thể navigate route, dial phone, open URL.

## P2 - UX/i18n/theme cần tiếp tục

### 11. Dark mode và contrast toàn app

Rà soát các màn chính: Home, Account, Profile, Identity, LoanList, LoanDetail, LoanManagement, Payment, Reward, Event, Notification, Contract.

Acceptance:
- Không còn text/icon mờ khó đọc trong dark mode.
- Màu UI dùng `MaterialTheme.colorScheme` hoặc token theme nội bộ.

### 12. Hard-code string/i18n

Yêu cầu:
- Text UI mới phải vào resource VI/EN.
- Guide XML dùng `@string/...`.
- Master data/backend labels lấy theo `lang=vi|en`, submit id/code ổn định.

### 13. Link/highlight navigation

Yêu cầu:
- Hotline mở dialer.
- Terms/contract mở route đúng.
- CTA event/reward/chatbot có hành vi rõ ràng.

## P3 - Cleanup/testing

- Thêm mapping tests cho DTO quan trọng khi thêm endpoint.
- Chạy `.\gradlew.bat test` sau khi sửa logic repository/mapping.
- Chạy `.\gradlew.bat assembleDebug` sau thay đổi route/UI lớn.
- Cập nhật `API_SPEC.md` trong cùng PR/change set nếu API/data path thay đổi.

## Done từ backlog cũ không đưa lại làm task chính

Các mục sau đã được note là hoàn thành hoặc code hiện tại đã có nên không ưu tiên như backlog mới:

- Home banner sang Loan Detail và check eligibility tại Detail.
- GeneralSettings đổi ngôn ngữ VI/EN.
- Guide XML cho nhiều route đã tồn tại.
- Loan search filter đã có tham số keyword/amount/tenor/interest/hot/new/promotional/lang trong `LoanApiService`.
- Profile và Identity đã bắt đầu tách hub/workflow, cần tiếp tục refine theo P0.2.
