# CLAUDE.md - EasyMoney Agent Entry

Đây là entrypoint cho Claude/agent khi làm việc trong repo EasyMoney.

## Thứ tự đọc bắt buộc

1. `documents/AGENT_TASKS.md` - backlog chuẩn hiện tại, đã gộp từ các task/plan cũ.
2. `documents/SRS.md` - mô tả hệ thống và quy tắc nghiệp vụ.
3. `documents/PROJECT_STRUCTURE.md` - package ownership, route ownership, nơi đặt code.
4. `documents/API_SPEC.md` - API contract, endpoint/data path theo code hiện tại và trạng thái remote.

Những file task/plan/backend mapping cũ đã bị gộp/xóa để tránh trùng lặp.

## Nguyên tắc làm việc

- UI/ViewModel không gọi network trực tiếp. Dữ liệu đi theo luồng `UI -> ViewModel -> Repository -> RemoteDataSource/Local`.
- Mock chỉ được đặt ở repository/data source/sample, không hard-code mock trong UI.
- Mọi repository có dữ liệu backend phải tôn trọng `AppPreferences.dataSourceMode` (`MOCK`/`REMOTE`).
- Khi thêm/sửa endpoint: cập nhật `API_SPEC.md`, `LoanApiService.kt` hoặc service tương ứng, DTO/mapping, repository, và test mapping nếu cần.
- Khi sửa UI: kiểm tra route trong `AppDestination.kt` và `AppNavHost.kt`; text mới phải vào `values/strings.xml` và `values-en/strings.xml`.
- Không sửa thư mục build sinh ra: `.gradle/`, `build/`, `app/build/`.

## Build / test nhanh

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

## File hay cần xem

- `app/src/main/java/com/example/easymoney/navigation/AppDestination.kt`
- `app/src/main/java/com/example/easymoney/navigation/AppNavHost.kt`
- `app/src/main/java/com/example/easymoney/data/remote/LoanApiService.kt`
- `app/src/main/java/com/example/easymoney/data/remote/LoanRemoteDataSource.kt`
- `app/src/main/java/com/example/easymoney/domain/repository/*Repository*.kt`
- `app/src/main/java/com/example/easymoney/data/local/AppPreferences.kt`
