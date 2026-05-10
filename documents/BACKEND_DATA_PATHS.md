# BACKEND_DATA_PATHS

Tài liệu này chuẩn hoá đường đi dữ liệu giữa Backend và Mobile app EasyMoney.

## Mục đích

- Ghi rõ endpoint nào phục vụ screen/flow nào.
- Ghi rõ dữ liệu đi từ DTO → domain model → UI như thế nào.
- Làm rõ mode **mock** và **connected** đang được quyết định ở đâu.
- Giảm tình trạng UI nhận đúng log nhưng hiển thị sai do mapping.

## Quy tắc chung

1. UI chỉ gọi `Repository`.
2. `Repository` quyết định dùng mock hay remote.
3. `RemoteDataSource` chịu trách nhiệm gọi API và map DTO.
4. Domain model không nên phụ thuộc trực tiếp vào tên field backend.
5. Nếu backend đổi field, cập nhật tại DTO/mapping trước, không sửa trực tiếp UI.

## Bảng đối chiếu dữ liệu

| Screen / Flow | Repository | Remote DataSource | Backend endpoint | Domain model |
| --- | --- | --- | --- | --- |
| Home |  |  |  |  |
| Loan management |  |  |  |  |
| Reward redemption |  |  |  |  |
| Notifications |  |  |  |  |
| AI Chatbot |  |  |  |  |
| Cashflow / wallet |  |  |  |  |

## Mock vs Connected mode

- **Mock mode**: dùng dữ liệu mẫu để phát triển nhanh UI/UX.
- **Connected mode**: gọi backend thật qua repository/remote data source.
- Cần ghi rõ:
  - mode được đọc từ đâu (`AppPreferences`, sandbox, remote config, v.v.)
  - cách chuyển mode
  - những flow nào bắt buộc phải gọi backend thật

## Mapping notes

- Ghi chú các trường backend hay gặp:
  - snake_case ↔ camelCase
  - nested object
  - list / optional / nullable field
- Với mỗi API quan trọng, nên mô tả:
  - request payload
  - response payload
  - error code
  - fallback khi thiếu dữ liệu

## Cần bổ sung sau

- Endpoint chi tiết cho từng module.
- Bảng mapping DTO ↔ domain model.
- Quy ước cache/local storage nếu có.
- Quy định versioning API nếu backend thay đổi lớn.

