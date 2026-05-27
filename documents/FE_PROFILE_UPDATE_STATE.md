# Current State of Frontend Profile Update Implementation

*Date: May 28, 2026*

This document summarizes the current architecture, data flow, and UI implementation for the User Profile Update feature within the EasyMoney Android application.

## 1. Architecture Overview
The profile update flow strictly adheres to the Clean Architecture principles utilized throughout the project:
*   **UI Layer:** Composable screens (`EditPersonalInfoScreen`, `EditJobInfoScreen`, `EditContactInfoScreen`) using Jetpack Compose.
*   **Presentation Layer:** `EditProfileViewModel` handles UI state, user intent, and master data management, decoupled via Hilt.
*   **Domain Layer:** `UserProfile` and `UserRepository` define the contract for profile operations.
*   **Data Layer:** `UserRepositoryImpl` routes requests to `UserRemoteDataSource` (when in REMOTE mode), which maps Domain models to DTOs (`UserProfileDto`) and executes network calls via `UserApiService` using Retrofit.

## 2. UI & UX Improvements
The profile editing screens have been refactored to align with the visual standards of the `LoanInformationFormScreen`:
*   **Standardized Components:** Utilization of unified `InputField`, `SelectorItem`, and `FormCard` components to ensure a consistent look and feel across the app.
*   **Smart Job Fields:** In the `EditJobInfoScreen`, the "Company Name" (`companyName`) and "Position" (`position`) fields are dynamically displayed *only* if the selected profession is "Nhân viên văn phòng công ty" (ID `p1`).
*   **Income Formatting:** The `monthlyIncome` field utilizes a `ThousandsSeparatorTransformation` for visual formatting (e.g., `100.000.000`) while stripping non-digit characters before saving to the ViewModel, preventing parsing crashes (e.g., `NumberFormatException`) and ensuring exact values (e.g., `100000000`) are sent to the backend.
*   **Contact Integration:** The `EditContactInfoScreen` includes a "Danh bạ" (Contacts) button that requests permissions and opens the native contact picker, automatically populating the `contactName` and `phoneNumber` fields.

## 3. Master Data Integration
*   The `EditProfileViewModel` fetches necessary master data (Professions, Positions, Education Levels, Marital Statuses, Relationships) from the backend during initialization via `loanRepository.getMasterDataMetadata()`.
*   Selection bottom sheets present these dynamically loaded items rather than hardcoded arrays.
*   The ViewModel now explicitly tracks the selected `MasterDataItem` objects (e.g., `selectedProfession`), enabling robust conditional UI logic that doesn't rely solely on string matching.

## 4. API & Data Mapping (Synchronized with Backend)
The data mapping layers have been cleaned up and synchronized with the latest backend improvements:
*   **Clean DTOs:** The `UserProfileDto.kt` file maps the internal Kotlin `camelCase` structure to the backend's expected `snake_case` JSON structure without the need for flat-field workarounds.
*   **Nested Payloads:** The `updateProfile` function sends a deeply nested JSON structure (`personal_info`, `job_info`, `contact_info`, `address_info`), taking full advantage of the backend's ability to parse and update specific nested attributes.
*   **No Address Duplication:** The `extractDetail` workaround in the frontend has been removed. The backend now safely handles the `permanent_address` string without causing a feedback loop of duplicated province names (e.g., "Hà Nội, Hà Nội").
*   **Endpoint:** The save operation executes a `PATCH /api/v1/user/profile` request.

## 5. End-to-End Save Flow
1.  **User Action:** User clicks "Lưu thay đổi" (Save Changes).
2.  **Focus Clearing:** `focusManager.clearFocus()` ensures any pending `onValueChange` actions (especially for numbers) are committed.
3.  **ViewModel Execution:** `viewModel.saveProfile()` sets the `isLoading` state to true.
4.  **Network Call:** The repository sends the patched `UserProfileDto` to the backend.
5.  **Completion Refresh:** Upon a successful `Resource.Success` response, the ViewModel immediately triggers `userRepository.getProfileCompletion(forceRefresh = true)` to fetch the updated profile completion percentage and status.
6.  **Navigation:** The UI observes the `isSuccess` state and pops the back stack (`onBack()`), returning the user to the Profile overview screen seamlessly.