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
- [ ] `documents/PROJECT_STRUCTURE.md` được đọc trước khi triển khai các task này.
