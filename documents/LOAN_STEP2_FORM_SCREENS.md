# Step 2 - Danh sach man hinh dien thong tin (sau eKYC)

Lien ket quay lai plan tong: [LOAN_STEP2_PLAN.md](./LOAN_STEP2_PLAN.md)

## Muc tieu
Liet ke cac man hinh form va component dung chung cho viec nhap thong tin sau eKYC.

## Cac file screen de tao
- `app/src/main/java/com/example/easymoney/ui/loan/information/form/LoanInfoFormEntryScreen.kt`
  - Entry gate sau eKYC thanh cong, dieu phoi cac section form.
- `app/src/main/java/com/example/easymoney/ui/loan/information/form/PersonalInfoFormScreen.kt`
  - Nhom thong tin ca nhan co ban.
- `app/src/main/java/com/example/easymoney/ui/loan/information/form/AddressInfoFormScreen.kt`
  - Nhom dia chi thuong tru/hien tai.
- `app/src/main/java/com/example/easymoney/ui/loan/information/form/EmploymentInfoFormScreen.kt`
  - Nhom nghe nghiep/thu nhap.
- `app/src/main/java/com/example/easymoney/ui/loan/information/form/ReferenceContactFormScreen.kt`
  - Nhom thong tin nguoi lien he tham chieu.

## Components dung chung cho form
- `app/src/main/java/com/example/easymoney/ui/loan/information/components/LoanInfoStepScaffold.kt`
- `app/src/main/java/com/example/easymoney/ui/loan/information/components/LoanInfoSectionCard.kt`
- `app/src/main/java/com/example/easymoney/ui/loan/information/components/LoanInfoTextField.kt`
- `app/src/main/java/com/example/easymoney/ui/loan/information/components/LoanInfoDropdownField.kt`
- `app/src/main/java/com/example/easymoney/ui/loan/information/components/LoanInfoBottomActionBar.kt`

## Mau bottom sheet dropdown dung chung (bat buoc)
- File: `app/src/main/java/com/example/easymoney/ui/loan/information/components/LoanOptionBottomSheetDropdown.kt`
- Muc dich: 1 component popup sheet duoi man hinh, nhan danh sach lua chon va tra item duoc chon.
- Contract de xuat:
  - `data class OptionItem<T>(val id: String, val label: String, val value: T)`
  - `@Composable fun <T> LoanOptionBottomSheetDropdown(..., options: List<OptionItem<T>>, onOptionSelected: (OptionItem<T>) -> Unit)`

## Done criteria
- Co du screen cho cac nhom form sau eKYC.
- Dropdown field co the dung chung 1 mau bottom sheet.
- Cac field dropdown khong tu viet popup rieng tung man hinh.

## Out-of-scope
- Chua chot danh sach option nghiep vu cu the cho tung field.
- Chua implement logic validate chi tiet.
