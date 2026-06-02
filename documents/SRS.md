# Business And UI Context - Agent Notes

Version: 2026-05-30.1.

EasyMoney is a consumer lending Android app. It covers login/register, home discovery, loan onboarding and application, eKYC, contract signing, wallet/payment, transaction history, notifications, rewards, account/profile, support, and chatbot.

This document explains what each screen means so an agent can avoid making technically valid but business-wrong edits.

## Global Product Rules

- Brand/provider name is EasyMoney unless the UI is intentionally displaying a third-party product such as a telecom voucher.
- Users must authenticate before using account-specific features.
- Profile and eKYC completion are backend-owned truth. Frontend can display cached status but should refetch after profile/eKYC changes.
- The product goal is a commercial-quality mobile app in `REMOTE` mode against `https://easymoney.lamgd.dev/`.
- `MOCK` mode is not a product requirement for the current work. Do not optimize, protect, or regress production UX for MOCK mode.
- `REMOTE` mode must call backend and surface errors; it must not silently fall back to mock/sample success.
- Main navigation tabs are Home, Transaction History, Notifications, Account.
- Important loan/payment/signing actions should not depend only on frontend flags; backend validates state.
- Sandbox/developer UI must not appear on production user-facing screens. Home should not expose sandbox/developer mode.
- Time-based lists must display newest items first and oldest items later.
- Current production default is Vietnamese. Language switching UI is out of scope for this batch.
- Current production default is light theme. Dark/light switching UI is out of scope for this batch.
- Reward points are loyalty points; credit score is a separate backend-owned underwriting value.

## Screen Meanings

### Auth

- `WelcomeScreen`: entry screen before login/register.
- `LoginScreen1`: phone/password login. On success, token is saved and app navigates to Home.
- `RegisterScreen1`: creates user account. Backend creates one EasyMoney wallet account only; it does not create a payment card. The user links bank cards later in Payment Cards.
- `QuickLoginScreen1`: local remembered-account convenience, not backend identity by itself.

### Home

Purpose: user's daily dashboard.

Shows:

- Greeting from backend profile.
- Reward points from rewards backend.
- Banners and hot/recommended loans.
- Profile/eKYC completion warning.
- Shortcuts to chatbot and loan management.

Must not show:

- Sandbox/developer mode entry points.
- Debug labels or controls meant only for testing data source mode.

Data sources:

- `HomeRepository` for banners/hot loans/recommended loan/support.
- `RewardRepository.getRewardsCatalog()` for points.
- `UserRepository` for profile and completion status.

### Loan Discovery

Purpose: browse packages before entering application flow.

- `LoanListScreen`: filter/search packages.
- `LoanDetailScreen`: package details and register CTA.
- Details-first rule: user should see terms and limits before eligibility/application.

### Loan Onboarding And Application

Purpose: collect consent and application data, then submit loan application.

Flow:

1. `OnboardingScreen`: provider/product information and terms acceptance.
2. `ConfirmInfoScreen`: confirm identity/profile readiness.
3. `LoanFlowScreen`: stepper host for information form, eKYC, confirmation, success.
4. `LoanInformationFormScreen`: address, income, profession, marital/contact data.
5. eKYC screens: face/document/NFC verification.
6. `LoanRegistrationSuccessScreen`: application submitted, waiting/review state.

Business details:

- Onboarding provider info should be compact and readable. Short fields can stay on one line; long address should wrap.
- The bottom CTA in form screens must not cover form content.
- Success UI must use app theme colors and EasyMoney copy.

### Profile And Identity

Purpose: user's verified personal data and completion path.

- `ProfileScreen`: overview.
- `ProfileCompletionScreen`: missing data/eKYC actions.
- `EditPersonalInfoScreen`, `EditJobInfoScreen`, `EditContactInfoScreen`: edit profile sections.

Rules:

- Master data should come from backend/repository.
- `identityDocumentVerified = nfcVerified OR documentUploadVerified`.
- Device-local 2FA is not part of the current production identity/profile UX.

### Rewards

Purpose: show points, catalog, redeemed vouchers, and redemption flow.

- `RewardScreen` should use backend/user rewards in `REMOTE`.
- Home and Account should display the same current points source.
- Redeem should update point balance from backend response.
- Reward points must not be used as credit score. Credit score comes from backend credit-score contract.

### Account

Purpose: profile shortcut, reward points, wallet/card/history shortcuts, support, settings, logout.

Important:

- Points must not be hard-coded.
- Support opens backend support link when available.
- Change password is the account security action.

### Notifications

Purpose: user-visible events and reminders.

Backend categories/types:

- `type`: legacy display grouping such as `transaction`, `promotion`, `reminder`.
- `category`: backend category such as `BALANCE`, `PROMOTION`, `REMINDER`.
- `target_type` and `target_id`: navigation targets like loan package, loan debt, transaction.

Frontend should:

- Sync backend notifications into Room cache in `REMOTE`.
- Use current authenticated user id for Room rows and FCM payload rows.
- Never show notification rows belonging to another account. Newly registered users start with an empty notification list unless backend created user-specific notifications.
- Sync mark-read/read-all/clear in `REMOTE`.
- Sort notifications by backend `timestamp` descending before display and after cache refresh. Newest notifications must be at the top for every tab/filter.

### Payment And Transactions

Purpose: wallet, cards, topup, withdraw, auto deduction, QR, history.

Rules:

- Frontend must not invent money state in `REMOTE`.
- Backend response should drive balances and transaction history.
- A newly registered user has an empty card list in `REMOTE`; screens that need a card should show add-card/choose-card state rather than assuming a default card exists.
- Card linking collects bank, card type, card number, holder name, expiry `MM/YYYY`, and CVV/CVC.
- Transaction history must be sorted newest first by backend timestamp when available. If the frontend model lacks timestamp, add a DTO/mapping layer rather than trusting arbitrary response order.

### Contract And OTP

Purpose: view legal contract and sign via OTP.

- Contract content comes from backend.
- OTP purpose should describe action, e.g. contract signing.
- Backend generates a real OTP for contract signing. Frontend auto-fills the OTP from API/FCM and sends it in the final sign request after the user taps confirm.
- Successful disbursement should create a user notification and FCM payload that can navigate to loan management.
- Success screen returns user to Home or loan management.

### Chatbot

Purpose: EasyMoney assistance with text/card/action responses.

Actions can navigate route, dial phone, or open URL if supported by frontend action model.

## UI Conventions

- Prefer Material3 `MaterialTheme.colorScheme`.
- Keep bottom bars/CTAs from hiding scrollable content.
- Avoid hard-coded brand/company copy in Kotlin when string resources exist.
- Guide/help XML files live in `res/layout/guide_*.xml`; routes with help enabled need a valid guide XML.
