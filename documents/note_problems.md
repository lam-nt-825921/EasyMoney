# `note_problems.md` — Requirements & TODOs

Tài liệu này dùng để gom các vấn đề còn tồn đọng thành yêu cầu triển khai rõ ràng, đủ để dev bắt tay làm ngay. Khi đọc tài liệu này, hãy đọc thêm `documents/PROJECT_STRUCTURE.md` để nắm cấu trúc package, nơi đặt UI/ViewModel/Repository và quy ước mock/remote.

## Tài liệu cần đọc trước

- `documents/PROJECT_STRUCTURE.md` — hiểu cấu trúc project, ownership của từng layer và quy ước mở rộng feature.
- `documents/CLAUDE.md` — tổng quan luồng làm việc và các tài liệu liên kết khác.

## Nguyên tắc chung

1. **Không mock ở UI**: mọi mock chỉ được đặt ở lớp trung gian như `RepositoryImpl` hoặc data source mock.
2. **UI chỉ gọi repository**: screen/viewmodel không gọi trực tiếp backend service.
3. **Ưu tiên API thật**: sau khi xử lý mock tạm, phải thay bằng luồng gọi backend đúng chuẩn production.
4. **Không hard-code text**: mọi chuỗi hiển thị phải dùng string resources.
5. **Điều hướng rõ ràng**: mọi CTA/highlight/link phải dẫn đến screen hoặc app đích tương ứng.

## P0 — Việc cần làm ngay

### 0) Chuẩn hoá nguồn dữ liệu MOCK/REMOTE theo Sandbox cho toàn bộ repository

**Hiện trạng đã xác thực**
- `documents/BACKEND_DATA_PATHS.md` đang ghi `HomeRepositoryImpl`, `EventRepositoryImpl`, `RewardRepositoryImpl`, `UserRepositoryImpl`, `PaymentRepositoryImpl`, `AccountRepositoryImpl` vẫn là `MOCK only`.
- `AppPreferences.dataSourceMode` và `SandBoxScreen` đã có cơ chế chọn `MOCK` / `REMOTE`, nhưng nhiều repository chưa branch theo mode.
- `EventDetailScreen` vẫn dựng mock trực tiếp trong UI.
- `ChatBotViewModel` tự sinh câu trả lời bằng rule local, chưa có repository.
- `TransactionHistoryScreen` có `mockTransactions` trực tiếp trong UI.

**Yêu cầu**
- Mọi repository implementation phải có đủ 2 nguồn dữ liệu:
  - `MOCK`: lấy từ `data/sample` hoặc data source mock trung gian.
  - `REMOTE`: gọi endpoint thật qua remote data source/API service.
- Việc chọn nguồn dữ liệu phải thống nhất theo `AppPreferences.dataSourceMode`, do giao diện `sandbox` điều khiển.
- UI và ViewModel không được tự dựng mock data hoặc tự quyết định mock/remote.
- Nếu backend chưa có endpoint, cập nhật contract trong `documents/API_SPEC.md` và coi như backend sẽ cung cấp.

**Tiêu chí hoàn thành**
- `HomeRepositoryImpl`, `EventRepositoryImpl`, `RewardRepositoryImpl`, `UserRepositoryImpl`, `PaymentRepositoryImpl`, `AccountRepositoryImpl`, `LoanRepositoryImpl`, `NotificationRepositoryImpl` đều branch rõ ràng `MOCK/REMOTE`.
- Không còn mock trực tiếp ở `EventDetailScreen`, `ChatBotViewModel`, `TransactionHistoryScreen` và các UI listed ở mục 2.
- `documents/BACKEND_DATA_PATHS.md` được cập nhật lại trạng thái repository/screen sau khi hoàn thiện.

### 1) Chuẩn hoá luồng Home → Loan Detail → Register

**Yêu cầu**
- Khi người dùng bấm banner gợi ý khoản vay ở Home, không kiểm tra điều kiện ngay tại banner.
- Banner phải điều hướng sang màn hình `LoanDetailScreen`.
- Việc kiểm tra đủ điều kiện chỉ thực hiện khi người dùng bấm nút **Đăng ký** ở màn detail.
- Nếu đủ điều kiện thì tiếp tục luồng đăng ký vay.
- Nếu không đủ điều kiện thì hiển thị thông báo phù hợp cho người dùng.

**Tiêu chí hoàn thành**
- Banner Home không còn trigger check eligibility trực tiếp.
- `LoanDetailScreen` là nơi duy nhất thực hiện eligibility check cho action đăng ký.
- Trạng thái đủ/không đủ điều kiện được phản hồi rõ ràng bằng UI message/dialog/toast/snackbar theo chuẩn dự án.

### 2) Loại bỏ mock trực tiếp tại UI

**Yêu cầu**
- Rà soát toàn bộ các màn hình đang mock dữ liệu trực tiếp ở UI.
- Chuyển mock về trung gian, ưu tiên `LoanRepositoryImpl` hoặc repository/data source tương ứng.
- UI chỉ nhận dữ liệu qua repository và state từ ViewModel.

**Danh sách ưu tiên rà soát**
- `AccountScreen`
- `ChatBotScreen`
- `TransactionHistoryScreen`
- `EventDetailScreen`
- `loan/configuration/*`
- `loan/discovery/LoanDetailScreen`
- `loan/discovery/LoanListScreen`
- `loan/flow/LoanRegistrationSuccessScreen`
- `loan/information/*`

**Tiêu chí hoàn thành**
- Không còn dữ liệu mock hard-coded trong UI layer cho các màn trên.
- Mock chỉ xuất hiện ở repository/data source trung gian khi cần.
- Dữ liệu thật từ backend được ưu tiên khi endpoint đã sẵn sàng.

### 2.1) Hoàn thiện hệ thống hướng dẫn theo từng màn hình

**Hiện trạng đã xác thực**
- `AppNavigationBar` có nút Help dùng `showHelpButton`.
- `AppRoot` điều hướng Help sang `AppDestination.PageGuide.createRoute(xmlName = destination.guideXmlName, ...)`.
- `AppDestination.PageGuide.DEFAULT_XML_NAME` là `guide_default_updating`.
- Hiện chỉ có các layout guide: `guide_home.xml`, `guide_loan_detail.xml`, `guide_onboarding.xml`, `guide_default_updating.xml`.
- Nhiều destination đang `showHelpButton = true` nhưng không có `guideXmlName`, ví dụ `ConfirmInformation`, `Contract`, `EventDetail`, `Rewards`, `LoanList`, `Profile`, `MoneyManagement`, `PaymentCards`, `GeneralSettings`, `SecuritySettings`, `IdentityVerification`, `TopUp`, `Withdraw`, `LoanManagement`, `EditPersonalInfo`, `EditJobInfo`, `EditContactInfo`.

**Yêu cầu**
- Với mỗi màn hình có thao tác nghiệp vụ đáng kể, tạo guide XML riêng và set `guideXmlName` đúng trong `AppDestination`.
- Với màn hình quá đơn giản hoặc không cần hướng dẫn, override `showHelpButton = false` ở `AppDestination` hoặc qua `AppTopBarOverride`.
- Không để Help mở `guide_default_updating` trong production cho các màn có nghiệp vụ chính.

**Tiêu chí hoàn thành**
- Mỗi route có `showHelpButton = true` đều có `guideXmlName` trỏ tới XML tồn tại trong `res/layout`.
- Các màn không cần guide không hiển thị nút Help.
- QA có thể mở Help từ từng màn chính và thấy hướng dẫn thao tác đúng màn đó.

## P1 — Nhiệm vụ cần hoàn thiện sớm

### 3) Cập nhật giao diện `GeneralSettingsScreen` để đổi ngôn ngữ thật

**Yêu cầu**
- Nút đổi ngôn ngữ phải thực sự chuyển ngôn ngữ app giữa `vi` và `en`.
- Việc đổi ngôn ngữ phải dùng cơ chế chuẩn của Android/AppCompat.
- Sau khi đổi ngôn ngữ, UI phải cập nhật lại text/resource tương ứng.

**Tiêu chí hoàn thành**
- Người dùng bấm đổi ngôn ngữ và app đổi được giữa tiếng Việt và tiếng Anh.
- Trạng thái ngôn ngữ được lưu và áp dụng ổn định sau khi mở lại app.

### 4) Hoàn thiện resource tiếng Anh

**Yêu cầu**
- Bổ sung và hoàn thiện `values-en/strings.xml`.
- Tất cả text trong các màn hình đã chuyển sang resource phải có bản dịch tiếng Anh tương ứng.
- Không để thiếu key gây fallback sai hoặc hiển thị text không nhất quán.

**Tiêu chí hoàn thành**
- Các screen hỗ trợ đổi ngôn ngữ hiển thị đúng bản dịch EN/VI.
- Không còn chuỗi tiếng Việt hard-code lẫn vào màn hình đa ngôn ngữ.

### 5) Hoàn thiện các màn đang hard-code / dùng R string dang dở

**Yêu cầu**
- Chuyển toàn bộ text hard-code sang string resources ở các màn và component còn lại.
- Nếu text là hành động điều hướng, phải gắn đúng callback/route.

**Đối tượng ưu tiên**
- `common/components/OtpDialog`
- `DocumentUploadModule`
- `FaceCaptureModule`
- `NfcReaderModule`
- `ContractScreen`
- `EsignSuccessScreen`
- `loan/components/LoanBottomButton`
- `loan/components/LoanExitDialog`
- `loan/components/LoanStepper`

**Tiêu chí hoàn thành**
- Không còn hard-code text ở các component/màn hình liệt kê trên.
- Những text có tính chất link/highlight có điều hướng đúng chức năng.

## P2 — Hoàn thiện nghiệp vụ và mở rộng backend integration

### 6) Đồng bộ dữ liệu hồ sơ và lịch sử giao dịch từ repository/backend

**Yêu cầu**
- `AccountScreen` phải lấy tên và số điện thoại từ repository.
- `TransactionHistoryScreen` phải hiển thị danh sách dữ liệu thật hoặc mock trung gian đúng chuẩn, không mock trực tiếp trong UI.
- `EventDetailScreen` phải lấy dữ liệu từ repository thay vì dựng mock tại UI.

**Tiêu chí hoàn thành**
- UI không tự sinh dữ liệu demo cho các màn trên.
- Data flow tuân thủ UI → ViewModel → Repository.

### 7) Hoàn thiện luồng loan-related screens

**Yêu cầu**
- `loan/configuration/*` phải dùng string resources và nhận dữ liệu qua repository.
- `loan/discovery/LoanListScreen` và `LoanDetailScreen` phải hoàn thiện text/resource và data flow.
- `LoanRegistrationSuccessScreen` phải chuyển toàn bộ text sang resource.
- `loan/information/*` phải lấy option theo ngôn ngữ app hiện tại khi gọi backend.

**Tiêu chí hoàn thành**
- Các màn loan hiển thị đúng ngôn ngữ theo locale.
- Option backend như tỉnh/thành, nghề nghiệp, tình trạng hôn nhân... trả về đúng ngôn ngữ đã chọn.

### 7.1) Mở rộng bộ lọc tìm kiếm khoản vay

**Hiện trạng đã xác thực**
- `LoanDiscoveryUiState` chỉ có `minAmount`, `maxAmount`, `tenor`, `eligibleOnly`.
- `LoanListScreen` hiện chỉ có slider hạn mức và switch "Chỉ hiện gói đủ điều kiện".
- `LoanRepository.getLoanPackages(...)` chưa nhận bộ lọc lãi suất, hot/new/ưu đãi hoặc keyword.

**Yêu cầu**
- Thiết kế lại UI bộ lọc `LoanListScreen` đẹp hơn, dễ quét và có trạng thái filter đang áp dụng.
- Bổ sung filter:
  - Khoảng lãi suất.
  - Kỳ hạn.
  - Gói hot.
  - Gói mới.
  - Gói ưu đãi/khuyến mãi.
  - Chỉ gói đủ điều kiện.
  - Từ khóa/tên gói nếu UX cần.
- Cập nhật domain model/API để backend trả metadata tương ứng: `isHot`, `isNew`, `isPromotional`, `badges`, `interestRate`, `tenorRange`.
- Repository phải truyền filter xuống backend ở REMOTE mode và filter từ sample data ở MOCK mode.

**Tiêu chí hoàn thành**
- Người dùng lọc được theo các tham số trên và có nút reset filter.
- Filter hoạt động nhất quán ở cả MOCK và REMOTE.
- API contract trong `documents/API_SPEC.md` mô tả đủ query params.

### 7.2) Bắt buộc master data theo ngôn ngữ cho hồ sơ và LoanFlow

**Hiện trạng đã xác thực**
- `EditProfileViewModel.loadMasterData()` gọi `loanRepository.getProfessions()`, `getPositions()`, `getEducationLevels()`, `getMaritalStatuses()`, `getRelationships()` nhưng interface không có tham số ngôn ngữ.
- `LoanInformationFormViewModel.loadMasterData()` gọi `loanRepository.getMasterDataMetadata()` cũng không truyền `lang/locale`.
- `LoanApiService.getMasterDataMetadata()`, `getDistricts()`, `getWards()` chưa có query language.

**Yêu cầu**
- Repository/API master data phải nhận tham số ngôn ngữ hiện tại (`vi`/`en`) hoặc đọc locale từ app và truyền xuống backend.
- Áp dụng cho cả màn sửa hồ sơ (`EditPersonalInfoScreen`, `EditJobInfoScreen`, `EditContactInfoScreen`) và `LoanInformationFormViewModel` trong `LoanFlow`.
- Dropdown phải lưu bằng id/code ổn định, không chỉ lưu text hiển thị, để đổi ngôn ngữ không làm hỏng dữ liệu đã chọn.

**Tiêu chí hoàn thành**
- Khi app ở tiếng Việt/Anh, dropdown nhận label đúng ngôn ngữ từ backend.
- Dữ liệu submit dùng id/code ổn định; label chỉ dùng để hiển thị.
- `documents/API_SPEC.md` mô tả query `lang` cho master data.

### 8) Gắn điều hướng đúng cho các text highlight / link trong UI

**Yêu cầu**
- Text highlight như số điện thoại chăm sóc khách hàng, hợp đồng, điều khoản phải điều hướng đúng tới screen hoặc ứng dụng điện thoại tương ứng.
- Nếu chưa có screen đích thì phải tạo mới.

**Tiêu chí hoàn thành**
- Mọi link/hotline/contract/terms đều có hành vi bấm rõ ràng và có thể kiểm thử được.

## P3 — Chuẩn hoá hệ thống và tài liệu

### 9) Hoàn thiện chatbot theo backend

**Yêu cầu**
- `ChatBotScreen` phải gọi backend thật qua repository.
- Chức năng trả lời không chỉ là text mà cần hỗ trợ component điều hướng trong khung chat nếu backend trả về dạng action/component.

**Tiêu chí hoàn thành**
- Không còn chatbot mock hoàn toàn ở UI.
- Chatbot có thể hiển thị phản hồi dạng text + component điều hướng.

### 9.1) Cải thiện UI các màn đang đơn điệu

**Hiện trạng đã xác thực**
- `RewardScreen` dùng grid card đơn giản, nhiều text hard-code và visual gift placeholder.
- `EventDetailScreen` dùng hero placeholder icon và mock content trong UI.
- `ChatBotScreen` chỉ có bubble cơ bản, action button đơn giản.
- `LoanManagementScreen` chỉ là danh sách contract card cơ bản với 2 nút.

**Yêu cầu**
- Thiết kế lại các màn `RewardScreen`, `EventDetailScreen`, `ChatBotScreen`, `LoanManagementScreen` theo UX giàu thông tin hơn, có loading/error/empty state tốt và responsive.
- Dữ liệu hiển thị phải đến từ repository/backend; UI không dựng mock.
- `RewardScreen`: có phân loại quà, trạng thái đổi quà, lịch sử/ưu đãi nổi bật, confirm redeem đẹp và kết quả trả về từ backend.
- `EventDetailScreen`: hero image thật từ backend, timeline/điều kiện tham gia, CTA theo `interactionType`.
- `ChatBotScreen`: hỗ trợ message text/card/action component từ backend, trạng thái typing/error/retry, action điều hướng rõ ràng.
- `LoanManagementScreen`: hiển thị hợp đồng đã duyệt, trạng thái, lịch trả nợ, CTA hủy/ký hợp đồng, yêu cầu xác thực 2 bước nếu user bật.

**Tiêu chí hoàn thành**
- Các màn trên không còn UI tạm/placeholder làm trải nghiệm chính.
- Các action quan trọng có loading/error/success feedback.
- Text được đưa vào string resources và hỗ trợ dark mode.

### 9.2) Chuẩn hoá xác thực sinh trắc học thiết bị và dùng optional 2FA

**Hiện trạng đã xác thực**
- `BiometricModule` có gọi `androidx.biometric.BiometricPrompt.authenticate(...)`.
- `SecurityViewModel.isBiometricSupported` đang hard-code `true // Mocked` và toggle chỉ update state local.
- `ProfileCompletionScreen` coi "Sinh trắc học thiết bị" như một task hồ sơ, trong khi yêu cầu mới là 2FA optional.
- `LoanFlowScreen` chưa dùng `BiometricModule` khi xác nhận/gửi hồ sơ vay.

**Yêu cầu**
- Kiểm tra capability thật bằng `BiometricManager` trước khi bật/tắt biometric.
- Thiết kế popup/xác nhận UI đơn giản cho 2 loại phổ biến: vân tay và FaceID/face unlock, nhưng vẫn dùng `BiometricPrompt` hệ thống để người dùng xác thực bằng cảm biến thiết bị.
- Biometric là 2FA optional theo cài đặt người dùng, không bắt buộc mặc định trong hồ sơ.
- Lưu trạng thái bật/tắt qua repository/backend hoặc secure local storage theo policy.
- Tích hợp vào `LoanFlow`: trước bước submit/xác nhận cuối, nếu user bật biometric 2FA thì yêu cầu xác thực thành công mới tiếp tục.
- Rút tiền/ký hợp đồng cũng dùng cùng cơ chế xác thực này nếu nghiệp vụ yêu cầu.

**Tiêu chí hoàn thành**
- Thiết bị không hỗ trợ biometric hiển thị trạng thái unsupported và không cho bật.
- User bật/tắt biometric thật qua luồng xác thực thành công.
- `LoanFlow` chặn submit khi user đã bật 2FA nhưng xác thực thất bại/hủy.

### 9.3) Sửa logic hoàn thiện hồ sơ định danh: NFC CCCD hoặc hồ sơ giấy tờ là đủ

**Hiện trạng đã xác thực**
- `IdentitySection` hiển thị riêng `Quét thẻ chip CCCD (NFC)` và `Tải hồ sơ/giấy tờ`.
- `DocumentUploadModule` trả mock result, nhưng `ProfileCompletionScreen` chỉ `closeModule()` và không cập nhật trạng thái document verified.
- `IdentityVerificationStatus` hiện có `isNfcVerified`, chưa có trạng thái document upload verified rõ ràng.

**Yêu cầu**
- NFC CCCD và upload giấy tờ là 2 phương thức thay thế nhau để xác nhận thông tin định danh; chỉ cần 1 trong 2 thành công là đủ cho phần định danh giấy tờ.
- Tách rõ:
  - eKYC face/liveness.
  - Identity document verification: `NFC` OR `DOCUMENT_UPLOAD`.
  - Biometric 2FA optional, không tính là điều kiện hoàn thiện hồ sơ bắt buộc.
- Cập nhật model/backend contract để có trạng thái `documentVerified`, `nfcVerified`, `identityDocumentVerified` hoặc tương đương.

**Tiêu chí hoàn thành**
- Hồ sơ không còn yêu cầu đồng thời cả NFC và document upload.
- UI hiển thị rõ người dùng có thể chọn một trong hai phương thức.
- Backend/profile completion tính hoàn thiện đúng theo điều kiện OR.

### 9.4) Hoàn thiện Payment: nạp/rút, tự động thanh toán, thẻ và QR

**Hiện trạng đã xác thực**
- `PaymentRepositoryImpl` chỉ mock in-memory balance/cards, không branch REMOTE.
- `PaymentRepository.addPaymentCard()` chỉ add local, chưa xác minh thẻ qua API.
- `PaymentCardsScreen` có nút thêm thẻ nhưng chưa thấy form nhập/xóa thẻ đầy đủ; card holder/expiry còn hard-code.
- `TopUpScreen` và `WithdrawScreen` là form cơ bản, chưa có QR flow hoặc trạng thái giao dịch realtime/polling.

**Yêu cầu**
- `PaymentRepository` phải hỗ trợ MOCK/REMOTE cho wallet, cards, top-up, withdraw, auto deduction.
- Nhập thẻ/xóa thẻ phải gọi API backend để xác minh thẻ hợp lệ trước khi lưu hoặc xóa.
- Hoàn thiện UI nạp/rút/tự động thanh toán đẹp hơn, có trạng thái giao dịch rõ ràng.
- Thêm luồng thanh toán bằng QR:
  - User nhập số tiền.
  - Frontend gọi API tạo QR/payment intent.
  - UI hiển thị QR code và trạng thái chờ thanh toán.
  - Frontend poll/subscribe trạng thái giao dịch và hiển thị success/failed/expired/cancelled.
- Backend xử lý tiền thật, frontend chỉ hiển thị trạng thái theo API.

**Tiêu chí hoàn thành**
- Thêm/xóa thẻ có validation từ backend và xử lý lỗi rõ ràng.
- QR payment hiển thị được pending/success/failed/expired.
- Auto deduction toggle gọi API và đồng bộ lại wallet info.
- Payment docs/API contract được cập nhật.

### 9.5) Cải thiện UI sửa thông tin hồ sơ

**Hiện trạng đã xác thực**
- `EditPersonalInfoScreen`, `EditJobInfoScreen`, `EditContactInfoScreen` còn form đơn giản.
- Dropdown đã có gọi repository lấy master data ở `EditProfileViewModel`, nhưng chưa truyền ngôn ngữ và chưa lưu id/code ổn định.

**Yêu cầu**
- Thiết kế lại các màn sửa thông tin cá nhân, công việc/thu nhập, người liên hệ để đẹp hơn, có grouping, helper text, validation inline, loading/error state.
- Dropdown phải lấy dữ liệu từ backend/repository theo ngôn ngữ app hiện tại.
- Các field như giới tính, nghề nghiệp, chức vụ, học vấn, tình trạng hôn nhân, quan hệ nên dùng selector từ master data thay vì text input tự do khi có master data.
- Contact picker cần chuẩn hóa số điện thoại trước khi lưu.

**Tiêu chí hoàn thành**
- Form có validation rõ ràng và không submit khi dữ liệu sai.
- Dropdown hiển thị đúng locale và submit id/code.
- Save profile gọi repository/backend, có loading/success/error feedback.

### 10) Rà soát toàn bộ repository để đảm bảo đã gọi API thật

**Yêu cầu**
- Kiểm tra từng repository implementation để xác nhận có gọi backend thật khi endpoint đã có.
- Chỗ nào chưa gọi thật thì bổ sung theo pattern của các API đã hoàn thiện.
- Nếu cần mock thì chỉ mock tại repositoryImpl hoặc trung gian, không đặt ở UI.

**Tiêu chí hoàn thành**
- Có danh sách các màn/repository đã kết nối backend và các màn còn cần bổ sung.
- Không còn tình trạng UI tưởng là production nhưng thực tế đang dựng dữ liệu cục bộ.

## Ghi chú triển khai

- Khi sửa một màn hình, kiểm tra luôn route trong `navigation/` và repository liên quan trong `domain/repository/`.
- Khi thêm text mới, cập nhật cả `values/strings.xml` và `values-en/strings.xml`.
- Khi thêm ngôn ngữ hoặc đổi ngôn ngữ, đảm bảo app đọc đúng locale hiện tại trước khi gọi backend cho các option phụ thuộc ngôn ngữ.

## Checklist nghiệm thu tổng

- [ ] Home banner điều hướng sang detail thay vì check điều kiện trực tiếp.
- [ ] Check đủ điều kiện chỉ diễn ra tại nút Đăng ký trong detail.
- [ ] `GeneralSettingsScreen` đổi được giữa `vi` và `en`.
- [ ] `values-en/strings.xml` được hoàn thiện.
- [ ] Không còn mock trực tiếp ở UI cho các màn đã liệt kê.
- [ ] Các màn loan sử dụng repository/backend đúng chuẩn.
- [ ] Chatbot gọi backend thật và hỗ trợ component điều hướng.
- [ ] Các link/hotline/terms/contracts điều hướng đúng.
- [ ] Sandbox chọn được MOCK/REMOTE và mọi repository tuân thủ mode này.
- [ ] Mỗi màn có nút Help đều có guide riêng hoặc đã tắt Help hợp lý.
- [ ] Biometric 2FA optional hoạt động thật và được dùng trong LoanFlow khi user bật.
- [ ] NFC CCCD hoặc upload giấy tờ chỉ cần một phương thức thành công để hoàn thiện phần định danh giấy tờ.
- [ ] Payment hỗ trợ xác minh thẻ qua API, QR payment và trạng thái giao dịch.
- [ ] Loan search filter hỗ trợ lãi suất, hot, new, ưu đãi và các tham số cần thiết.
- [ ] `documents/PROJECT_STRUCTURE.md` được đọc trước khi triển khai các task này.
