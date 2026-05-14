# Tài liệu Đặc tả API (API Specification) - EasyMoney
**Phiên bản:** 3.0 (Cập nhật 09/05/2026)

Tài liệu quy định các contract trao đổi dữ liệu JSON giữa ứng dụng Mobile và Backend.

---

## 1. Nhóm Khoản vay (Loans)

### 1.1 Danh sách khoản vay (Có lọc)
*   **Path:** `GET /loans`
*   **Query Params:**
    *   `keyword`: (String, optional) Từ khóa tên gói/nhà cung cấp.
    *   `minAmount`: (Long, optional) Hạn mức tối thiểu người dùng tìm kiếm.
    *   `maxAmount`: (Long, optional) Hạn mức tối đa người dùng tìm kiếm.
    *   `tenor`: (Int, optional) Kỳ hạn mong muốn theo tháng.
    *   `minInterest`: (Double, optional) Lãi suất tối thiểu.
    *   `maxInterest`: (Double, optional) Lãi suất tối đa.
    *   `eligibleOnly`: (Boolean) Chỉ lấy gói đủ điều kiện.
    *   `hotOnly`: (Boolean, optional) Chỉ lấy gói hot.
    *   `newOnly`: (Boolean, optional) Chỉ lấy gói mới.
    *   `promotionalOnly`: (Boolean, optional) Chỉ lấy gói đang ưu đãi.
    *   `lang`: (`vi` | `en`, optional) Ngôn ngữ label/badge backend trả về.
*   **Response:**
    ```json
    {
      "data": [
        {
          "id": "lp1",
          "packageName": "Vay Nhanh 24/7",
          "maxAmount": 50000000,
          "minAmount": 1000000,
          "minTenor": 3,
          "maxTenor": 24,
          "interest": 12.5,
          "isHot": true,
          "isNew": false,
          "isPromotional": true,
          "badges": ["HOT", "PROMOTION"],
          "isEligible": false,
          "ineligibilityReason": "MISSING_PROFILE"
        }
      ]
    }
    ```

### 1.2 Kiểm tra điều kiện (Eligibility)
*   **Path:** `POST /loans/{id}/check-eligibility`
*   **Response:**
    ```json
    {
      "isEligible": false,
      "reasonCode": "MISSING_PROFILE",
      "message": "Bạn cần hoàn thiện thông tin nghề nghiệp và eKYC.",
      "action": "NAVIGATE_PROFILE"
    }
    ```
    *   `action` values: `NONE`, `NAVIGATE_PROFILE`, `SHOW_REJECT`.

---

## 2. Nhóm Hồ sơ (User Profile)

### 2.1 Chi tiết Hồ sơ
*   **Path:** `GET /user/profile`
*   **Response:**
    ```json
    {
      "personalInfo": { "fullName": "...", "nationalId": "..." },
      "jobInfo": { "jobTitle": "...", "monthlyIncome": 20000000, "position": "..." },
      "contactInfo": { "contactName": "...", "relationship": "...", "phoneNumber": "..." },
      "education": "Đại học",
      "maritalStatus": "Độc thân",
      "identityStatus": {
        "isFaceVerified": true,
        "isNfcVerified": false,
        "isDocumentUploaded": true,
        "isIdentityDocumentVerified": true,
        "isBiometric2faEnabled": false
      }
    }
    ```
    *   `isIdentityDocumentVerified = isNfcVerified OR isDocumentUploaded` sau khi backend xác minh giấy tờ thành công.
    *   `isBiometric2faEnabled` là xác thực 2 bước optional của thiết bị, không phải điều kiện bắt buộc để hoàn thiện hồ sơ.

### 2.2 Cập nhật Hồ sơ (Partial)
*   **Path:** `PATCH /user/profile`
*   **Body:** Bất kỳ phần nào trong schema Chi tiết hồ sơ.

---

## 3. Nhóm Master Data
Tất cả endpoint Master Data phải nhận query `lang=vi|en` để trả label đúng ngôn ngữ hiện tại của app. Mobile submit id/code ổn định, không submit label hiển thị.

*   `GET /master/metadata?lang=vi`: Toàn bộ master data cho form hồ sơ/LoanFlow.
*   `GET /master/professions?lang=vi`: Danh sách nghề nghiệp.
*   `GET /master/positions?lang=vi`: Danh sách chức vụ.
*   `GET /master/relationships?lang=vi`: Danh sách mối quan hệ.
*   `GET /master/education-levels?lang=vi`: Danh sách trình độ học vấn.
*   `GET /master/marital-statuses?lang=vi`: Danh sách tình trạng hôn nhân.
*   `GET /master/districts/{provinceId}?lang=vi`: Danh sách quận/huyện.
*   `GET /master/wards/{districtId}?lang=vi`: Danh sách phường/xã.

---

## 4. Nhóm Chatbot

### 4.1 Gửi tin nhắn chatbot
*   **Path:** `POST /chatbot/messages`
*   **Body:**
    ```json
    {
      "conversationId": "optional-existing-id",
      "message": "Tôi muốn vay tiền",
      "lang": "vi"
    }
    ```
*   **Response:**
    ```json
    {
      "conversationId": "conv_123",
      "messages": [
        {
          "id": "msg_1",
          "type": "card",
          "text": "Bạn có thể xem các gói vay phù hợp.",
          "title": "Gói vay đề xuất",
          "actions": [
            { "label": "Xem gói vay", "type": "NAVIGATE_ROUTE", "target": "loan_list" }
          ]
        }
      ]
    }
    ```
    *   `type`: `text`, `action`, `card`.
    *   `actions.type`: `NAVIGATE_ROUTE`, `DIAL_PHONE`, `OPEN_URL`.

---

## 5. Nhóm Payment / Wallet

### 5.1 Thông tin ví
*   **Path:** `GET /wallet`
*   **Response:** Số dư, trạng thái tự động thanh toán, giao dịch gần đây.

### 5.2 Danh sách thẻ
*   **Path:** `GET /payment/cards`

### 5.3 Xác minh và thêm thẻ
*   **Path:** `POST /payment/cards/verify`
*   **Body:** card number/token, expiry, holder name, bank info theo chuẩn bảo mật backend/PSP.
*   **Response:** card id, masked number, card type, bank name, verification status.

### 5.4 Xóa thẻ
*   **Path:** `DELETE /payment/cards/{cardId}`

### 5.5 Nạp/rút tiền
*   **Path:** `POST /wallet/top-up`
*   **Path:** `POST /wallet/withdraw`
*   **Body:** `amount`, `cardId`, optional `biometricAssertionId` nếu user bật 2FA.

### 5.6 Tự động thanh toán
*   **Path:** `PATCH /wallet/auto-deduction`
*   **Body:** `{ "enabled": true }`

### 5.7 Thanh toán QR
*   **Path:** `POST /payments/qr-intents`
*   **Body:** `{ "amount": 100000, "lang": "vi" }`
*   **Response:**
    ```json
    {
      "paymentIntentId": "pi_123",
      "qrPayload": "000201...",
      "qrImageUrl": "https://...",
      "amount": 100000,
      "status": "PENDING",
      "expiresAt": 1778750000000
    }
    ```
*   **Path:** `GET /payments/qr-intents/{paymentIntentId}`
*   **Response:** `status` là `PENDING`, `SUCCESS`, `FAILED`, `EXPIRED`, `CANCELLED`.

---

## 6. Nhóm Rewards / Events

### 6.1 Danh mục đổi thưởng
*   **Path:** `GET /rewards/catalog?lang=vi`

### 6.2 Đổi thưởng
*   **Path:** `POST /rewards/{itemId}/redeem`
*   **Response:** điểm còn lại, trạng thái, payload quà/voucher/code nếu có.

### 6.3 Chi tiết event
*   **Path:** `GET /events/{id}?lang=vi`
*   **Response:** title, content sections, imageUrl, expiryDate, interactionType, CTA.

### 6.4 Tham gia event
*   **Path:** `POST /events/{id}/join`
