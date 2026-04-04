# Step 2 - ViewModel va UiState plan

Lien ket:
- Plan tong: [LOAN_STEP2_PLAN.md](./LOAN_STEP2_PLAN.md)
- eKYC screens: [LOAN_STEP2_EKYC_SCREENS.md](./LOAN_STEP2_EKYC_SCREENS.md)
- Form screens: [LOAN_STEP2_FORM_SCREENS.md](./LOAN_STEP2_FORM_SCREENS.md)

## Muc tieu
Dinh nghia ten ViewModel/UiState va pham vi trach nhiem de de code, de test, de mo rong.

## De xuat ViewModel

### 1) `LoanInformationViewModel` (orchestration Step 2)
- File:
  - `app/src/main/java/com/example/easymoney/ui/loan/information/LoanInformationViewModel.kt`
  - `app/src/main/java/com/example/easymoney/ui/loan/information/LoanInformationUiState.kt`
- Nhiem vu:
  - Quan ly phan doan hien tai: `eKyc` hay `form`.
  - Dieu phoi chuyen trang ben trong Step 2.
  - Nhan ket qua tu eKYC va mo phan form.

### 2) `EkycViewModel` (nhom eKYC)
- File:
  - `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycViewModel.kt`
  - `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycUiState.kt`
- Nhiem vu:
  - Quan ly trang thai cac man hinh eKYC.
  - Quan ly trang thai permission/capture/result o muc UI.
  - Phat event ket thuc eKYC de `LoanInformationViewModel` nhan.

### 3) `LoanInfoFormViewModel` (nhom form sau eKYC)
- File:
  - `app/src/main/java/com/example/easymoney/ui/loan/information/form/LoanInfoFormViewModel.kt`
  - `app/src/main/java/com/example/easymoney/ui/loan/information/form/LoanInfoFormUiState.kt`
- Nhiem vu:
  - Quan ly state form theo section.
  - Quan ly dropdown state (sheet dang mo, field dang chon).
  - Tong hop du lieu nhap de chuyen sang Step 3.

## De xuat UiState tong quan

### `LoanInformationUiState`
- `phase: LoanInformationPhase` (`EKYC`, `FORM`)
- `currentScreen: LoanInformationScreenKey`
- `canGoBack: Boolean`
- `isLoading: Boolean`
- `message: String?`

### `EkycUiState`
- `currentEkycScreen: EkycScreenKey`
- `permissionState: EkycPermissionState`
- `documentCaptureState: CaptureState`
- `faceCaptureState: CaptureState`
- `resultState: EkycResultState`

### `LoanInfoFormUiState`
- `currentFormScreen: LoanFormScreenKey`
- `sections: LoanFormSectionsState`
- `isSubmitEnabled: Boolean`
- `activeDropdownField: LoanDropdownField?`
- `dropdownOptions: List<OptionItem<String>>` (co the generic sau)

## Done criteria
- Moi ViewModel co 1 pham vi trach nhiem ro.
- Khong duplicate state giua VM tong va VM con.
- Co du ui state de map tat ca man hinh trong 2 nhom eKYC + form.

## Out-of-scope
- Chua mo ta event/effect chi tiet.
- Chua map den repository use-case cu the.
