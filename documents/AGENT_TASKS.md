# Agent Tasks - Frontend UI Fix Batch

Purpose: this is the only active frontend task list. Old task batches are intentionally removed.

Read first:

- `documents/CLAUDE.md`
- `documents/PROJECT_STRUCTURE.md`
- `documents/API_SPEC.md`
- `documents/backend_contract.yaml`
- This file

Assume the implementation agent may only have the Android frontend repository. Backend source is not available to the frontend agent. Public demo base URL remains `https://easymoney.lamgd.dev/`.

## Source Issues Covered

| Issue | Main files to inspect | Expected outcome |
|---|---|---|
| OTP still fills automatically without user consent | `ui/esign/ContractViewModel.kt`, `ui/common/components/OtpDialog.kt`, `messaging/ContractOtpHolder.kt` | OTP is only filled after the user explicitly chooses the suggestion |
| OTP request endpoint can be spammed by leaving/reopening the dialog | `ui/esign/ContractViewModel.kt`, `ui/esign/ContractUiState.kt` | Existing valid OTP request is reused; sign button reopens dialog instead of calling endpoint again |
| Text pieces are stuck together in contract/OTP UI | `ui/esign/ContractScreen.kt`, `ui/common/components/OtpDialog.kt`, string resources | Text has explicit spacing independent of XML trailing/leading spaces |
| Canceling one contract disables buttons for other contracts until screen reopen | `ui/loan/management/LoanManagementViewModel.kt`, `LoanManagementUiState.kt`, `LoanManagementScreen.kt` | Submitting state is reset and/or scoped to the affected contract |
| Loan suggestion amount slider has awkward far-apart / fractional-looking steps | `ui/loan/configuration/LoanConfigurationContent.kt`, `LoanConfigurationViewModel.kt`, possibly `LoanListScreen.kt` if the observed screen is the list filter | Slider snaps to clean, user-friendly VND increments with readable labels |
| AI chatbot user messages look like raw text without a proper bubble | `ui/chatbot/ChatBotScreen.kt` | User messages render as clear right-aligned bubbles with background/border/padding |

## 1. Contract OTP Requires User Consent To Fill

Current root cause:

- `ContractViewModel` writes incoming API/FCM OTP directly into `otpAutofill`.
- `OtpDialog` receives `prefillOtp` and immediately assigns it to `otpValue`.

Required fix:

- Remove the direct auto-fill path.
- Do not pass a `prefillOtp` that mutates the input field automatically.
- Treat OTP from API response or FCM as a suggestion only.
- Store suggested OTP separately from the typed OTP, for example:
  - `otpSuggestion: String?`
  - `otpInput: String`
  - `otpExpiresAt: Long?`
- `OtpDialog` should show a compact action such as `Điền OTP từ thông báo` only when a valid suggestion exists.
- Only when the user taps that action should the dialog copy `otpSuggestion` into `otpInput`.
- User must still be able to type the OTP manually.
- The final sign request still sends `{ otp, purpose: "SIGN_CONTRACT" }`.

Implementation guidance:

- FCM/API handlers may call something like `setOtpSuggestion(contractId, otp, expiresAt)`.
- Do not call `onOtpChange(otp)` or assign to the input field from a `LaunchedEffect`.
- If using Android keyboard autofill is unreliable for FCM data messages, prefer the in-app suggestion chip/button. The product requirement is user consent, not a particular Android autofill API.

Acceptance:

- Requesting OTP does not populate the six OTP boxes by itself.
- Receiving matching `CONTRACT_SIGN_OTP` FCM does not populate the boxes by itself.
- Tapping `Điền OTP từ thông báo` fills the boxes.
- Manual typing still works.

## 2. Prevent OTP Request Spam And Preserve OTP Dialog State

Current root cause:

- `showOtpDialog()` calls `requestSignOtp()` every time the sign button is pressed.
- Dismissing the dialog sets `showOtpDialog=false` but there is no state representing "OTP was already requested and is still valid".

Required fix:

- Add frontend-only OTP request state per active `contractId`.
- Suggested state model:
  - `NotRequested`
  - `Requesting`
  - `WaitingForOtp(expiresAtMillis: Long?)`
  - `Expired`
  - `Failed(message: UiText?)`
- Pressing `Ký hợp đồng` should:
  - open the OTP dialog;
  - call `requestSignOtp()` only when state is `NotRequested`, `Expired`, or `Failed`;
  - not call the endpoint when state is `Requesting` or valid `WaitingForOtp`.
- Dismissing/tapping outside the OTP dialog should only hide it. It must not clear the OTP request state, typed value, or suggestion.
- Pressing `Ký hợp đồng` again while waiting for OTP should only show the existing OTP dialog.
- Resend should be an explicit action and should reset state only after expiry or when user intentionally retries a failed request.
- Clear OTP state only after successful signing, contract change, or confirmed expiry.

Acceptance:

- Tap `Ký hợp đồng`, dismiss OTP dialog, tap `Ký hợp đồng` again before expiry: no second request-OTP API call is made.
- While request state is `Requesting`, repeated taps do not create concurrent OTP requests.
- Dialog reopening preserves typed OTP/suggestion if still valid.
- After successful sign, OTP holder/state is consumed.

## 3. Fix Contract And OTP Text Spacing

Current root cause:

- Contract agreement text is built by concatenating resource strings and relies on a trailing space in `contract_agree_prefix`.
- OTP description concatenates `otp_desc_1`, phone number, and `otp_desc_2`, relying on leading/trailing spaces in XML.

Required fix:

- Do not rely on resource strings carrying invisible leading/trailing spaces.
- Build annotated strings with explicit `append(" ")` before/after inline styled segments.
- For OTP description, ensure spaces around the phone number are explicit in Kotlin or use a single formatted string resource.
- Prefer formatted resources for sentence templates if it keeps localization cleaner, e.g. `Vui lòng nhập mã OTP được gửi về SĐT %1$s để ký hợp đồng`.

Acceptance:

- Contract text reads naturally: `Tôi đã đọc và đồng ý với Điều khoản...`, not stuck together.
- OTP text reads naturally with spaces around the phone number.
- Vietnamese and English resources both remain valid.

## 4. Fix Contract Cancel Disabling Other Buttons

Current root cause:

- `LoanManagementViewModel.cancelContract()` sets `isSubmitting=true`.
- On success it calls `load()`, but `load()` does not reset `isSubmitting=false`.
- `ContractList` disables all contract buttons via `enabled = !isSubmitting`.

Required fix:

- At minimum, ensure `isSubmitting=false` after cancel success and after `load()` finishes.
- Preferred: scope submitting state to the affected contract/action instead of disabling every contract card.
- Suggested model:
  - replace or supplement `isSubmitting` with `submittingContractId: String?`
  - disable only the card whose id matches `submittingContractId`
  - keep other contracts active.
- Avoid keeping global submit state true after any success path.

Acceptance:

- Cancel one contract successfully; remaining contract cards stay clickable without leaving the screen.
- Cancel error resets submitting state and shows the error.
- Repayment/debt submit loading still behaves correctly.

## 5. Make Loan Suggestion Amount Slider Steps Clean

Observed issue:

- The amount slider in loan suggestion/configuration has awkward steps: jumps feel too far apart in some ranges and selected values can look arbitrary or "lẻ".

Current likely cause:

- `LoanAmountSection` snaps with `((value / 100_000f).toInt() * 100_000L)`, regardless of package min/max range.
- For larger packages this creates too many tiny raw stops; for uneven min/max it can also produce values that do not feel aligned to clean product increments.

Required fix:

- Define a clean amount step based on package range and product money scale.
- Recommended step policy:
  - range <= 5,000,000 VND: step `500,000`
  - range <= 20,000,000 VND: step `1,000,000`
  - range > 20,000,000 VND: step `5,000,000`
- Snap relative to `minAmount`, not absolute zero:
  - `snapped = minAmount + round((raw - minAmount) / step) * step`
  - clamp to `minAmount..maxAmount`
  - ensure exact min/max can still be selected.
- Display amount labels in clean compact format (`5tr`, `10tr`, `50tr`) or full VND where needed.
- If the user meant `LoanListScreen` filter slider instead of loan configuration, apply the same clean-step principle there too. Do not leave one amount slider polished and the other awkward.

Acceptance:

- Slider values land on clean increments such as `5tr`, `6tr`, `7tr`, not arbitrary values.
- Min and max package amounts remain selectable.
- Moving the slider feels smooth enough and does not jump across too much money for small packages.
- Displayed selected amount matches the value passed into quote/application state.

## 6. Improve AI Chatbot User Message Bubble

Observed issue:

- User messages in the AI chatbot look like plain text and are visually hard to distinguish.

Current likely cause:

- `MessageRow` uses a `Surface` for both roles, but user messages use `MaterialTheme.colorScheme.surface` and `fillMaxWidth(0.85f)`, making the user bubble blend into the page.

Required fix:

- Render user messages as a clearly visible right-aligned bubble:
  - width wraps content up to a max width, not always `fillMaxWidth(0.85f)`;
  - background should differ from the screen surface, e.g. `primary` / `primaryContainer` depending on contrast;
  - text color must meet contrast (`onPrimary` or `onPrimaryContainer`);
  - add rounded corners, with a slightly different corner on the speaker side if desired;
  - add internal padding around text;
  - optionally add a subtle border if using a light background.
- Bot messages should remain left-aligned and readable.
- Cards/actions from bot should keep their existing structure, only polished if needed to avoid layout mismatch.

Acceptance:

- User text messages visibly appear inside a bubble.
- User and bot messages are easy to distinguish at a glance.
- Long user messages wrap cleanly and do not span edge-to-edge.
- Chat input row remains usable and visually separated from the message list.

## Verification Checklist

Run from frontend root:

```powershell
.\gradlew.bat build
```

Manual verification:

- OTP is not inserted until the user taps the OTP suggestion action.
- Dismiss/reopen OTP dialog before expiry does not call request-OTP again.
- OTP resend is explicit and does not create duplicate concurrent requests.
- Contract agreement text and OTP phone text have correct spacing.
- Canceling one contract does not disable other contract buttons.
- Loan amount slider snaps to clean increments and still reaches min/max.
- Chatbot user messages render as distinct right-aligned bubbles with readable background/text contrast.
