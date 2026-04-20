# Tài liệu Đặc tả API Mock Server (EasyMoney)

## 1. Mục đích
Tài liệu này được xây dựng nhằm mô tả chi tiết các endpoint, cấu trúc dữ liệu yêu cầu (Request) và phản hồi (Response) cần thiết để xây dựng một Mock Server hoàn chỉnh. Mục tiêu cuối cùng là thay thế các dữ liệu giả (hardcoded) trong Repository hiện tại bằng các lời gọi API thực tế, giúp chuyển đổi ứng dụng từ giai đoạn prototype sang bản Product một cách mượt mà.

## 2. Hướng dẫn thực hiện
- **Nguyên tắc bổ sung:** Đây là một nhiệm vụ dài hạn. Không thực hiện liệt kê tất cả endpoint cùng một lúc.
- **Quy trình:**
    1. Phân tích các màn hình UI hiện tại và logic trong các lớp `RepositoryImpl`.
    2. Xác định các điểm dữ liệu đang được "mock" cứng trong code.
    3. Thiết kế cấu trúc JSON tương ứng cho endpoint đó vào tài liệu này.
    4. Cập nhật Mock Server dựa trên đặc tả này.
- **Tiêu chuẩn dữ liệu:** Các cấu trúc trả về phải tuân thủ đúng các Model đã định nghĩa trong package `com.example.easymoney.domain.model`.

## 3. Danh sách các Endpoint

### 3.1. Authentication (Xác thực)

#### **[POST] /api/v1/auth/login**
- **Mô tả:** Đăng nhập vào hệ thống bằng số điện thoại và mật khẩu.
- **Request:**
  ```json
  {
    "phone": "0987654321",
    "password": "hashed_password"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "accessToken": "eyJhbGci...",
      "refreshToken": "eyJhbGci...",
      "expiresIn": 3600
    }
  }
  ```

#### **[POST] /api/v1/auth/register**
- **Mô tả:** Đăng ký tài khoản mới.
- **Request:**
  ```json
  {
    "phone": "0987654321",
    "fullName": "NGUYEN LE MINH",
    "password": "hashed_password"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "accessToken": "eyJhbGci...",
      "refreshToken": "eyJhbGci...",
      "expiresIn": 3600
    }
  }
  ```

---

### 3.2. User Profile & Account (Thông tin người dùng)

#### **[GET] /api/v1/user/profile**
- **Mô tả:** Lấy thông tin cá nhân cơ bản của người dùng (dùng cho HOME và hồ sơ).
- **Phản ánh Model:** `MyInfoModel`
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "fullName": "NGUYEN LE MINH",
      "gender": "Nam",
      "dateOfBirth": "1995-10-20",
      "phoneNumber": "0987654321",
      "nationalId": "012345678901",
      "issueDate": "2020-01-01",
      "avatarUrl": "https://example.com/avatars/minh.png",
      "permanentProvince": "Thành phố Hà Nội",
      "permanentDistrict": "Quận Cầu Giấy",
      "permanentWard": "Phường Dịch Vọng",
      "permanentDetail": "Số 123, Đường Cầu Giấy"
    }
  }
  ```

#### **[GET] /api/v1/user/account**
- **Mô tả:** Lấy thông tin tài khoản ngân hàng và số dư khả dụng.
- **Phản ánh Model:** `AccountEntity`
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "userId": "user_id_123",
      "accountNumber": "19034567890123",
      "bankName": "Techcombank",
      "ownerName": "NGUYEN LE MINH",
      "balance": 5000000.0,
      "currency": "VND",
      "isActive": true
    }
  }
  ```

### 3.3. Loan Configuration (Cấu hình khoản vay)

#### **[GET] /api/v1/loan/package/my**
- **Mô tả:** Lấy thông tin gói vay được định cấu hình riêng cho người dùng hiện tại.
- **Phản ánh Model:** `LoanPackageModel`
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "id": "package_gold_001",
      "packageName": "Gói vay Tiêu dùng Vàng",
      "tenorRange": "6, 12, 18, 24, 36",
      "minAmount": 5000000,
      "maxAmount": 50000000,
      "interest": 1.5,
      "overdueCost": 0.05,
      "eligibleCreditScore": 650
    }
  }
  ```

---

### 3.4. Master Data (Dữ liệu danh mục cho Form)

#### **[GET] /api/v1/master/metadata**
- **Mô tả:** Lấy toàn bộ dữ liệu danh mục cần thiết cho các form (ngoại trừ Quận/Huyện/Xã do số lượng lớn).
- **Chiến lược:** Client nên thực hiện cache dữ liệu này. Backend trả về `version` và `expiredAt` để client quyết định khi nào cần reload.
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "version": "2026.04.15.01",
      "expiredAt": "2026-05-15T00:00:00Z",
      "masterData": {
        "provinces": [
          { "id": "01", "name": "Thành phố Hà Nội" },
          { "id": "79", "name": "Thành phố Hồ Chí Minh" }
        ],
        "professions": [
          { "id": "p1", "name": "Nhân viên văn phòng công ty" },
          { "id": "p2", "name": "Công chức nhà nước" }
        ],
        "positions": [
          { "id": "pos1", "name": "Nhân viên/Chuyên viên" },
          { "id": "pos2", "name": "Tổ phó" }
        ],
        "educationLevels": [
          { "id": "e1", "name": "Không có chuyên môn" },
          { "id": "e2", "name": "Bằng trung cấp" }
        ],
        "maritalStatuses": [
          { "id": "m1", "name": "Độc thân" },
          { "id": "m2", "name": "Đã kết hôn" }
        ],
        "relationships": [
          { "id": "r1", "name": "Ông/Bà" },
          { "id": "r2", "name": "Bố/Mẹ" }
        ]
      }
    }
  }
  ```

#### **[GET] /api/v1/master/districts/{provinceId}**
- **Mô tả:** Lấy danh sách Quận/Huyện theo Tỉnh/Thành phố (Lazy load).
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      { "id": "001", "name": "Quận Ba Đình", "parentId": "01" }
    ]
  }
  ```

#### **[GET] /api/v1/master/wards/{districtId}**
- **Mô tả:** Lấy danh sách Phường/Xã theo Quận/Huyện (Lazy load).
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      { "id": "0001", "name": "Phường Phúc Xá", "parentId": "001" }
    ]
  }
  ```

---

### 3.5. Loan Application (Hồ sơ vay)

#### **[POST] /api/v1/loan/submit**
- **Mô tả:** Gửi hồ sơ đăng ký vay vốn sau khi người dùng điền đầy đủ form.
- **Request:** Tuân thủ `LoanApplicationRequest` model.
  ```json
  {
    "loanAmount": 20000000,
    "tenorMonth": 12,
    "hasInsurance": true,
    "permanentProvince": "01",
    "permanentDistrict": "001",
    "permanentWard": "0001",
    "permanentDetail": "Số 10 Lý Nam Đế",
    "currentProvince": "01",
    "currentDistrict": "001",
    "currentWard": "0001",
    "currentDetail": "Số 10 Lý Nam Đế",
    "monthlyIncome": 15000000,
    "profession": "off_01",
    "position": "staff",
    "education": "university",
    "maritalStatus": "single",
    "contactName": "Nguyễn Văn A",
    "contactRelationship": "Brother",
    "contactPhone": "0912345678"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "applicationId": "APP_2026_001",
      "status": "PENDING_EKYC",
      "message": "Hồ sơ đã được tiếp nhận, vui lòng thực hiện định danh."
    }
  }
  ```

---

### 3.6. eKYC (Định danh điện tử)

#### **[POST] /api/v1/ekyc/capture/face**
- **Mô tả:** Tải lên ảnh khuôn mặt (selfie) để thực hiện định danh sinh trắc học.
- **Content-Type:** `multipart/form-data`
- **Request Parameters:**
    - `image`: File (Binary) - Ảnh chụp khuôn mặt.
    - `sessionId`: String - Mã phiên định danh.
    - `deviceModel`: String - Tên thiết bị (ví dụ: Samsung SM-G991B).
    - `qualityScore`: Float - Điểm chất lượng ảnh từ Mobile SDK (0.0 - 1.0).
    - `precheckPassed`: Boolean - Kết quả kiểm tra nhanh tại thiết bị.
- **Response (200 OK - Thành công):**
  ```json
  {
    "status": "success",
    "data": {
      "captureId": "CAP_789_XYZ",
      "status": "accepted",
      "message": "Ảnh khuôn mặt hợp lệ.",
      "nextStep": "liveness_check"
    }
  }
  ```
- **Response (200 OK - Thất bại do chất lượng ảnh):**
  ```json
  {
    "status": "success",
    "data": {
      "captureId": "CAP_789_ERR",
      "status": "rejected",
      "reason": "IMAGE_BLURRY",
      "message": "Ảnh quá mờ, vui lòng chụp lại ở nơi đủ ánh sáng.",
      "nextStep": "retry_selfie"
    }
  }
  ```
- **Response (200 OK - Thất bại do không phát hiện khuôn mặt):**
  ```json
  {
    "status": "success",
    "data": {
      "captureId": "CAP_789_NF",
      "status": "rejected",
      "reason": "NO_FACE_DETECTED",
      "message": "Không tìm thấy khuôn mặt trong khung hình.",
      "nextStep": "retry_selfie"
    }
  }
  ```

#### **[POST] /api/v1/ekyc/capture/face-base64**
- **Mô tả:** (Fallback) Tải lên ảnh khuôn mặt dưới dạng chuỗi Base64.
- **Content-Type:** `application/json`
- **Request:**
  ```json
  {
    "image": "data:image/jpeg;base64,...",
    "sessionId": "session_123",
    "deviceModel": "Pixel 6",
    "precheckPassed": true
  }
  ```
- **Response:** Tương tự như endpoint Multipart.

---

### 3.7. OTP (Mã xác thực)

#### **[POST] /api/v1/otp/send**
- **Mô tả:** Gửi mã OTP tới số điện thoại của người dùng.
- **Request:**
  ```json
  {
    "purpose": "SIGN_CONTRACT"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Mã OTP đã được gửi tới số điện thoại của bạn."
  }
  ```

#### **[POST] /api/v1/otp/verify**
- **Mô tả:** Xác thực mã OTP người dùng nhập vào.
- **Request:**
  ```json
  {
    "otp": "123456",
    "purpose": "SIGN_CONTRACT"
  }
  ```
- **Response (200 OK - Thành công):**
  ```json
  {
    "status": "success",
    "message": "Xác thực OTP thành công."
  }
  ```
- **Response (400 Bad Request - Thất bại):**
  ```json
  {
    "status": "error",
    "message": "Mã OTP không chính xác hoặc đã hết hạn."
  }
  ```

---

### 3.8. Notifications (Thông báo)

#### **[GET] /api/v1/notifications**
- **Mô tả:** Lấy danh sách thông báo của người dùng.
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      {
        "id": 1,
        "title": "Giải ngân thành công",
        "content": "Khoản vay 5.000.000đ đã được giải ngân vào tài khoản của bạn.",
        "type": "transaction",
        "amount": 5000000,
        "balanceAfter": 5200000,
        "transactionCode": "TX123456789",
        "timestamp": 1712130000000,
        "isRead": false
      },
      {
        "id": 2,
        "title": "Ưu đãi đặc biệt",
        "content": "Giảm ngay 0.5% lãi suất cho lần vay tiếp theo.",
        "type": "promotion",
        "timestamp": 1712120000000,
        "isRead": true
      }
    ]
  }
  ```

#### **[PATCH] /api/v1/notifications/{id}/read**
- **Mô tả:** Đánh dấu một thông báo là đã đọc.
- **Response (200 OK):**
  ```json
  {
    "status": "success"
  }
  ```

#### **[POST] /api/v1/notifications/read-all**
- **Mô tả:** Đánh dấu tất cả thông báo là đã đọc.
- **Response (200 OK):**
  ```json
  {
    "status": "success"
  }
  ```

#### **[DELETE] /api/v1/notifications/clear**
- **Mô tả:** Xóa tất cả thông báo của người dùng.
- **Response (200 OK):**
  ```json
  {
    "status": "success"
  }
  ```

---

### 3.9. Transaction History (Lịch sử giao dịch)

#### **[GET] /api/v1/user/transactions**
- **Mô tả:** Lấy danh sách lịch sử giao dịch.
- **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      {
        "date": "01/04/2026",
        "items": [
          {
            "description": "Nhận tiền từ ngân hàng",
            "transactionCode": "2604750502176432",
            "amount": 41652,
            "balance": 132376,
            "time": "07:45"
          },
          {
            "description": "GD thanh toán điện tử",
            "transactionCode": "2604750502914339",
            "amount": -14181,
            "balance": 90724,
            "time": "07:31"
          }
        ]
      }
    ]
  }
  ```

---
