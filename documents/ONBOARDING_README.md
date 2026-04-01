## Onboarding Package Documentation

### Cấu trúc thư mục:
```
ui/onboarding/
├── OnboardingScreen.kt (Main UI screen)
└── OnboardingViewModel.kt (Business logic)

data/model/
└── OnboardingCard.kt (Data models)
```

### Mô tả các file:

#### 1. **OnboardingScreen.kt**
- `OnboardingScreen()` - Main composable function
  - Header với tiêu đề "Vay tú chứcs tái chính"
  - Logo section với background teal
  - "Lý do chọn EASY MONEY" section với 3 reason cards
  - "Thông tin sản phẩm" section
  - "Thông tin nhà cung cấp dịch vụ vay" section
  - "Trải nghiệm ngay" button

**Sub-components:**
- `OnboardingHeader()` - Tiêu đề và logo
- `WhyChooseSection()` - 3 lý do chọn
- `ReasonCard()` - Từng card lý do
- `ProductInfoSection()` - Thông tin sản phẩm
- `InfoRow()` - Row hiển thị thông tin
- `ProviderInfoSection()` - Thông tin nhà cung cấp
- `DetailBulletItem()` - Bullet point item

#### 2. **OnboardingCard.kt**
Data models:
- `OnboardingCard` - Card model với id, title, description, icon
- `ProductInfo` - Thông tin sản phẩm (minAmount, maxAmount, interestRate, monthlyRate)
- `ProviderInfo` - Thông tin nhà cung cấp (title, description, details list)

#### 3. **OnboardingViewModel.kt**
- Placeholder cho business logic trong tương lai

### Cách sử dụng:

```kotlin
// Trong AppNavHost.kt
composable(AppDestination.Onboarding.route) {
    OnboardingScreen(
        onNavigateToLoan = {
            // Navigate to loan information screen
        }
    )
}
```

### Styling:
- **Màu sắc chính:**
  - TealPrimary: #137A91
  - TealSecondary: #E8F4F6
  - TextPrimary: #1D2939
  - TextSecondary: #667085

- **Font sizes:**
  - Tiêu đề chính: 18sp SemiBold
  - Section titles: 16sp Bold
  - Normal text: 13-14sp
  - Small text: 12sp

### Image Resources:
- `R.drawable.img` - Logo EASY MONEY
- `R.drawable.img_1` - Hình ngân hàng
- `R.drawable.img_2` - Hình thủ tục
- `R.drawable.img_3` - Hình dịch vụ
- `R.drawable.img_4` - (Có sẵn, có thể dùng sau)

### Navigation:
- **Start destination:** Onboarding screen
- **Transition:** Onboarding → Loan Information (khi click "Trải nghiệm ngay")

