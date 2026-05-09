# Tài liệu Đặc tả API (API Specification) - EasyMoney
**Phiên bản:** 3.0 (Cập nhật 09/05/2026)

Tài liệu quy định các contract trao đổi dữ liệu JSON giữa ứng dụng Mobile và Backend.

---

## 1. Nhóm Khoản vay (Loans)

### 1.1 Danh sách khoản vay (Có lọc)
*   **Path:** `GET /loans`
*   **Query Params:**
    *   `maxAmount`: (Long) Hạn mức tối đa người dùng tìm kiếm.
    *   `eligibleOnly`: (Boolean) Chỉ lấy gói đủ điều kiện.
*   **Response:**
    ```json
    {
      "data": [
        {
          "id": "lp1",
          "packageName": "Vay Nhanh 24/7",
          "maxAmount": 50000000,
          "interest": 12.5,
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
      "identityStatus": { "isFaceVerified": true, "isNfcVerified": false }
    }
    ```

### 2.2 Cập nhật Hồ sơ (Partial)
*   **Path:** `PATCH /user/profile`
*   **Body:** Bất kỳ phần nào trong schema Chi tiết hồ sơ.

---

## 3. Nhóm Master Data
*   `GET /master/professions`: Danh sách nghề nghiệp.
*   `GET /master/positions`: Danh sách chức vụ.
*   `GET /master/relationships`: Danh sách mối quan hệ.
*   `GET /master/education-levels`: Danh sách trình độ học vấn.
*   `GET /master/marital-statuses`: Danh sách tình trạng hôn nhân.
