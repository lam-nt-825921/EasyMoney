# CLAUDE.md — EasyMoney

Tài liệu này là bản đồ nhanh cho repo `EasyMoney`, giúp định hướng khi đọc/sửa code mà không phải lần theo toàn bộ cây thư mục mỗi lần.

## Nguồn bối cảnh cần đọc trước

Ưu tiên đọc các tài liệu nghiệp vụ sau vì chúng mô tả hành vi sản phẩm và API contract:

- [documents/SRS.md](SRS.md) — đặc tả yêu cầu nghiệp vụ/UX
- [documents/API_SPEC.md](API_SPEC.md) — contract JSON giữa Mobile và Backend
- [documents/PLAN.md](PLAN.md) — trạng thái hoàn thành và roadmap

`README.md` hiện đang trống, nên không phải nguồn bối cảnh hữu ích.

## Tổng quan dự án

- Ứng dụng Android viết bằng Kotlin + Jetpack Compose.
- Điểm vào app:
  - `app/src/main/java/com/example/easymoney/MainActivity.kt`
  - `app/src/main/java/com/example/easymoney/EasyMoneyApplication.kt`
- Dependency injection: Hilt.
- Điều hướng: Navigation Compose.
- Networking: Retrofit + OkHttp + Gson.
- Local storage: Room + DataStore/SharedPreferences wrapper trong `AppPreferences`.
- Tính năng đặc thù: Firebase Messaging, CameraX, ML Kit face detection, Biometrics.

## Cấu trúc thư mục chính

### `app/src/main/java/com/example/easymoney/ui`
UI Compose theo từng luồng:
- `home/` — trang chủ
- `loan/` — khám phá, chi tiết, flow đăng ký vay, eKYC
- `account/` — tài khoản, hồ sơ, cài đặt
- `login/` — đăng nhập/đăng ký/quick login
- `notification/` — thông báo
- `payment/` — quản lý nguồn tiền/thẻ
- `reward/` — đổi điểm
- `sandbox/` — màn test/developer
- `security/` — cài đặt bảo mật
- `guide/` — trang hướng dẫn theo route
- `theme/` — theme và màu sắc

### `app/src/main/java/com/example/easymoney/navigation`
- `AppDestination.kt` định nghĩa route trung tâm và metadata cho top bar/system bar.
- `AppNavHost.kt` chứa toàn bộ mapping route -> screen.
- `AppRoot.kt` là nơi dựng `Scaffold`, top bar, bottom bar và xin quyền thông báo.

### `app/src/main/java/com/example/easymoney/domain`
- `model/` — domain model dùng trong UI và repository.
- `repository/` — interface + implementation cho luồng nghiệp vụ.
- `common/Resource.kt` — wrapper trạng thái dữ liệu.

### `app/src/main/java/com/example/easymoney/data`
- `remote/` — Retrofit service, DTO, remote data source.
- `local/` — Room database/DAO/entity và `AppPreferences`.
- `sample/` — dữ liệu/mẫu thử nếu có.

### `app/src/main/java/com/example/easymoney/di`
- `NetworkModule.kt` — cấu hình Retrofit/OkHttp/Gson.
- `DatabaseModule.kt` — cung cấp Room database và DAO.
- `RepositoryModule.kt` — bind interface repository sang implementation.

## Luồng kiến trúc

Mẫu chung của app:

`UI (Compose Screen / ViewModel)` -> `Repository` -> `RemoteDataSource` hoặc `Local DB / AppPreferences`

Ghi chú quan trọng:
- `LoanRepositoryImpl` đang là nơi kết hợp dữ liệu mock và remote theo `AppPreferences.dataSourceMode`.
- `NetworkModule` đang cấu hình Gson với `FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES`, nhưng một số DTO vẫn dùng `@SerializedName` cho field camelCase của backend.
- Khi thấy dữ liệu fetch đúng trong log nhưng UI hiển thị sai, hãy kiểm tra mapping từ DTO -> domain model trong repository/data source trước.

## Điểm vào điều hướng cần nhớ

- `MainActivity` -> `AppRoot`
- `AppRoot` quản lý:
  - top bar tập trung
  - bottom bar cho các tab chính
  - xin `POST_NOTIFICATIONS` trên Android 13+
- `AppNavHost` quyết định start route dựa trên tài khoản ghi nhớ:
  - có tài khoản nhớ -> `quick_login_1`
  - không có -> `welcome`

## Quy ước làm việc với code

- Không sửa các thư mục sinh ra bởi build như `app/build/`, `build/`, `.gradle/`.
- Khi làm việc với API backend, ưu tiên kiểm tra:
  1. contract trong `documents/API_SPEC.md`
  2. DTO trong `data/remote`
  3. mapping sang `domain/model`
  4. cách ViewModel consume state
- Khi sửa UI, kiểm tra route tương ứng trong `navigation/AppDestination.kt` và `navigation/AppNavHost.kt`.
- Khi thêm dữ liệu mới, ưu tiên giữ domain model sạch, mapping rõ ràng ở repository/data source.

## Nhiệm vụ cần làm tiếp theo

> Đây là backlog các job cần làm tiếp theo của dự án. Khi bắt đầu một task, ưu tiên đọc lại `documents/SRS.md`, `documents/API_SPEC.md`, `documents/PLAN.md`, sau đó kiểm tra `repository`/`remote data source` trước khi chạm vào UI.

### P0 — Nền tảng bắt buộc

- [ ] Bảo đảm mọi truy vấn dữ liệu đều đi qua `Repository`; UI/ViewModel không được gọi thẳng network.
- [ ] Đồng bộ rõ ràng **mock mode** và **connected mode** theo `sandbox`/`AppPreferences` để biết chính xác dữ liệu đang lấy từ đâu.
- [ ] Quét lại toàn bộ repository implementation để xác nhận mapping DTO backend → domain model đúng với snake_case/camelCase.
- [ ] Tạo và duy trì tài liệu chuẩn hoá đường đi dữ liệu backend: endpoint → remote data source → repository → domain model → screen.
- [ ] Tạo tài liệu cấu trúc dự án cho dev mới: package ownership, route ownership, quy tắc naming, nơi đặt state/UI/business logic.
- [ ] Rà soát lại toàn bộ `ui/` để tìm chỗ nào còn xử lý dữ liệu cục bộ hoặc mock mà chưa đi qua repository/backend.

### P1 — Luồng nghiệp vụ cốt lõi

- [ ] Hoàn thiện luồng quản lý dòng tiền: khi nạp/rút/trừ tiền phải xác định rõ luồng thanh toán, có dùng sandbox bank, app wallet hay thẻ.
- [ ] Hoàn thiện đổi điểm thưởng: bấm nhận quà phải mở màn hình xác nhận, gọi API trừ điểm, backend trả về quà (mã giảm giá, thẻ cào, v.v.).
- [ ] Home banner “quản lý khoản vay” phải điều hướng đúng vào giao diện quản lý khoản vay.
- [ ] Giao diện quản lý khoản vay phải hiển thị các hợp đồng đã được hệ thống duyệt; user có 2 lựa chọn: hủy hoặc ký hợp đồng.
- [ ] Nếu user ký hợp đồng thì điều hướng sang luồng `eSign`.

### P2 — Trải nghiệm người dùng & điều hướng

- [ ] Hoàn thiện hệ thống thông báo end-to-end: backend → FCM/notification model → local display → màn danh sách thông báo.
- [ ] Các chỗ UI có số điện thoại chăm sóc khách hàng, hợp đồng, điều khoản được highlight phải điều hướng đúng sang màn hình/ứng dụng tương ứng; nếu chưa có màn hình/route thì phải code mới.
- [ ] Xây dựng giao diện AI chatbot: hỗ trợ trả lời bằng text và bằng component điều hướng ngay trong khung chat.
- [ ] Đảm bảo mọi màn hình mới tuân thủ Material Design, responsive, và không hard-code string; ưu tiên `string resources`.

### P3 — Kiểm tra & dọn nợ kỹ thuật

- [ ] Kiểm tra lại các route/entry point trong `navigation/` để đảm bảo mọi CTA quan trọng đều có điểm đến rõ ràng.
- [ ] Cập nhật TODO/fixme trong codebase sau mỗi đợt hoàn thiện nghiệp vụ để tránh trôi yêu cầu.

## Tài liệu cần có để dev hiểu dự án

- `documents/BACKEND_DATA_PATHS.md` — quy ước endpoint, mapping DTO/domain, luồng mock vs connected, và bảng đối chiếu screen/repository.
- `documents/PROJECT_STRUCTURE.md` — mô tả cấu trúc module/package, route ownership, rule đặt file, và convention khi thêm feature mới.
- `documents/SRS.md` — nghiệp vụ/UX.
- `documents/API_SPEC.md` — contract JSON giữa Mobile và Backend.
- `documents/PLAN.md` — roadmap và trạng thái hoàn thành.

## Build / chạy nhanh

Từ root repo:

```powershell
.\gradlew.bat assembleDebug
```

Kiểm tra lỗi nhanh nếu cần:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
```

## Tài liệu nghiệp vụ liên kết

- eKYC, hồ sơ, khoản vay, UI/UX: [documents/SRS.md](SRS.md)
- API JSON contract: [documents/API_SPEC.md](API_SPEC.md)
- Trạng thái hoàn thành và roadmap: [documents/PLAN.md](PLAN.md)

## Những chỗ hay cần xem khi debug

- `navigation/AppDestination.kt` — route và metadata của screen
- `navigation/AppNavHost.kt` — nơi gắn ViewModel/screen
- `data/remote/LoanApiService.kt` — contract Retrofit
- `data/remote/LoanRemoteDataSource.kt` — mapping remote -> domain
- `domain/repository/LoanRepositoryImpl.kt` — logic chọn mock/remote và filter dữ liệu
- `data/local/AppPreferences.kt` — base URL, data source mode, token lưu cục bộ

## Lưu ý

- `CLAUDE.md` này là tài liệu định hướng nhanh, không thay thế SRS/API spec.
- Nếu business thay đổi, cập nhật tài liệu trong `documents/` trước rồi mới cập nhật code và `CLAUDE.md`.

