# Hướng dẫn sử dụng và mở rộng hệ thống Notification

Tài liệu này hướng dẫn cách vận hành hệ thống thông báo nội bộ (Local Notification), cấu trúc Model và cách thêm các loại thông báo mới vào ứng dụng EasyMoney.

---

## 1. Cấu trúc Model và Vị trí File

Tuân thủ theo quy định tại `CLEAN_ARCHITECTURE.md`, hệ thống Notification được phân chia như sau:

### A. Data Layer (Room Entity)
- **Vị trí:** `app/src/main/java/com/example/easymoney/data/local/entity/NotificationEntity.kt`
- **Vai trò:** Định nghĩa cấu trúc bảng trong Database.
- **Lưu ý:** Chứa tất cả các trường dữ liệu có thể có của mọi loại thông báo (dùng các trường nullable cho các dữ liệu đặc thù).

### B. UI Layer (UiModel)
- **Vị trí:** `app/src/main/java/com/example/easymoney/ui/notification/model/NotificationUiModel.kt`
- **Vai trò:** Chuyển đổi dữ liệu từ Entity sang dạng giao diện dễ hiểu (màu sắc, icon, định dạng ngày tháng).
- **Hàm chuyển đổi:** `NotificationEntity.toUiModel()` chịu trách nhiệm logic mapping này.

---

## 2. Cách tạo và gửi Thông báo

Để gửi một thông báo, bạn **không** gọi trực tiếp Repository mà phải dùng `AppNotificationManager`.

### Bước 1: Inject Manager vào nơi cần dùng (ViewModel hoặc Component)
```kotlin
@Inject lateinit var notificationManager: AppNotificationManager
```

### Bước 2: Gọi hàm `showNotification`
```kotlin
notificationManager.showNotification(
    userId = "user_123",
    title = "Tiêu đề thông báo",
    content = "Nội dung chi tiết",
    type = "transaction", // Phân loại: transaction, promotion, reminder...
    amount = 500000L,     // (Optional) Cho giao dịch
    transactionCode = "TX123" // (Optional) Mã tham chiếu
)
```
*Hàm này sẽ tự động: Lưu vào Database -> Đẩy thông báo hệ thống (System Tray) -> Hiện badge đỏ.*

---

## 3. Cách mở rộng sang Loại Notification mới (Khác thuộc tính)

Khi bạn cần một loại thông báo mới (ví dụ: **"Món quà may mắn" - gift**), hãy thực hiện các bước sau:

### Bước 1: Cập nhật Entity (Nếu cần)
Nếu loại thông báo mới cần thuộc tính hoàn toàn mới (ví dụ: `giftCode`), hãy thêm vào `NotificationEntity` dưới dạng nullable:
```kotlin
// Trong NotificationEntity.kt
val giftCode: String? = null 
```

### Bước 2: Cập nhật hàm showNotification trong AppNotificationManager
Thêm tham số mới vào hàm `showNotification` để nhận dữ liệu từ bên ngoài.

### Bước 3: Định nghĩa hiển thị trong UiModel
Mở file `NotificationUiModel.kt`, cập nhật logic trong hàm `toUiModel()` để quyết định Icon và Màu sắc cho loại mới:

```kotlin
// Trong NotificationUiModel.kt -> toUiModel()
val icon = when(this.type) {
    "transaction" -> Icons.Default.SwapHoriz
    "promotion" -> Icons.Default.CardGiftcard
    "gift" -> Icons.Default.Redeem // Loại mới
    else -> Icons.Default.Notifications
}

val bgColor = when(this.type) {
    "gift" -> Color(0xFFE91E63) // Màu hồng cho quà tặng
    else -> Color(0xFF4CAF50)
}
```

### Bước 4: Xử lý giao diện Item (Nếu cần)
Trong `NotificationScreen.kt`, các hàm Composable như `TransactionNotificationItem` hoặc `GenericNotificationItem` sẽ dựa vào `type` để vẽ UI. Nếu loại mới có UI quá khác biệt, hãy tạo thêm một Composable mới và gọi nó trong `LazyColumn`.

---

## 4. Nguyên tắc quan trọng
1. **Duy nhất một nguồn:** Luôn dùng `AppNotificationManager` để tạo thông báo để tránh lỗi trùng lặp (duplicate).
2. **Nullable fields:** Các trường đặc thù của loại này (ví dụ `amount` của transaction) phải luôn là nullable để không ảnh hưởng đến loại khác (ví dụ `promotion` không có amount).
3. **Vietnamese literal:** Khi dùng `SimpleDateFormat`, các chữ cái tiếng Việt (như 'Tháng') phải được bọc trong dấu nháy đơn `'...'` để tránh lỗi `Illegal pattern character`.
