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

- Current product target is `REMOTE` mode as a commercial app. Do not rely on MOCK mode as a fallback for production flows.
- Backend `/api/v1/loan/package/my` currently returns `ApiResponse[List[LoanPackageDto]]`, while frontend `LoanApiService.getMyPackage()` expects `ApiResponse<LoanPackageModel>`. Check the YAML before changing this call.
- Notification DTO in backend includes `category`; frontend current DTO does not.
- Backend notification `amount` and `balance_after` can be fractional JSON numbers; frontend currently models them as `Long?`.
- Backend payment card `balance` is a float; frontend currently models it as `Long`.
- Backend transaction item DTO includes `timestamp`; frontend current transaction domain model omits it and can display groups/items in backend arrival order. Frontend must sort transaction history newest first.
- Generic `ApiResponse<T>` in backend has optional nullable `data`; frontend currently declares `data: T` non-null.
- Backend user reward response uses `total_points`; frontend model expects `totalPoints`. Gson policy should map it, but add tests if changing models.
- Backend `BannerInfo` includes optional `action_url`; frontend current `Banner` model ignores it and uses `target_id` for click handling. This is acceptable for current seed because `WEB.target_id` is the canonical HTTPS URL, but add `actionUrl` to frontend if direct embedded WebView banners are required.
- Several services exist outside `LoanApiService`, but some legacy endpoints remain there. Avoid duplicating calls unless you are intentionally splitting service ownership.
