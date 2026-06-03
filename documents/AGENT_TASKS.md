# Agent Tasks - Frontend Profile Fixes

Purpose: this is the only active frontend task list. Completed older tasks are intentionally removed.

Read first:

- `documents/CLAUDE.md`
- `documents/PROJECT_STRUCTURE.md`
- `documents/API_SPEC.md`
- `documents/backend_contract.yaml`
- This file

Assume the implementation agent may only have the Android frontend repository. Backend source is not available to the frontend agent. Public demo base URL must be `https://easymoney.lamgd.dev/`.

## Source Issues Covered

| Issue | Main files to inspect | Expected outcome |
|---|---|---|
| Edit profile accepts unrealistic input and fixed fields are typed manually | `EditPersonalInfoScreen.kt`, `EditContactInfoScreen.kt`, `EditJobInfoScreen.kt`, `EditProfileViewModel.kt`, string resources | Profile forms validate like real customer data; invalid values are blocked or shown with clear field errors; fixed-choice fields use selectors/dropdowns |
| Profile screen still exposes avatar update action | `ProfileScreen.kt`, `ProfileCompletionViewModel.kt`, string resources | Avatar update camera button and click-to-pick behavior are removed from profile UI |

## 1. Add Realistic Edit Profile Input Validation And Fixed-Choice Selectors

Observed issue:

- Edit profile currently accepts unrealistic input:
  - full name can contain numbers or special characters;
  - citizen ID / national ID can accept incomplete or malformed values;
  - phone numbers can contain arbitrary text or malformed contact-picker formatting;
  - fixed fields such as gender are manually typed instead of selected.

Current confirmed root cause:

- `EditPersonalInfoScreen.InputField` and `EditContactInfoScreen.InputField` pass `onValueChange` directly to `EditProfileViewModel`.
- `EditProfileViewModel.updatePersonalInfo()` and `updateContactInfo()` store raw strings without normalization or validation.
- `saveProfile()` sends the whole profile immediately and only reports repository errors.
- The personal info save button does not check per-field validity.
- The contact info save button only checks non-blank values.
- `gender` is rendered as a free-text `InputField` in `EditPersonalInfoScreen`.
- Contact picker phone numbers are saved with whatever formatting Android Contacts returns, such as spaces, dashes, or country prefix.

Required fix:

- Add a small, centralized validation layer for edit profile, preferably in `EditProfileViewModel` or a nearby `ProfileInputValidator` helper so all edit screens share the same rules.
- Track field-level errors in `EditProfileUiState`, for example `Map<ProfileField, String>` or explicit properties such as `fullNameError`, `nationalIdError`, `contactPhoneError`.
- Show field errors with Material `TextField`/`OutlinedTextField` support:
  - set `isError = true`;
  - show a short supporting/error text below the field;
  - disable save while required fields in the active screen are invalid.
- Normalize input before storing or before saving:
  - trim leading/trailing whitespace;
  - collapse repeated internal spaces in names;
  - strip spaces, dashes, dots, parentheses from phone and citizen ID fields;
  - convert `+84xxxxxxxxx` phone numbers to local `0xxxxxxxxx` format if the rest is valid.
- Recommended validation rules:
  - Full name/contact name: required when the screen requires it; allow Vietnamese letters with accents and spaces; reject digits and special characters; reject one-character names; collapse repeated spaces.
  - Phone/contact phone: digits only after normalization; Vietnamese mobile number format, usually `0` + 9 digits; reject letters and special characters; contact picker output must be normalized before validation.
  - Citizen ID / national ID: digits only; accept exactly 12 digits for CCCD. If the product must still support old CMND, accept 9 or 12 digits, but document the chosen rule in code comments and UI copy.
  - Date of birth: do not accept arbitrary digits. Either use a date picker or validate `dd/MM/yyyy`; reject future dates and unrealistic ages for loan users. A practical quick rule is age 18 to 70.
  - Monthly income, if touched in `EditJobInfoScreen`: positive number, no negative or text input, and format/parse consistently.
- Replace gender free-text with a fixed-choice selector:
  - options: `Nam`, `Nữ`;
  - use a dropdown, exposed dropdown, modal bottom sheet, or the app's existing selector pattern;
  - do not allow typing arbitrary gender values.
- Review other fixed-choice fields in edit profile and keep them as selectors:
  - relationship already uses `SimpleSelectionBottomSheet`;
  - profession, position, education, and marital status should remain master-data selectors if currently implemented that way.
- Preserve backend DTO/path contracts. This task should not require backend changes.

Implementation guidance:

- Keep validation close to the edit-profile feature; avoid a large app-wide form framework.
- Prefer filtering impossible characters on input only when it does not hide useful errors. For example:
  - names can reject digits immediately or show an error;
  - phone can normalize contact-picker formatting and show an error if still invalid.
- Do not silently drop meaningful invalid input and save a different value without user visibility.
- Existing profile values from backend that are invalid should be displayed but should block save until corrected.
- Add/adjust string resources for Vietnamese error messages. Keep English resources valid if the project still compiles `values-en`.

Acceptance:

- Full name rejects `Nguyen Van A1`, `Nguyen@A`, and similar values with a visible error.
- Contact name follows the same no-number/no-special-character rule.
- Phone rejects letters/special characters and malformed lengths; valid `0xxxxxxxxx` saves.
- Contact picker numbers with spaces/dashes are normalized before validation.
- Citizen ID rejects non-digits and wrong length.
- Gender cannot be typed manually; user must choose `Nam` or `Nữ`.
- Save is disabled or blocked with field errors until the active form is valid.
- Valid realistic data saves successfully through the existing update-profile API.
- Build passes with `.\gradlew.bat build`.

## 2. Remove Profile Avatar Update Button And Picker

Observed issue:

- The profile screen still allows updating avatar through the circular avatar/camera button.
- Product requirement is to remove the avatar update action.

Current confirmed root cause:

- `ProfileScreen` creates an `avatarPicker` with `ActivityResultContracts.GetContent()`.
- `ProfileHeader` receives `onAvatarClick`.
- The avatar `Surface` and the bottom-right camera `Surface` are clickable and call `onAvatarClick`.
- `ProfileCompletionViewModel.updateAvatar()` remains available and is called from the picker result.

Required fix:

- Remove the visible camera/update-avatar button from `ProfileHeader`.
- Remove click handling from the avatar image placeholder; avatar should be display-only.
- Remove `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())` from `ProfileScreen` if no other avatar picker remains.
- Stop calling `viewModel.updateAvatar()` from profile UI.
- Keep displaying the existing `avatarUri` if backend/profile data already has one.
- Do not remove unrelated eKYC camera/document upload flows.
- `ProfileCompletionViewModel.updateAvatar()` may remain unused if removing it causes a larger refactor, but no production UI should call it.

Acceptance:

- Profile header still displays avatar or default person icon.
- No camera icon appears on the avatar.
- Tapping the avatar does not open the image picker.
- No storage/media picker permission flow is triggered from profile avatar.
- Build passes with `.\gradlew.bat build`.

## Verification Checklist

Run from frontend root:

```powershell
.\gradlew.bat build
```

Manual verification:

- In edit profile, try invalid names, phone numbers, citizen IDs, and date of birth values; verify clear errors and no invalid save.
- Verify gender is selected from fixed `Nam`/`Nữ` choices.
- Open profile and verify avatar is display-only with no camera/update action.
