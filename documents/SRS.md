# Tài liệu Đặc tả Yêu cầu Nghiệp vụ (SRS) - EasyMoney
**Phiên bản:** 3.0 (Cập nhật 09/05/2026)

Tài liệu này quy định chi tiết các luồng nghiệp vụ và trải nghiệm người dùng (UX) cho ứng dụng EasyMoney, tập trung vào hệ thống định danh, trải nghiệm khoản vay và quản lý tài chính cá nhân.

---

## 1. Hệ thống Định danh và Hồ sơ (Identity & Profile)

### 1.1 Cơ chế eKYC Đa lớp
Hệ thống kết hợp 3 lớp xác thực để đảm bảo tính an toàn cao nhất:
*   **Lớp 1 - OCR:** Trích xuất thông tin tự động từ ảnh chụp CMND/CCCD.
*   **Lớp 2 - Face Matching & Liveness:** So sánh khuôn mặt thực tế với ảnh trên giấy tờ và kiểm tra sự sống (nháy mắt).
*   **Lớp 3 - NFC Reading:** Đọc dữ liệu ký số từ chip CCCD (dành cho thiết bị hỗ trợ NFC).

### 1.2 Quản lý Hồ sơ Người dùng
Màn hình `ProfileCompletionScreen` đóng vai trò là trung tâm dữ liệu, cho phép xem và cập nhật thông tin theo từng phần:
*   **Tính nhất quán (MasterData):** Toàn bộ các trường dữ liệu như Nghề nghiệp, Chức vụ, Trình độ học vấn, Tình trạng hôn nhân và Mối quan hệ phải được chọn từ danh sách (Bottom Sheet Selector) dựa trên MasterData chuẩn từ Backend.
*   **Tiện ích Contact Picker:** Hỗ trợ nút mở danh bạ hệ thống để chọn nhanh thông tin người liên hệ (Tên, SĐT).
*   **Trạng thái Hồ sơ:** Bao gồm `INCOMPLETE` (Vàng), `PENDING` (Xanh dương), `VERIFIED` (Xanh lá), `EXPIRED/REJECTED` (Đỏ).

---

## 2. Trải nghiệm Khoản vay (Loan Experience)

### 2.1 Luồng Khám phá & Đăng ký (Details-First Approach)
Để đảm bảo tính minh bạch, ứng dụng áp dụng luồng điều hướng ưu tiên thông tin:
1.  **Từ Home/Banner/Danh sách:** Người dùng click vào một gói vay sẽ luôn được dẫn đến màn hình **Chi tiết khoản vay (`LoanDetailScreen`)**.
2.  **Tại màn hình Chi tiết:** Người dùng xem đầy đủ thông tin về lãi suất, kỳ hạn, điều kiện và bảng tính minh họa.
3.  **Bấm "Đăng ký ngay":** Lúc này hệ thống mới thực hiện **Kiểm tra Điều kiện (Eligibility Check)**:
    *   **Đủ điều kiện:** Chuyển vào luồng đăng ký hồ sơ vay (`LoanFlow`).
    *   **Thiếu hồ sơ:** Hiện Dialog liệt kê các thông tin còn thiếu (VD: eKYC, nghề nghiệp) kèm nút "Hoàn thiện hồ sơ".
    *   **Từ chối:** Hiện Dialog thông báo lý do (VD: Điểm tín dụng thấp, nợ xấu).

### 2.2 Bộ lọc Khoản vay Nâng cao
Màn hình `LoanListScreen` cung cấp công cụ lọc mạnh mẽ:
*   **Slider Hạn mức:** Chọn khoảng số tiền mong muốn vay.
*   **Switch "Đủ điều kiện":** Khi bật, danh sách chỉ hiển thị các gói mà người dùng chắc chắn có thể vay (dựa trên trạng thái hồ sơ hiện tại).
*   **Visual Cues:** Sử dụng Badge (Đủ điều kiện/Chưa phù hợp) và Border màu sắc để phân biệt các gói vay trên danh sách.

---

## 3. Quản lý Tài chính & Ví (Money Management)
*   **Ví nội bộ:** Quản lý số dư khả dụng, nạp tiền từ thẻ liên kết và rút tiền về ngân hàng.
*   **Xác thực giao dịch:** Các giao dịch rút tiền hoặc ký hợp đồng nhạy cảm yêu cầu xác thực Sinh trắc học (FaceID/Vân tay) của thiết bị.

---

## 4. Giao diện & Điều hướng (UI/UX Standards)
*   **Thống nhất Navigation:** Sử dụng một thanh Top Bar duy nhất được quản lý tại `AppRoot`. Các màn hình con không tự khai báo thanh tiêu đề để đảm bảo tính nhất quán.
*   **Chế độ tối (Dark Mode):** Hỗ trợ toàn diện giao diện Dark Mode trên toàn bộ các màn hình.
