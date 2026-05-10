# PROJECT_STRUCTURE

Tài liệu này mô tả cấu trúc dự án EasyMoney để dev mới nắm nhanh nơi đặt code, nơi xử lý logic, và quy ước mở rộng feature.

## Mục tiêu

- Xác định ownership của từng package/module.
- Giúp tìm nhanh screen, ViewModel, repository, remote/local source.
- Giảm việc code logic nghiệp vụ lẫn vào UI.
- Giữ cấu trúc dễ mở rộng khi thêm feature mới.

## Cấu trúc chính

### `app/src/main/java/com/example/easymoney/ui`
Chứa Compose UI theo luồng nghiệp vụ.

- `home/` — màn hình trang chủ
- `loan/` — khám phá khoản vay, chi tiết, đăng ký, eKYC, quản lý hợp đồng
- `account/` — tài khoản, hồ sơ, cài đặt
- `login/` — đăng nhập, đăng ký, quick login
- `notification/` — danh sách thông báo
- `payment/` — nguồn tiền, thẻ, ví
- `reward/` — đổi điểm, quà tặng
- `sandbox/` — màn test/dev để kiểm tra mock/connected mode
- `security/` — bảo mật, biometrics, PIN
- `guide/` — màn hướng dẫn theo route
- `theme/` — màu sắc, typography, theme Compose

### `app/src/main/java/com/example/easymoney/navigation`
Chứa route trung tâm và điều hướng.

- `AppDestination.kt` — khai báo route, metadata top bar/system bar
- `AppNavHost.kt` — map route → screen
- `AppRoot.kt` — scaffold tổng, bottom bar, permission, entry point UI

### `app/src/main/java/com/example/easymoney/domain`
Chứa domain thuần.

- `model/` — model dùng trong business/UI
- `repository/` — interface + implementation
- `common/Resource.kt` — wrapper state

### `app/src/main/java/com/example/easymoney/data`
Chứa dữ liệu và mapping.

- `remote/` — Retrofit service, DTO, remote data source
- `local/` — Room, DAO, entity, `AppPreferences`
- `sample/` — dữ liệu mẫu nếu cần

### `app/src/main/java/com/example/easymoney/di`
Dependency injection.

- `NetworkModule.kt`
- `DatabaseModule.kt`
- `RepositoryModule.kt`

## Quy tắc đặt logic

1. UI chỉ giữ state hiển thị và user events.
2. ViewModel điều phối state, không gọi network trực tiếp nếu có repository.
3. Repository quyết định mock/remote và map dữ liệu.
4. Remote DataSource chỉ nên lo API call + DTO mapping.
5. Local data source chỉ lo cache/local persistence.

## Quy tắc mở rộng feature

Khi thêm feature mới:

1. Xác định route trong `navigation/`.
2. Tạo screen trong `ui/<feature>/`.
3. Tạo ViewModel cho state và event.
4. Thêm repository method.
5. Nếu cần backend: thêm DTO/service/remote data source.
6. Nếu cần lưu local: thêm entity/DAO/preference.
7. Nếu có text mới: dùng `string resources`, không hard-code.

## Quy ước UI

- Responsive trên nhiều kích thước màn hình.
- Tuân thủ Material Design.
- Ưu tiên component tái sử dụng.
- Mọi CTA quan trọng phải có route rõ ràng.

## Cần cập nhật thêm sau

- Sơ đồ luồng screen → ViewModel → Repository.
- Bảng route đầy đủ theo `AppDestination.kt`.
- Quy ước đặt tên file theo từng feature.
- Danh sách integration point với backend.

