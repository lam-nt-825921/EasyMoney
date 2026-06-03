# Workflow #79 — Reward Points and Backend-Only Credit Score Separation

## Goal
Treat reward points and credit score as separate concepts. Credit score is backend-only in the current frontend scope.

## Requirements

### Functional
- Reward points are spendable loyalty points; credit score is a backend underwriting score. Never compute credit score from reward points on the frontend.
- Do **not** add or consume `GET /api/v1/user/credit-score` in this frontend batch. There is no current screen that displays user credit score.
- Do **not** add `CreditScoreDto`, `CreditScoreModel`, a `getCreditScore()` repository method, a credit-score ViewModel, or any credit-score UI surface unless a future approved design explicitly asks for it.

  Backend credit-score endpoint shape, documented for context only:
  ```json
  { "credit_score": 720, "tier": "GOOD", "min_score": 0, "max_score": 1000,
    "total_reward_points": 1347,
    "explanation": "Điểm tín dụng dùng để xét điều kiện vay; điểm thưởng dùng riêng cho ưu đãi và voucher." }
  ```
- Reward endpoints/events: `GET /api/v1/rewards/user`; sign (`POST /api/v1/loan/contracts/{contract_id}/sign`) and repay (`POST /api/v1/loan/debts/{debt_id}/repay`) responses may return `reward_points_added`, `total_reward_points`, `credit_score_delta`, `credit_score`.
- Frontend may ignore `credit_score_delta` and `credit_score` in sign/repay responses because no UI displays them. These fields must not be treated as reward points.
- After disbursement (sign success) or any repay success, refresh: debts, wallet, rewards, and profile state where visible.

### Files likely involved
- `app/src/main/java/com/example/easymoney/data/remote/**` (sign/repay result DTOs, reward DTOs)
- `app/src/main/java/com/example/easymoney/domain/model/**` (reward model)
- `app/src/main/java/com/example/easymoney/domain/repository/**` (reward + loan repositories)
- `app/src/main/java/com/example/easymoney/ui/reward/**`, `ui/loan/management/**`, `ui/payment/**`, `ui/account/**`, `ui/esign/**`

### Acceptance criteria
- [x] Reward point display changes only from rewards API or returned backend result.
- [x] No credit-score UI/API consumer is added in this batch.
- [x] Credit score is never derived from reward points.
- [x] Loan eligibility remains backend-driven through package eligibility and backend reason codes.
- [~] After disbursement/repay success, debts, wallet, rewards, and visible profile state refresh. _(repay path explicit; disbursement via fresh-VM refetch — see Current State)_
- [x] Build passes: `./gradlew assembleDebug`
- [x] Unit tests pass: `./gradlew test`

## Notes
- Source of truth: `documents/AGENT_TASKS.md` task 4 (2026-06-02 batch).
- `/api/v1/user/credit-score` is backend-owned context only. Do not fetch it from frontend in this workflow.

## Current State — 2026-06-03

**Status: Done for current scope.** The credit-score endpoint is intentionally not consumed because the frontend has no credit-score UI.

### Done
- `ui/reward/RewardViewModel.kt` — `RewardUiState.totalPoints` default `1250 → 0` so no fake placeholder shows before the rewards API loads.

### Verified already satisfied by existing architecture (no change needed)
- **Reward points come only from the API.** Home (`HomeViewModel`), Account (`AccountViewModel`), and Reward (`RewardViewModel`) all read `rewardRepository.getRewardsCatalog().totalPoints`; their `rewardPoints` defaults are `0`.
- **Credit score is not derived from reward points.** No frontend code computes a credit score. The only "credit score" values in the UI are the loan *package* threshold (`LoanPackageModel.eligibleCreditScore`) and the backend reason code `LOW_CREDIT_SCORE` — not a user score.
- **Loan eligibility is backend-driven.** `LoanRepositoryImpl.getLoanPackages` / `checkEligibility` return `isEligible` + `reasonCode` from the backend in REMOTE mode; the frontend never recomputes eligibility from points.
- **Repay refresh.** `LoanManagementViewModel.repayDebt` already refreshes rewards (`getRewardsCatalog`) and reloads debts (`load()`) on success (workflow #71).
- **Disbursement refresh.** Login/navigation clears the backstack (`popUpTo(0) { inclusive = true }`), so screens reached after signing (loan management, wallet) construct fresh ViewModels that refetch from the backend.

### Explicit Non-Goal
- Do not implement user credit-score fetching or display. If a future design introduces a visible credit-score screen, create a separate workflow for `GET /api/v1/user/credit-score`.

All other criteria pass; `assembleDebug` and `testDebugUnitTest` were green in Claude's run.

> Note: verification was build + unit tests only. The reward/profile refresh after a real disbursement/repayment needs a manual REMOTE run on a device/emulator to confirm end-to-end.
