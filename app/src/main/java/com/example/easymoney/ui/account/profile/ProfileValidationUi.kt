package com.example.easymoney.ui.account.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.easymoney.R

/**
 * Workflow #95 — Map [ProfileValidationError] sang chuỗi hiển thị tiếng Việt.
 * Tách khỏi [ProfileInputValidator] để lớp validate giữ thuần Kotlin, dễ test.
 */
@Composable
fun ProfileValidationError.asMessage(): String = stringResource(
    id = when (this) {
        ProfileValidationError.REQUIRED -> R.string.profile_error_required
        ProfileValidationError.NAME_INVALID -> R.string.profile_error_name_invalid
        ProfileValidationError.NAME_TOO_SHORT -> R.string.profile_error_name_too_short
        ProfileValidationError.PHONE_INVALID -> R.string.profile_error_phone_invalid
        ProfileValidationError.NATIONAL_ID_INVALID -> R.string.profile_error_national_id_invalid
        ProfileValidationError.GENDER_REQUIRED -> R.string.profile_error_gender_required
        ProfileValidationError.DOB_INVALID -> R.string.profile_error_dob_invalid
        ProfileValidationError.DOB_FUTURE -> R.string.profile_error_dob_future
        ProfileValidationError.DOB_AGE_RANGE -> R.string.profile_error_dob_age_range
        ProfileValidationError.INCOME_INVALID -> R.string.profile_error_income_invalid
    }
)
