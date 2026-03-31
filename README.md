# EasyMoney

Android app project for loan simulation and related flows.

---

## Mục lục

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Kiến trúc tổng thể](#3-kiến-trúc-tổng-thể)
4. [Cấu trúc thư mục](#4-cấu-trúc-thư-mục)
5. [Tầng dữ liệu (Data Layer)](#5-tầng-dữ-liệu-data-layer)
6. [Tầng điều hướng (Navigation Layer)](#6-tầng-điều-hướng-navigation-layer)
7. [Tầng giao diện (UI Layer)](#7-tầng-giao-diện-ui-layer)
8. [Luồng tính toán khoản vay](#8-luồng-tính-toán-khoản-vay)
9. [Tính năng hiện tại](#9-tính-năng-hiện-tại)
10. [Lộ trình phát triển](#10-lộ-trình-phát-triển)

---

## 1. Tổng quan dự án

**EasyMoney** là ứng dụng Android mô phỏng quy trình đăng ký vay vốn cá nhân. Ứng dụng cho phép người dùng:

- Xem thông tin gói vay (hạn mức, lãi suất, kỳ hạn).
- Nhập số tiền vay và lựa chọn kỳ hạn trả.
- Bật/tắt bảo hiểm khoản vay.
- Xem ngay kết quả tính toán: tiền thực nhận, phí bảo hiểm, tiền lãi, số tiền trả hàng tháng, tổng tiền phải trả.
- Đọc hướng dẫn từng màn hình thông qua màn hình hướng dẫn (PageGuide).

---

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin |
| UI Framework | Jetpack Compose (Material 3) |
| Navigation | Navigation Compose |
| State Management | ViewModel + StateFlow |
| Local Storage | Jetpack DataStore (Preferences) |
| Build System | Gradle (Kotlin DSL) |
| Min SDK | Android 24 (Nougat, API 24) |

---

## 3. Kiến trúc tổng thể

Ứng dụng tuân theo mô hình **MVVM (Model – View – ViewModel)** kết hợp với **Unidirectional Data Flow (UDF)**:

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                         │
│   Composable Screens  ←  ViewModel (StateFlow/UiState)  │
│         ↕ events (user actions)                         │
│   AppRoot → AppNavHost → [LoanScreen | PageGuideScreen] │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                    Navigation Layer                     │
│      AppNavHost, AppDestination, AppState               │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                     Data Layer                          │
│   LoanPackage (model)  |  UserPreferencesRepository     │
└─────────────────────────────────────────────────────────┘
```

### Nguyên tắc chính

- **Single source of truth**: Toàn bộ trạng thái UI được quản lý trong `LoanUiState`, phát ra qua `StateFlow`.
- **Immutable state**: Mỗi thay đổi tạo ra một bản sao mới của `LoanUiState` thay vì mutation.
- **Separation of concerns**: Logic tính toán nằm hoàn toàn trong `LoanViewModel`, UI chỉ hiển thị dữ liệu và gửi sự kiện.

---

## 4. Cấu trúc thư mục

```
app/src/main/java/com/example/easymoney/
│
├── data/
│   ├── model/
│   │   └── LoanPackage.kt          # Model gói vay, hỗ trợ Parcelable
│   └── UserPreferencesRepository.kt # Lưu trữ thông tin user (DataStore)
│
├── navigation/
│   ├── AppDestination.kt           # Định nghĩa các route và metadata top-bar
│   ├── AppNavHost.kt               # NavHost – khai báo các composable route
│   └── AppState.kt                 # rememberAppState(), quản lý NavController
│
├── ui/
│   ├── components/
│   │   └── AppNavigationBar.kt     # Top app bar dùng chung toàn app
│   ├── guide/
│   │   └── PageGuideScreen.kt      # Màn hình hướng dẫn – render XML layout
│   ├── loan/
│   │   ├── LoanScreen.kt           # UI màn hình thông tin khoản vay
│   │   ├── LoanUiState.kt          # Data class trạng thái UI
│   │   └── LoanViewModel.kt        # ViewModel: logic tính toán + state
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── AppRoot.kt                  # Scaffold gốc: Scaffold + AppNavHost
│
└── MainActivity.kt                 # Entry point
```

---

## 5. Tầng dữ liệu (Data Layer)

### 5.1 `LoanPackage` (data/model/LoanPackage.kt)

Model đại diện cho một gói vay. Implement `Parcelable` để truyền qua Intent/Bundle.

| Trường | Kiểu | Mô tả |
|---|---|---|
| `id` | String | Định danh duy nhất gói vay |
| `packageName` | String | Tên gói vay (ví dụ: "Vay Nhanh") |
| `tenorRange` | String | Danh sách kỳ hạn, format `"6,12,18,24"` |
| `minAmount` | Long | Số tiền vay tối thiểu (VND) |
| `maxAmount` | Long | Số tiền vay tối đa (VND) |
| `interest` | Double | Lãi suất năm (%) |
| `overdueCost` | Double | Phí phạt quá hạn (%) |
| `eligibleCreditScore` | Int | Điểm tín dụng tối thiểu yêu cầu |

**Phương thức hỗ trợ:**
- `getTenorList(): List<Int>` – Phân tích `tenorRange` thành danh sách số nguyên; fallback về `[6, 12, 18, 24]` nếu phân tích thất bại.

### 5.2 `UserPreferencesRepository` (data/UserPreferencesRepository.kt)

Lưu trữ thông tin phiên người dùng qua **Jetpack DataStore**.

| Key | Mô tả |
|---|---|
| `customer_id` | ID khách hàng |
| `customer_name` | Tên khách hàng |
| `auth_token` | Token xác thực |

**API:**
- `customerId: Flow<String?>` – Lắng nghe ID khách hàng bất đồng bộ.
- `customerName: Flow<String?>` – Lắng nghe tên khách hàng bất đồng bộ.
- `saveUserInfo(id, name, token)` – Lưu thông tin người dùng (suspend).
- `clear()` – Xóa toàn bộ preferences (suspend).

---

## 6. Tầng điều hướng (Navigation Layer)

### 6.1 `AppDestination`

Sealed class định nghĩa tất cả màn hình trong app cùng metadata top-bar:

| Destination | Route | Tiêu đề | Nút back | Nút help |
|---|---|---|---|---|
| `LoanInformation` | `loan_information` | Thông tin khoản vay | Không | Có |
| `PageGuide` | `page_guide?xml={xml}` | Hướng dẫn | Có | Không |

`PageGuide` cung cấp `createRoute(xmlName)` để tạo route động theo tên file XML hướng dẫn.

### 6.2 `AppNavHost`

Khai báo các composable route và inject dependency cần thiết vào từng màn hình. Hiện tại dùng mock data cho `LoanInformation` trong quá trình phát triển.

### 6.3 `AppState`

Wrapper quản lý `NavController`, cung cấp:
- `currentDestination()` – Trả về `AppDestination` hiện tại.
- `navigateTo(route)` – Điều hướng tới route chỉ định.
- `popBackStack()` – Quay lại màn hình trước.

---

## 7. Tầng giao diện (UI Layer)

### 7.1 `AppRoot`

Màn hình root bao gồm `Scaffold` với `AppNavigationBar` ở top và `AppNavHost` ở body. Tự động cập nhật tiêu đề, nút back, nút help theo màn hình hiện tại.

### 7.2 `AppNavigationBar`

Top app bar dùng chung với khả năng:
- Tự động tính màu nội dung (text/icon) tương phản với màu nền (dark/light).
- Hỗ trợ nút back (tùy chọn) và nút help/info (tùy chọn).
- Nhận `scrollBehavior` để hỗ trợ scroll-away behavior.

### 7.3 `LoanScreen` + `LoanViewModel`

Màn hình chính mô phỏng quy trình vay:

- **Bước 1**: Chọn số tiền vay (slider/input trong khoảng `minAmount`–`maxAmount`).
- **Bước 2**: Chọn kỳ hạn từ danh sách `tenorList`.
- **Bước 3**: Bật/tắt bảo hiểm khoản vay.
- **Kết quả**: Hiển thị bảng tính toán (xem mục 8).

`LoanViewModel` quản lý `LoanUiState` và tái tính toán tự động mỗi khi người dùng thay đổi tham số.

### 7.4 `PageGuideScreen`

Màn hình hướng dẫn hiển thị nội dung từ file XML layout:
- Nhận tham số `xmlName` từ navigation argument.
- Tự động fallback về `guide_default_updating` nếu không tìm thấy file XML tương ứng.
- Áp dụng màu chữ từ Compose theme lên toàn bộ `TextView` trong XML (hỗ trợ dark mode).
- Xóa màu nền của XML để hòa với theme `Card` Compose.

---

## 8. Luồng tính toán khoản vay

Khi người dùng thay đổi bất kỳ tham số nào (số tiền, kỳ hạn, bảo hiểm), `LoanViewModel.calculateLoan()` được gọi tự động:

```
insuranceFee     = amount × 1%           (nếu bảo hiểm được bật, ngược lại = 0)
interestAmount   = amount × (interest/100) × (tenor/12)
totalPayment     = amount + insuranceFee + interestAmount
monthlyPayment   = totalPayment / tenor
actualReceived   = amount - insuranceFee  (≥ 0)
```

Kết quả được cập nhật vào `LoanUiState` và hiển thị ngay lập tức trên UI.

---

## 9. Tính năng hiện tại

- [x] Hiển thị thông tin gói vay (tên, hạn mức, lãi suất, kỳ hạn)
- [x] Nhập số tiền vay trong giới hạn cho phép
- [x] Chọn kỳ hạn trả nợ
- [x] Bật/tắt bảo hiểm khoản vay
- [x] Tính toán và hiển thị ngay: tiền thực nhận, phí bảo hiểm, tiền lãi, trả hàng tháng, tổng tiền trả
- [x] Navigation đa màn hình (LoanInformation ↔ PageGuide)
- [x] Top app bar động theo từng màn hình
- [x] Hướng dẫn theo từng trang (PageGuide) với fallback an toàn
- [x] Hỗ trợ dark mode / light mode cho cả Compose lẫn XML legacy
- [x] Lưu trữ thông tin người dùng (DataStore)

---

## 10. Lộ trình phát triển

### Giai đoạn 1 – Nền tảng (Hiện tại)
- [x] Kiến trúc MVVM + Navigation Compose
- [x] Màn hình tính toán khoản vay
- [x] Hệ thống hướng dẫn trang
- [x] Hỗ trợ dark/light mode

### Giai đoạn 2 – Luồng đăng ký vay
- [ ] Màn hình danh sách gói vay (kết nối API thực)
- [ ] Màn hình nhập thông tin cá nhân (KYC)
- [ ] Màn hình xác nhận và nộp đơn vay
- [ ] Tích hợp API backend để gửi hồ sơ vay

### Giai đoạn 3 – Quản lý khoản vay
- [ ] Màn hình tra cứu trạng thái hồ sơ
- [ ] Màn hình lịch sử giao dịch / thanh toán
- [ ] Thông báo push (nhắc nhở thanh toán)

### Giai đoạn 4 – Tính năng nâng cao
- [ ] Xác thực sinh trắc học (fingerprint / face ID)
- [ ] Biểu đồ trực quan hóa lịch trả nợ
- [ ] So sánh đa gói vay
- [ ] Hỗ trợ đa ngôn ngữ (i18n)
