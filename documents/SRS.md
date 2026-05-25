# SRS - EasyMoney

Cập nhật: 2026-05-26.

EasyMoney là ứng dụng Android Kotlin/Jetpack Compose cho vay tiêu dùng, quản lý hồ sơ định danh, hợp đồng vay, ví/thanh toán, thông báo, ưu đãi và chatbot hỗ trợ. App có 2 chế độ dữ liệu: `MOCK` để dev/QA offline và `REMOTE` để gọi backend thật.

## 1. Kiến trúc nghiệp vụ

Luôn giữ luồng dữ liệu:

`UI Screen -> ViewModel -> Repository -> RemoteDataSource/Room/AppPreferences`

Quy tắc:
- UI không tự tạo mock data cho trải nghiệm production.
- Repository quyết định MOCK/REMOTE theo `AppPreferences.dataSourceMode`.
- Backend label phụ thuộc ngôn ngữ phải nhận `lang=vi|en`.
- Mobile submit id/code ổn định, không submit label hiển thị.

## 2. Xác thực, tài khoản và bảo mật

Người dùng có thể đăng nhập, đăng ký, quick login bằng tài khoản đã ghi nhớ và logout.

Yêu cầu mới:
- Account screen không hiển thị Support Center nếu chưa có dịch vụ đích.
- CTA Account Security được thay bằng Đổi mật khẩu.
- Đổi mật khẩu gồm mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới, validation và API submit.
- Device biometric là 2FA optional. Chỉ bật khi thiết bị hỗ trợ và user xác thực thành công bằng `BiometricPrompt`.
- Nếu user đã bật 2FA, các hành động nhạy cảm như rút tiền, ký hợp đồng, submit hồ sơ vay có thể yêu cầu biometric trước khi tiếp tục theo policy backend.

## 3. Hồ sơ và định danh

Profile là hub tổng quan hồ sơ: thông tin cá nhân, công việc/thu nhập, người liên hệ, trạng thái định danh.

Identity workflow gồm:
- eKYC face/liveness.
- Căn cước công dân: user chọn một trong hai cách `Upload document` hoặc `NFC`.
- Device Biometrics: 2FA optional, không tính là điều kiện bắt buộc hoàn thiện hồ sơ.

Rule hoàn thiện:
- `identityDocumentVerified = nfcVerified OR documentUploadVerified`.
- Thiết bị không hỗ trợ NFC phải disable/báo unsupported cho NFC option và hướng sang upload document.
- Các màn edit hồ sơ dùng master data từ repository/backend theo locale hiện tại.
- Các màn edit profile phải có UX đóng keyboard hợp lý khi tap ra ngoài hoặc bấm Done.

## 4. Khoản vay

Loan discovery/detail theo details-first:
1. Home/banner/list điều hướng sang `LoanDetailScreen`.
2. User xem lãi suất, hạn mức, kỳ hạn, điều kiện, phí, minh họa trả nợ.
3. Chỉ khi user bấm Đăng ký, app mới check eligibility.
4. Nếu đủ điều kiện, vào `LoanFlow`; nếu thiếu hồ sơ, điều hướng/nhắc hoàn thiện hồ sơ; nếu bị từ chối, hiển thị lý do.

Loan list có filter:
- keyword.
- min/max amount.
- tenor.
- min/max interest.
- eligible only.
- hot/new/promotional.
- lang cho label/badge.

Loan management:
- Hiển thị hợp đồng đã duyệt, trạng thái, lịch/chi tiết cần thiết.
- CTA Ký hợp đồng là primary action sang eSign.
- CTA Hủy là action có confirm và style destructive/secondary.
- Card và text phải có contrast tốt trong light/dark.

## 5. Hợp đồng, OTP và legal

- Contract/eSign lấy nội dung hợp đồng theo `contractId` và `lang`.
- Backend render nội dung pháp lý; mobile không tự dịch nội dung hợp đồng remote.
- OTP gửi/xác thực theo purpose. Backend tự xác định số điện thoại từ session/token.
- Terms hiện tại có thể dùng resource nội bộ; nếu backend/CMS quản lý legal content thì bổ sung API legal theo locale/version.

## 6. Ví, thanh toán và giao dịch

Payment domain gồm:
- Wallet info.
- Payment cards.
- Verify/add/delete card.
- Top up.
- Withdraw.
- Auto deduction.
- QR payment intent và poll status.
- Transaction history.

REMOTE mode phải gọi backend, không tự thay đổi tiền thật ở frontend. MOCK mode có thể mô phỏng số dư/trạng thái để dev.

## 7. Home, Event, Reward, Notification, Chatbot

- Home lấy banners, hot loans, eKYC status từ repository.
- Event detail và join event đi qua `EventRepository`.
- Reward catalog/user rewards/redeem đi qua `RewardRepository`.
- Notification sync backend -> Room cache -> UI; read status cần sync backend khi endpoint có.
- Chatbot đi qua `ChatBotRepository`, response hỗ trợ text/card/action. Action có thể navigate route, dial phone, open URL.

## 8. UI/UX và i18n

- Navigation route tập trung trong `AppDestination.kt`, screen binding trong `AppNavHost.kt`.
- Top bar/bottom bar quản lý tập trung qua app scaffold.
- Mọi text UI production dùng string resource VI/EN.
- Guide XML dùng `@string/...` và route nào `showHelpButton=true` phải có guide XML hợp lệ.
- Light/dark mode phải dùng `MaterialTheme.colorScheme` hoặc token theme nội bộ, tránh hard-code màu làm hỏng contrast.
