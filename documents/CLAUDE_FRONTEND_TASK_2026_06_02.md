# Claude Frontend Task - Backend-Aligned Fix Batch

Date: 2026-06-02.

Read first:

- `documents/CLAUDE.md`
- `documents/PROJECT_STRUCTURE.md`
- `documents/API_SPEC.md`
- `documents/backend_contract.yaml`
- This file.

The backend source is not available to the frontend agent. Treat the backend contract below as the expected integration boundary for this task batch. The public demo base URL remains `https://easymoney.lamgd.dev/`.

## Product Direction Change

The previous final task about making biometric 2FA real is superseded. For this batch, remove user-facing biometric/sinh trắc học functionality from the app. Biometric must not be required for withdraw, repayment, contract signing, or identity/profile completion.

## Backend Contract Delta Expected For This Batch

These endpoints may not exist in the old snapshot yet. Implement frontend against this contract where possible, and handle missing endpoints with clear user-facing errors rather than mock success in `REMOTE`.

### Rewards After Repayment

When a debt repayment or early settlement succeeds, backend will add reward points to the current user.

- Existing endpoint: `POST /api/v1/loan/debts/{debt_id}/repay`
- Existing request:

```json
{
  "repay_type": "MONTHLY",
  "payment_method": "WALLET",
  "card_id": null
}
```

- Success remains `ApiResponse[dict]` or `ApiResponse[RepayResultDto]`.
- If backend returns `RepayResultDto`, parse these optional fields:

```json
{
  "debt_id": 1,
  "repay_type": "MONTHLY",
  "amount_paid": 983333,
  "reward_points_added": 98,
  "total_reward_points": 1498,
  "debt_status": "ACTIVE"
}
```

Frontend requirement: after successful repay/settle, refresh rewards from `GET /api/v1/rewards/user` and update Home/Account/Rewards state when the user returns to those screens.

### Repayment Estimate

Backend will expose an estimate endpoint so the UI can show the exact amount before the user confirms monthly repayment or early settlement.

- `GET /api/v1/loan/debts/{debt_id}/repayment-estimate`
- Auth: required.
- Query:
  - `repay_type`: `MONTHLY` or `FULL_EARLY`
  - `payment_method`: optional, `WALLET` or `CARD`
  - `card_id`: optional string
- Response: `ApiResponse[RepaymentEstimateDto]`

```json
{
  "debt_id": 1,
  "repay_type": "FULL_EARLY",
  "payment_method": "WALLET",
  "amount_due": 11760000,
  "principal_due": 10000000,
  "interest_due": 1500000,
  "penalty_fee": 260000,
  "discount_amount": 0,
  "reward_points_preview": 117,
  "currency": "VND",
  "debt_status_after_payment": "PAID"
}
```

Money fields are JSON numbers; parse as `Double` if the existing payment/loan DTO strategy uses `Double`, then round only for display.

### Contract Creation And Detail

Backend will expose canonical contract APIs under `/api/v1/loan/contracts`.

- `POST /api/v1/loan/contracts`
- Auth: required.
- Request:

```json
{
  "application_id": "APP_XXXXXXXX"
}
```

- Response: `ApiResponse[LoanContractDetailDto]`

- `GET /api/v1/loan/contracts/{contract_id}`
- Auth: required.
- Response: `ApiResponse[LoanContractDetailDto]`

DTO:

```json
{
  "id": "CONTRACT_APP_XXXXXXXX",
  "application_id": "APP_XXXXXXXX",
  "contract_number": "APP_XXXXXXXX",
  "amount": 20000000,
  "term_months": 12,
  "interest_rate": 1.5,
  "approved_at": 1717240000000,
  "status": "APPROVED",
  "content": "Hợp đồng vay ...",
  "html_content": null,
  "otp_required": true
}
```

Existing `GET /api/v1/loan/contracts/approved` may still be used for list screens. Prefer the new detail endpoint for the contract screen. Do not depend on legacy `GET /api/v1/contracts/{contractId}` except as temporary fallback if already wired.

### Contract OTP Via FCM

During contract signing, backend will send OTP by Firebase Cloud Messaging data payload.

- `POST /api/v1/loan/contracts/{contract_id}/sign/request-otp`
- Auth: required.
- Response: `ApiResponse[dict]` with optional `expires_at`.

Expected FCM data payload:

```json
{
  "type": "CONTRACT_SIGN_OTP",
  "purpose": "SIGN_CONTRACT",
  "contract_id": "CONTRACT_APP_XXXXXXXX",
  "otp": "123456",
  "expires_at": "1717240300000"
}
```

Frontend requirement: `EasyMoneyMessagingService` must parse this payload, store/emit it to the contract signing UI, and auto-fill the OTP field only when `purpose == SIGN_CONTRACT` and `contract_id` matches the currently signing contract. Never auto-submit only because an OTP arrived; keep the user's final confirm action.

Final signing still calls:

- `POST /api/v1/loan/contracts/{contract_id}/sign`

If the current backend still uses the older sign endpoint to both send OTP and sign, keep frontend code structured so the OTP request/sign steps are separate once the new endpoint is available.

### Loan Approval Notification

After `POST /api/v1/loan/applications` returns an approved application, backend will create a notification and send FCM push to registered devices.

Expected FCM data payload:

```json
{
  "type": "LOAN_APPLICATION_APPROVED",
  "application_id": "APP_XXXXXXXX",
  "contract_id": "CONTRACT_APP_XXXXXXXX",
  "title": "Hồ sơ vay đã được duyệt",
  "body": "Vui lòng kiểm tra và ký hợp đồng để nhận giải ngân.",
  "target_type": "CONTRACT",
  "target_id": "CONTRACT_APP_XXXXXXXX"
}
```

Frontend requirement: ensure FCM token registration still calls `POST /api/v1/notifications/fcm-token` after login/register and app start when authenticated. Notification click should navigate to the contract screen when `target_type == CONTRACT`.

### Payment Wallet Recent Flows

- Existing endpoint: `GET /api/v1/payment/wallet`
- Contract: `recent_flows` must represent the newest recent transactions first.

Frontend requirement: preserve backend ordering if it is newest-first. If existing frontend sorts recent flows, sort by `timestamp DESC`.

### Bank Card Add/Verify

Backend will support a clearer add-card UX with bank/card-type metadata and structured validation errors.

- `GET /api/v1/payment/banks`
- Auth: not required or required is acceptable; frontend should call through authenticated client if available.
- Response: `ApiResponse[List[BankDto]]`

```json
[
  {
    "id": "VCB",
    "name": "Vietcombank",
    "short_name": "VCB",
    "bin_prefixes": ["970436"],
    "logo_url": null,
    "supported_card_types": ["DEBIT", "CREDIT"]
  }
]
```

- `POST /api/v1/payment/cards/verify`
- `POST /api/v1/payment/cards`
- Request:

```json
{
  "bank_id": "VCB",
  "bank_name": "Vietcombank",
  "card_type": "DEBIT",
  "card_number": "9704361234567890",
  "card_holder_name": "NGUYEN VAN A",
  "expiry_month": "12",
  "expiry_year": "2029"
}
```

Structured error shape:

```json
{
  "status": "error",
  "code": "INVALID_CARD_NUMBER",
  "message": "Số thẻ không hợp lệ.",
  "field_errors": {
    "card_number": "Số thẻ phải có 16-19 chữ số."
  }
}
```

Frontend requirement: show inline validation errors and preserve all user-entered fields when verify/add fails. Do not clear the form on validation failure. Use dropdowns for bank and card type.

## Frontend Tasks

### 1. Remove Biometric UX

- Remove or hide biometric/sinh trắc học entries from security settings and identity/profile completion.
- Remove biometric gating from withdraw, debt repayment, early settlement, and contract signing if any exists.
- Remove user-facing strings related to biometric from production screens if they become unused.
- Do not delete low-level helper files unless that is low-risk and build-safe. The key requirement is no production UX and no required biometric flow.

Acceptance:

- Searching production UI code for `biometric`, `Sinh trắc`, `sinh trắc` should not reveal active user-facing controls or required flows.
- Withdraw, repay, settle, and contract signing proceed through their normal non-biometric confirmation paths.

### 2. Loan Management Repayment Estimate

- Add repository/data-source/service support for `GET /api/v1/loan/debts/{debt_id}/repayment-estimate`.
- In `LoanManagementScreen`, show the estimate before final confirmation for both `MONTHLY` and `FULL_EARLY`.
- Include amount due, penalty/fee if present, and reward points preview if present.
- If the estimate endpoint is unavailable, show a clear error/retry state; do not silently guess an amount in `REMOTE`.

Acceptance:

- User can see "how much will be paid" before confirming monthly payment or early settlement.
- Successful repay refreshes debts, wallet, transaction history if already part of the screen flow, and rewards/user points.

### 3. Contract Creation, Detail, And OTP Autofill

- Add DTOs and Retrofit calls for:
  - `POST /api/v1/loan/contracts`
  - `GET /api/v1/loan/contracts/{contract_id}`
  - `POST /api/v1/loan/contracts/{contract_id}/sign/request-otp`
  - `POST /api/v1/loan/contracts/{contract_id}/sign`
- Use the detail endpoint in `ContractScreen`.
- Parse FCM `CONTRACT_SIGN_OTP` data messages and auto-fill the matching contract's OTP field.
- Do not auto-submit the OTP automatically.
- Keep contract signing resilient if FCM arrives before the screen is visible: store the latest OTP by `contract_id` in a local in-memory/state holder or preferences with short expiry.

Acceptance:

- Approved application can lead to a contract detail screen.
- OTP from FCM fills the OTP field for the matching contract only.
- Wrong contract/purpose payload is ignored.

### 4. Loan Approval Push Navigation

- Ensure FCM token registration happens after authentication.
- Extend notification payload handling for `LOAN_APPLICATION_APPROVED`.
- Notification tap with `target_type == CONTRACT` opens `contract?contractId=<target_id>`.
- After loan application submission returns `APPROVED`, refresh notifications or make the success screen able to navigate to contract when contract id is available.

Acceptance:

- Approval push is displayed/handled.
- User can navigate from notification to the contract screen.

### 5. Money Management Recent Transactions

- Audit `MoneyManagementScreen`, `PaymentViewModel`, and payment mappers for `recent_flows`.
- Ensure recent flows are newest-first using `timestamp DESC`.
- Do not replace backend wallet data with mock/sample data in `REMOTE`.

Acceptance:

- Wallet recent transaction section shows newest transaction first.

### 6. Add Bank Card UX

- Add bank dropdown using `GET /api/v1/payment/banks`.
- Add card type dropdown with `DEBIT` and `CREDIT`, constrained by selected bank's `supported_card_types` when available.
- Add inline validation for:
  - bank required
  - card type required
  - card number 16-19 digits after removing spaces
  - card holder name required, uppercase display preferred
  - expiry month/year valid and not expired if fields are present
- Preserve form state on failure.
- Parse backend `field_errors` and map them to inline fields. Unknown errors should be shown as a form-level message.
- Keep existing empty-card state after registration; do not create or fake cards locally.

Acceptance:

- Invalid card input does not crash or navigate away.
- User does not have to re-enter the whole form after a validation error.
- Bank and card type are selected through dropdowns.

## Verification Checklist

Run from frontend root:

```powershell
.\gradlew.bat build
```

Manual flows to verify in `REMOTE` mode:

- Login as `0987654321` / `123`.
- Money management recent flows are newest-first.
- Add card form validation preserves data on error.
- Loan management shows repayment estimate before monthly repay and early settlement.
- Successful repay refreshes reward points.
- Submit loan and handle approved notification/contract navigation.
- Contract OTP FCM payload auto-fills but does not auto-submit.
- No production biometric UI remains.
