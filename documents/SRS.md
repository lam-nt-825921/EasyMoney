# SRS - EasyMoney

Cập nhật: 2026-05-27.

EasyMoney là ứng dụng Android Kotlin/Jetpack Compose cho vay tiêu dùng, quản lý hồ sơ định danh, hợp đồng vay, ví/thanh toán, thông báo, ưu đãi và chatbot hỗ trợ. App có 2 chế độ dữ liệu: `MOCK` để dev/QA offline và `REMOTE` để gọi backend production/staging thật.

## 1. Kiến trúc nghiệp vụ

Luồng dữ liệu bắt buộc:

`UI Screen -> ViewModel -> Repository -> RemoteDataSource/Room/AppPreferences`

Quy tắc:
- UI không tự tạo mock data cho trải nghiệm production.
- Repository quyết định MOCK/REMOTE theo `AppPreferences.dataSourceMode`.
- Ở `REMOTE`, nếu endpoint chưa sẵn sàng thì trả lỗi rõ ràng, không fallback sang mock success.
- Backend label phụ thuộc ngôn ngữ phải nhận `lang=vi|en`.
- Mobile submit id/code ổn định, không submit label hiển thị.
- Mọi request cần xác định tài khoản phải gửi access token thật; backend không còn được xem là mock.

## 2. Xác thực, tài khoản và bảo mật

Người dùng có thể đăng nhập, đăng ký, quick login bằng tài khoản đã ghi nhớ và logout.

Yêu cầu:
- Account screen không hiển thị Support Center nếu chưa có dịch vụ đích hoặc URL hợp lệ.
- CTA Account Security được thay bằng Đổi mật khẩu.
- Đổi mật khẩu gồm mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới, validation và API submit.
- Device biometric là 2FA optional. Chỉ bật khi thiết bị hỗ trợ và user xác thực thành công bằng `BiometricPrompt`.
- Nếu user đã bật 2FA, các hành động nhạy cảm như rút tiền, ký hợp đồng, submit hồ sơ vay có thể yêu cầu biometric trước khi tiếp tục theo policy backend.

## 3. Hồ sơ và định danh

Profile là hub tổng quan hồ sơ: thông tin cá nhân, công việc/thu nhập, người liên hệ và trạng thái định danh.

Hồ sơ được coi là hoàn thiện khi backend xác nhận:
- Đã xác thực eKYC khuôn mặt/liveness.
- Đã xác thực Căn cước công dân bằng một trong hai cách: upload document hoặc NFC.
- Thông tin CCCD từ eKYC/NFC/document khớp với profile theo rule backend.

Quy tắc trạng thái:
- `identityDocumentVerified = nfcVerified OR documentUploadVerified`.
- `faceVerified` là điều kiện bắt buộc để hoàn thiện hồ sơ.
- `biometricEnabled` là 2FA thiết bị, không tính là điều kiện bắt buộc để hoàn thiện hồ sơ.
- Thiết bị không hỗ trợ NFC phải disable/báo unsupported cho NFC option và hướng user sang upload document.
- Các màn edit hồ sơ dùng master data từ repository/backend theo locale hiện tại.
- Các màn edit profile phải có UX đóng keyboard hợp lý khi tap ra ngoài hoặc bấm Done.

## 4. Cảnh báo hồ sơ chưa hoàn thiện

Tài khoản phải hoàn thiện hồ sơ mới được sử dụng các chức năng quan trọng.

Nguồn sự thật:
- Frontend không tự suy đoán hồ sơ đã hoàn thiện.
- Trạng thái hoàn thiện hồ sơ phải lấy từ endpoint backend, ưu tiên `GET /api/v1/ekyc/status` hoặc field identity status trong `GET /api/v1/user/profile`.
- Backend chịu trách nhiệm xác định profile/eKYC/document có hợp lệ hay không.

Caching để hiển thị UI cảnh báo:
- Sau khi đăng nhập thành công, app fetch trạng thái hồ sơ và cache vào state/repository phù hợp.
- Home/Profile/Loan entry points đọc cache để hiển thị cảnh báo nhanh.
- Khi quay về Home từ edit profile hoặc sau khi edit profile thành công, app refetch trạng thái hồ sơ.
- Nếu refetch lỗi mạng, UI giữ cache gần nhất nhưng phải hiển thị thông báo lỗi để người dùng hiểu trạng thái có thể chưa cập nhật.

Hiển thị:
- Cảnh báo phải nói rõ hành động cần làm: hoàn thiện thông tin cá nhân, xác thực khuôn mặt, upload/NFC CCCD hoặc sửa thông tin không khớp.
- CTA phải điều hướng đến màn phù hợp: `profile`, `identity_verification`, `edit_personal_info`, `edit_job_info`, `edit_contact_info`.

## 5. Khoản vay

Loan discovery/detail theo details-first:
1. Home/banner/list điều hướng sang `LoanDetailScreen`.
2. User xem lãi suất, hạn mức, kỳ hạn, điều kiện, phí, minh họa trả nợ.
3. Chỉ khi user bấm Đăng ký, app mới check eligibility.
4. Nếu đủ điều kiện, vào loan flow; nếu thiếu hồ sơ, hiển thị/dẫn hướng hoàn thiện hồ sơ; nếu bị từ chối, hiển thị lý do.

Trong luồng đăng ký vay:
- Phần eKYC phải gửi đến endpoint backend để match.
- Khi eKYC thành công, backend trả về khóa/trạng thái xác thực cần thiết.
- Khi submit hồ sơ vay, request phải có khóa/trạng thái backend yêu cầu; backend vẫn phải kiểm tra lại, không tin flag frontend.
- Eligibility và application API phải reject nếu eKYC thiếu, expire hoặc mismatch.

Loan list có filter:
- keyword.
- min/max amount.
- tenor.
- min/max interest.
- eligible only.
- hot/new/promotional.
- `lang` cho label/badge.

Loan management:
- Hiển thị hợp đồng đã duyệt, trạng thái, lịch/chi tiết cần thiết.
- CTA Ký hợp đồng là primary action sang eSign.
- CTA Hủy là action có confirm và style destructive/secondary.
- Card và text phải có contrast tốt trong light/dark.

## 6. Hợp đồng, OTP và legal

- Contract/eSign lấy nội dung hợp đồng theo `contractId` và `lang`.
- Backend render nội dung pháp lý; mobile không tự dịch nội dung hợp đồng remote.
- OTP gửi/xác thực theo purpose. Backend tự xác định số điện thoại từ session/token.
- Terms hiện tại có thể dùng resource nội bộ; nếu backend/CMS quản lý legal content thì bổ sung API legal theo locale/version.

## 7. Ví, thanh toán và giao dịch

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

## 8. Home, Event, Reward, Notification, Chatbot

- Home lấy banners, hot loans và eKYC/profile completion status từ repository.
- Event detail và join event đi qua `EventRepository`.
- Reward catalog/user rewards/redeem đi qua `RewardRepository`.
- Notification sync backend -> Room cache -> UI; read status cần sync backend khi endpoint có.
- Các tác vụ FE nhận phản hồi ngay thì hiển thị thông báo trực tiếp bằng UI.
- Các tác vụ bất đồng bộ hoặc xảy ra ngoài phiên hiện tại thì backend phải đẩy notification.
- Chatbot đi qua `ChatBotRepository`, response hỗ trợ text/card/action. Action có thể navigate route, dial phone, open URL khi mobile đã support.

## 9. Lỗi và thông báo người dùng

Lỗi kỹ thuật từ network/backend phải được chuyển thành ngôn ngữ tự nhiên:
- HTTP 401: phiên đăng nhập hết hạn hoặc không hợp lệ, cần đăng nhập lại.
- HTTP 403: tài khoản không có quyền thực hiện thao tác.
- HTTP 409/422: dữ liệu không hợp lệ, hồ sơ thiếu hoặc trạng thái không cho phép tiếp tục.
- HTTP 500: hệ thống đang gặp sự cố, thử lại sau.
- HTTP 503: dịch vụ tạm thời gián đoạn, thử lại sau.
- Timeout/no internet: kiểm tra kết nối mạng.

UI không hiển thị raw status code hoặc exception text cho người dùng cuối, trừ khi ở màn debug/sandbox.

## 10. UI/UX và i18n

- Navigation route tập trung trong `AppDestination.kt`, screen binding trong `AppNavHost.kt`.
- Top bar/bottom bar quản lý tập trung qua app scaffold.
- Mọi text UI production dùng string resource VI/EN.
- Guide XML dùng `@string/...` và route nào `showHelpButton=true` phải có guide XML hợp lệ.
- Light/dark mode phải dùng `MaterialTheme.colorScheme` hoặc token theme nội bộ, tránh hard-code màu làm hỏng contrast.

## 11. Mode switching

Mode lưu trong `AppPreferences.dataSourceMode`.

- `MOCK`: repository lấy từ sample/in-memory/Room.
- `REMOTE`: repository phải gọi remote service. Nếu endpoint chưa có, trả lỗi rõ ràng; không trả mock success.
- Sandbox cho phép chọn mode và base URL để lần gọi repository tiếp theo đọc cấu hình mới.
