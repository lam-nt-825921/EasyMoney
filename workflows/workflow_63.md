# Workflow #63 — Final A: Hard-coded user-facing text → VI/EN resources

## Goal
Mọi user-facing production string đều đến từ `values/strings.xml` và `values-en/strings.xml` với cùng key set; không hard-code Vietnamese/English trong composables/ViewModels/repositories.

## Requirements

### Functional
- Audit hot-spots (theo AGENT_TASKS.md Final A):
  - Profile/contact edit: `EditContactInfoScreen.kt`, `EditJobInfoScreen.kt`, `EditPersonalInfoScreen.kt`, `ProfileScreen.kt`, `ProfileCompletionViewModel.kt`.
  - Confirm: `ConfirmInfoScreen.kt`.
  - Rewards: `RewardScreen.kt`, `RewardViewModel.kt`, `RewardRepositoryImpl.kt`.
  - Payment: `MoneyManagementScreen.kt`, `TopUpScreen.kt`/`ViewModel`, `WithdrawScreen.kt`/`ViewModel`, `PaymentCardsScreen.kt`.
  - Loan config/info/confirm: `LoanConfigurationContent.kt`, `LoanBreakdownBottomSheet.kt`, `TenorBottomSheet.kt`, `LoanInformationFormScreen.kt`, `LoanSelectionBottomSheet.kt`, `ConfirmLoanInformationScreen.kt`.
  - eKYC: `EkycFaceCaptureScreen.kt`, `EkycErrorScreen.kt`, `EkycIntroScreen.kt`.
  - Loan management: `LoanManagementScreen.kt`, `LoanManagementViewModel.kt`.
  - Contract/eSign production strings.
  - Chat/common: `ChatBotScreen.kt`, `AppTextField.kt`.
- KHÔNG localize backend dynamic content (banner, event, contract HTML, reward names, transaction descriptions, notification content).
- Naming: prefix theo screen, e.g. `withdraw_empty_cards`, `loan_mgmt_tab_contracts`, `confirm_info_full_name`. Dùng `action_*` cho shared.
- Replace `"..."` thành `stringResource(R.string.key)`.
- ViewModel/repository: prefer return error code/state, map ở UI; nếu phải có `UiText` pattern thì dùng (không inject Android Context vào repository).
- Placeholder format: `%1$s`, `%1$d`, `%1$.1f`. Currency/month formatting locale-aware ở Kotlin, labels từ strings.
- Key parity: mọi key mới ở `values/strings.xml` phải có ở `values-en/strings.xml`.

### Files likely involved
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-en/strings.xml`
- Toàn bộ files được liệt kê ở bảng hotspots trong AGENT_TASKS.md Final A.

### Acceptance criteria
- [ ] `rg -n 'Text\("|contentDescription = "|Toast\.makeText\([^,]+,\s*"|errorMessage = "|title = "|label = "' app/src/main/java/com/example/easymoney` không còn match trên các screen được audit (trừ debug/preview).
- [ ] Switch VI/EN không để lộ chuỗi hard-coded trên screens được audit.
- [ ] Không thiếu key ở một trong hai locale.
- [ ] Không có raw debug text trên production flow.
- [ ] Build passes: `./gradlew assembleDebug`
- [ ] Unit tests pass: `./gradlew test`

## Notes
- Tham chiếu: AGENT_TASKS.md Final Problem A.
- Chỉ chạy sau khi Problem 0–7 đã hoàn tất + committed.
