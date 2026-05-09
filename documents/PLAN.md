# Kế hoạch hoàn thiện ứng dụng EasyMoney
**Phiên bản:** 3.0 (Cập nhật 09/05/2026)

Tài liệu này tổng hợp các hạng mục đã hoàn thành và lộ trình tiếp theo của dự án.

---

## 1. Các hạng mục ĐÃ HOÀN THÀNH (Done)

### 1.1 Hệ thống Identity dùng chung
*   Tách module `FaceCapture`, `NfcReader`, `Biometric`, `DocumentUpload` thành các thành phần độc lập.
*   Tích hợp thành công vào Dashboard Hồ sơ và Luồng Vay.

### 1.2 Trải nghiệm Khoản vay & Hồ sơ
*   **Loan Flow (Details-First):** Click Home/Banner -> Detail -> Eligibility Check -> Loan Flow.
*   **Bộ lọc Khoản vay:** Slider hạn mức và Switch "Đủ điều kiện" đã hoạt động.
*   **Hồ sơ thông minh:** Chỉnh sửa nghề nghiệp qua Dropdown (MasterData) và trích xuất danh bạ cho Người liên hệ.

### 1.3 Kiến trúc & UI/UX
*   Thống nhất Top Bar tập trung tại `AppRoot`.
*   Build thành công toàn bộ dự án với Gradle.

---

## 2. Các hạng mục TIẾP THEO (Next Steps)

### 2.1 Hoàn thiện Tính năng
*   [TODO] **Chat Bot Tư vấn:** Tích hợp logic xử lý câu hỏi thường gặp và điều hướng gói vay qua chat.
*   [TODO] **Ví & Thẻ thực tế:** Kết nối API thực cho nạp/rút và quản lý liên kết thẻ.
*   [TODO] **Hệ thống Điểm thưởng:** Xây dựng logic trừ điểm khi đổi quà và cập nhật lịch sử.

### 2.2 Kỹ thuật & Hiệu năng
*   [TODO] **Cache MasterData:** Lưu trữ danh sách nghề nghiệp/vùng miền xuống Local DB (Room) để dùng offline.
*   [TODO] **Lighthouse Audit:** Tối ưu hóa thời gian render và kích thước bundle của ứng dụng.
*   [TODO] **Unit Tests:** Bổ sung test case cho logic Eligibility và Filtering trong ViewModels.

---

## 3. Lộ trình Triển khai (Timeline)
*   **Tuần 1:** Hoàn thiện Chat Bot và logic Loyalty.
*   **Tuần 2:** Tối ưu hóa dữ liệu Local và Security Audit.
*   **Tuần 3:** Kết nối API Production và UAT.
