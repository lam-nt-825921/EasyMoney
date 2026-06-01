# API Spec - Agent Backend Contract Index

Version: 2026-05-30.4.

This frontend repo is not the source of truth for backend implementation. GitHub agents may only have the mobile frontend source, so this file is the backend HTTP contract summary they should use for frontend work.

Do not depend on local backend file paths or backend source availability. Treat the public demo backend and the endpoint contracts below as the integration boundary.

Machine-readable snapshots included in this repo:

- `documents/backend_contract.yaml`: preferred contract snapshot with endpoint list, auth requirement, DTO schemas, seed/demo behavior, and banner contract.
- `documents/backend_openapi.yaml`: raw FastAPI OpenAPI snapshot. Use it when request-body details are missing from `backend_contract.yaml`.

## Runtime

- Backend framework: FastAPI + SQLModel + SQLite.
- Public demo backend/tunnel: `https://easymoney.lamgd.dev/`.
- Frontend default base URL should stay `https://easymoney.lamgd.dev/` for demo and GitHub agent work.
- Local desktop backend, only when explicitly testing locally: `http://localhost:8000/`.
- Local Android emulator backend, only when explicitly testing local backend: `http://10.0.2.2:8000/`.
- Backend process management is outside the frontend repo. Frontend agents should not assume they can edit or run backend source.

## Auth

Protected endpoints require:

```text
Authorization: Bearer mock_access_token_{user_id}
```

Seed accounts:

| Phone | Password | Expected token |
|---|---|---|
| `0987654321` | `123` | `mock_access_token_1` |
| `0912345678` | `123` | `mock_access_token_2` |
| `0909090909` | `123` | `mock_access_token_3` |

Seed personas:

- `0987654321`: verified high-score active borrower with active debt, approved contract, cards, rewards, and notifications.
- `0912345678`: verified lower-score borrower with a rejected application and an active older debt.
- `0909090909`: incomplete eKYC user with limited profile data.

Login/register endpoints:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`

Response model: `ApiResponse[AuthTokenDto]`, with `accessToken`, `refreshToken`, `expiresIn`, and backend may include `user`.

Registration contract:

- `POST /api/v1/auth/register` creates `User` and one EasyMoney wallet `Account`.
- It must not create a default `PaymentCard`.
- A newly registered user's `GET /api/v1/payment/cards` response is `data: []` until the user links a card through `POST /api/v1/payment/cards`.
- If the frontend shows a card immediately after registration in `REMOTE` mode, that is frontend stale mock/cache state and must be fixed in frontend.

## Response Envelope

Most JSON endpoints return:

```json
{
  "status": "success",
  "message": "optional",
  "data": {}
}
```

FastAPI `HTTPException` errors return `{ "detail": "..." }`; validation errors return FastAPI's `HTTPValidationError`.

`data` is treated as **nullable** on the frontend (workflow #59). Mutating endpoints that return `ApiResponse[None]` (`{}` or `null` payload) are explicitly modelled as `Resource<Unit>` via `safeUnitApiCall`. Backend may keep returning `{}` or omit `data` for unit-style endpoints — frontend will not crash. Endpoints that promise a payload must still return non-null `data` on `status=success`; a null `data` on success is treated as Error.

## Money Handling Contract

**Single rule: monetary amounts in JSON are numbers (float / int). The frontend parses every money field as `Double` and rounds to integer VND only at the display layer.**

### Why

Backend repayment math produces fractional VND in real responses we have seen, e.g.:

```json
{ "amount": -983333.3333333334, "balance_after": 12345678.5 }
```

Previously the frontend modelled these as `Long`, which produces a Gson parse failure (`NumberFormatException`) on any fractional value — the entire response is rejected and the screen breaks. The frontend has now been migrated to parse `Double?`, so fractional values do not crash anything.

### What this means for backend

- **You do not need to round before sending.** Frontend tolerates fractional amounts.
- **But please do round** wherever your math allows, because:
  - Customer-facing display in VND has no fractional unit (there is no "0.33 VND" in real life).
  - Fractional values in DB persistence accumulate floating-point drift across repayments.
  - QA gets confused when the UI shows `-983,333 đ` and the backend log shows `-983,333.3333333334`.
- **Recommended backend approach:** round repayment splits to integer VND at the moment of computation (e.g. last instalment absorbs the rounding remainder) and persist `Decimal`/`int`. Send integers in JSON when you can.

### Fields covered by this rule

All of these are parsed as `Double` on the frontend:

| Endpoint | Field |
|---|---|
| `GET /api/v1/notifications` | `amount`, `balance_after` |
| `GET /api/v1/payment/cards` | `balance` |
| `GET /api/v1/payment/wallet` | `available_balance`, `recent_flows[].amount` |
| `GET /api/v1/transactions` | `items[].amount`, `items[].balance` |
| `POST /api/v1/payment/topup` request | `amount` (still `Long` — input, never fractional) |
| `POST /api/v1/payment/withdraw` request | `amount` (still `Long` — input, never fractional) |

Request-side money fields (topup, withdraw, repay) stay integer — users only ever enter whole VND.

### Display format

Frontend rounds `Double` → `Long` (truncating toward zero) and formats with locale-aware thousands separator (`vi-VN`: `1.234.567 đ`; `en-US`: `1,234,567 đ`). Negative is shown with the locale's minus sign.

## Naming

- Backend DTOs often use `snake_case`.
- Frontend Gson is configured with `LOWER_CASE_WITH_UNDERSCORES`.
- Use `@SerializedName` explicitly for mixed naming, especially in DTOs currently declared in `LoanApiService.kt`.

## Endpoint Groups Frontend Uses

Prefer `/api/v1` paths.

| Domain | Backend paths | Frontend service |
|---|---|---|
| Auth | `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/auth/change-password` | `LoanApiService`, `UserApiService` |
| User | `/api/v1/user/profile`, `/api/v1/user/profile/completion`, `/api/v1/user/account` | `UserApiService`, `LoanApiService.getProfile` legacy |
| Home | `/api/v1/home/banners`, `/api/v1/home/hot-loans`, `/api/v1/home/recommended-loan`, `/api/v1/support/customer-care` | `HomeApiService` |
| Loan | `/api/v1/loan/package`, `/api/v1/loan/package/my`, `/api/v1/loan/package/{id}`, `/api/v1/loan/applications`, `/api/v1/loan/contracts/*`, `/api/v1/loan/debts/*` | `LoanApiService` |
| Master Data | `/api/v1/master/metadata`, `/api/v1/master/districts/{provinceId}`, `/api/v1/master/wards/{districtId}` | `LoanApiService` |
| eKYC | `/api/v1/ekyc/session`, `/api/v1/ekyc/status`, `/api/v1/ekyc/match`, `/api/v1/ekyc/capture/face`, `/api/v1/ekyc/capture/face-base64`, `/api/v1/ekyc/document/upload`, `/api/v1/ekyc/document/nfc` | `LoanApiService` |
| OTP | `/api/v1/otp/send`, `/api/v1/otp/verify` | `LoanApiService` |
| Notifications | `/api/v1/notifications`, `/api/v1/notifications/{id}/read`, `/api/v1/notifications/read-all`, `/api/v1/notifications/clear`, `/api/v1/notifications/fcm-token` | currently `LoanApiService` |
| Rewards | `/api/v1/rewards/catalog`, `/api/v1/rewards/user`, `/api/v1/rewards/{item_id}/redeem` | `RewardApiService` |
| Payment | `/api/v1/payment/cards`, `/api/v1/payment/wallet`, `/api/v1/payment/topup`, `/api/v1/payment/withdraw`, `/api/v1/payment/auto-deduction`, `/api/v1/payments/qr` | `PaymentApiService` |
| Transactions | `/api/v1/transactions` | `TransactionHistoryApiService` |
| Chatbot | `/api/v1/chat/message`, `/api/v1/chatbot/chat` | `ChatApiService` |
| Events/Web | `/api/v1/events/{id}`, `/api/v1/events/{id}/join`, `/event/{id}`, `/cskh` | `EventApiService`, `WebContentScreen` |

## Banner And Web Contract

Backend seed banners are designed to match current frontend navigation:

| target_type | target_id meaning | Expected frontend behavior |
|---|---|---|
| `LOAN` | Loan package id, e.g. `package_fast_001` | Navigate to `LoanDetail` |
| `EVENT` | Event id, e.g. `e1` or `e2` | Navigate to `EventDetail`; event `action_url` may open WebView or a native deeplink |
| `WEB` | Absolute HTTPS URL | Current frontend opens through `LinkHandler.openUrl`; if product wants embedded WebView, change banner click handling |

Canonical demo web URLs:

- `https://easymoney.lamgd.dev/event/e1`
- `https://easymoney.lamgd.dev/pages/security-policy`
- `https://easymoney.lamgd.dev/pages/credit-score-guide`
- `https://easymoney.lamgd.dev/cskh`

These are normal backend `GET` endpoints returning `text/html`; do not replace them with frontend-local static pages.

## Current Integration Warnings

Active:

- Current product target is `REMOTE` mode as a commercial app. Do not rely on MOCK mode as a fallback for production flows.
- Frontend handles non-standard backend errors generically: it prefers a structured `code` field when present, and falls back to known markers inside `message` / `detail` / plain text. Backend changes are not required for current workflows.
- Backend `BannerInfo` includes optional `action_url`; frontend current `Banner` model ignores it and uses `target_id` for click handling. This is acceptable for current seed because `WEB.target_id` is the canonical HTTPS URL, but add `actionUrl` to frontend if direct embedded WebView banners are required.
- Several services exist outside `LoanApiService`, but some legacy endpoints remain there. Avoid duplicating calls unless you are intentionally splitting service ownership.
- Backend user reward response uses `total_points`; frontend model expects `totalPoints`. Gson policy maps it, but add tests if changing models.

Resolved on frontend (workflow #59 batch, 2026-06-01):

- ✅ `ApiResponse<T>.data` is now nullable on frontend; `safeApiCall` treats success-with-null as Error; `safeUnitApiCall` handles mutating endpoints that return `{}` / `null`.
- ✅ `GET /api/v1/loan/package/my` now correctly parsed as a list; frontend picks the first package.
- ✅ Notification `category` is parsed and persisted in Room (workflow #54).
- ✅ Notification `amount` and `balance_after` parsed as `Double?` per the Money Handling Contract above.
- ✅ Payment card `balance` parsed as `Double` via `PaymentCardDto`, rounded to `Long` at the domain boundary.
- ✅ Wallet `available_balance` and `recent_flows[].amount` parsed as `Double` via `WalletInfoDto`.
- ✅ Transaction history DTOs include `timestamp`; frontend sorts newest-first at both group and item level (workflow #60).
- ✅ `EkycStatusDto` (workflow #59) parses rich fields (`status`, `session_id`, `document_method`, `verified_at`, `match_score`) — frontend domain model is forward-compatible but UI currently only displays the existing summary.

## Asks for Backend

These are not bugs in the current contract — they are improvements that would simplify the integration:

1. **Round monetary outputs to integer VND** at compute time wherever possible (see Money Handling Contract). Frontend tolerates fractional, but rounded is cleaner.

2. **Document a canonical error-code field.** _Most important after #1._ Today frontend pattern-matches strings like `"CARD_REQUIRED"` and `"chưa thêm thẻ"` inside `message` because the only structured signal is the human-readable text. Please return errors in this shape going forward:

   ```json
   {
     "status": "error",
     "code": "CARD_REQUIRED",
     "message": "User has no linked payment card"
   }
   ```

   The `code` should be `SCREAMING_SNAKE_CASE` and stable across versions; `message` is for human/debug context and is allowed to change. Frontend already has a `BackendErrorCode` enum (`app/src/main/java/com/example/easymoney/ui/common/error/BackendErrorCode.kt`) — when `code` is added we'll switch from `message.contains(...)` matching to reading `code` directly. The codes we currently rely on or plan to consume:

   | code | When backend returns it | Frontend reaction |
   |---|---|---|
   | `CARD_REQUIRED` | Topup / withdraw / repay without a linked card | Navigate to Payment Cards screen |
   | `INSUFFICIENT_BALANCE` | Withdraw / repay > available balance | Show inline error, no navigation |
   | `INVALID_AMOUNT` | Amount below min / above max / non-numeric | Show inline error |
   | `NETWORK_ERROR` | Backend can't reach a downstream (rare; if you surface it, please use this exact code) | Show retry CTA |

   Please tell us if you add new codes — frontend will add a mapping entry. New codes without a frontend mapping fall back to showing `message` verbatim.

3. **Confirm `AuthTokenDto.user` is populated** on `POST /auth/login` and `POST /auth/register`. Frontend now reads it (workflow #59) to cache user profile and avoid a `GET /user/profile` round-trip immediately after login.

4. **Stable `mock_access_token_{id}` format**: frontend derives the current user id by stripping the `mock_access_token_` prefix (see `AppPreferences.currentUserId`). If you change the token format in REMOTE production, please tell mobile first.

5. **`GET /api/v1/ekyc/status` rich fields**: please keep `status`, `session_id`, `document_method`, `verified_at`, `face_match_score`, `document_match_score`, `is_identified`, `missing_documents` in the contract. Frontend persists them all; if any disappear, future detail screens break.

6. **`ApiResponse[None]` is fine to return as `{}` or `null`** for mutating endpoints (DELETE notifications/clear, POST /otp/send, etc.). Frontend treats these as `Resource<Unit>` via `safeUnitApiCall` and never reads `data` for them. No change needed on your side — this entry is just to confirm the contract.
