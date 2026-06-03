package com.example.easymoney.ui.account.profile

import java.util.Calendar

/**
 * Workflow #95 — Các trường có thể có lỗi validate trong luồng chỉnh sửa hồ sơ.
 */
enum class ProfileField {
    FULL_NAME,
    NATIONAL_ID,
    GENDER,
    DATE_OF_BIRTH,
    CONTACT_NAME,
    CONTACT_PHONE,
    INCOME
}

/**
 * Workflow #95 — Loại lỗi validate, độc lập với Android resource để dễ unit test.
 * UI map sang chuỗi hiển thị qua [ProfileValidationError.asMessage].
 */
enum class ProfileValidationError {
    REQUIRED,
    NAME_INVALID,
    NAME_TOO_SHORT,
    PHONE_INVALID,
    NATIONAL_ID_INVALID,
    GENDER_REQUIRED,
    DOB_INVALID,
    DOB_FUTURE,
    DOB_AGE_RANGE,
    INCOME_INVALID
}

/**
 * Workflow #95 — Lớp validate tập trung cho các màn chỉnh sửa hồ sơ.
 *
 * Mọi hàm validate đều tự normalize đầu vào trước khi kiểm tra, nên giá trị thô
 * từ contact picker (có khoảng trắng, dấu gạch, tiền tố +84...) vẫn được đánh giá đúng.
 * Các hàm normalize được tách riêng để màn save dùng lại trước khi gửi lên backend.
 */
object ProfileInputValidator {

    const val GENDER_MALE = "Nam"
    const val GENDER_FEMALE = "Nữ"

    private const val MIN_AGE = 18
    private const val MAX_AGE = 70
    private const val CCCD_LENGTH = 12

    // Cho phép chữ cái Unicode (gồm tiếng Việt có dấu) và khoảng trắng.
    private val NAME_REGEX = Regex("^[\\p{L} ]+$")
    private val VN_MOBILE_REGEX = Regex("^0\\d{9}$")
    private val DOB_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    // ---- Normalization ----

    /** Trim 2 đầu và gộp các khoảng trắng lặp lại thành 1. */
    fun normalizeName(raw: String): String = raw.trim().replace(Regex("\\s+"), " ")

    /** Chỉ giữ lại chữ số. */
    fun normalizeDigits(raw: String): String = raw.filter { it.isDigit() }

    /**
     * Bỏ khoảng trắng, gạch ngang, dấu chấm, ngoặc; chuyển +84/84 về dạng nội địa 0xxxxxxxxx.
     */
    fun normalizePhone(raw: String): String {
        val cleaned = raw.replace(Regex("[\\s\\-.()]"), "")
        return when {
            cleaned.startsWith("+84") -> "0" + cleaned.removePrefix("+84")
            cleaned.startsWith("84") && cleaned.length > 10 -> "0" + cleaned.removePrefix("84")
            else -> cleaned
        }
    }

    // ---- Validation ----

    /** Họ tên / tên người liên hệ: bắt buộc, chỉ chữ + khoảng trắng, tối thiểu 2 ký tự. */
    fun validateName(raw: String): ProfileValidationError? {
        val name = normalizeName(raw)
        return when {
            name.isBlank() -> ProfileValidationError.REQUIRED
            name.replace(" ", "").length < 2 -> ProfileValidationError.NAME_TOO_SHORT
            !NAME_REGEX.matches(name) -> ProfileValidationError.NAME_INVALID
            else -> null
        }
    }

    /** Số điện thoại VN: 0 + 9 chữ số sau khi normalize. */
    fun validatePhone(raw: String): ProfileValidationError? {
        val phone = normalizePhone(raw)
        return when {
            phone.isBlank() -> ProfileValidationError.REQUIRED
            !VN_MOBILE_REGEX.matches(phone) -> ProfileValidationError.PHONE_INVALID
            else -> null
        }
    }

    /** CCCD: đúng 12 chữ số. */
    fun validateNationalId(raw: String): ProfileValidationError? {
        val digits = normalizeDigits(raw)
        return when {
            digits.isBlank() -> ProfileValidationError.REQUIRED
            digits.length != CCCD_LENGTH -> ProfileValidationError.NATIONAL_ID_INVALID
            else -> null
        }
    }

    /** Giới tính: chỉ chấp nhận lựa chọn cố định Nam / Nữ. */
    fun validateGender(raw: String): ProfileValidationError? =
        if (raw == GENDER_MALE || raw == GENDER_FEMALE) null
        else ProfileValidationError.GENDER_REQUIRED

    /**
     * Ngày sinh theo định dạng backend yyyy-MM-dd; không cho ngày tương lai;
     * độ tuổi hợp lệ 18–70.
     */
    fun validateDateOfBirth(raw: String): ProfileValidationError? {
        val value = raw.trim()
        if (value.isBlank()) return ProfileValidationError.REQUIRED
        if (!DOB_REGEX.matches(value)) return ProfileValidationError.DOB_INVALID

        val parts = value.split("-")
        val year = parts[0].toIntOrNull() ?: return ProfileValidationError.DOB_INVALID
        val month = parts[1].toIntOrNull() ?: return ProfileValidationError.DOB_INVALID
        val day = parts[2].toIntOrNull() ?: return ProfileValidationError.DOB_INVALID

        if (month !in 1..12) return ProfileValidationError.DOB_INVALID
        if (day !in 1..daysInMonth(year, month)) return ProfileValidationError.DOB_INVALID

        val today = Calendar.getInstance()
        val dob = Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day)
        }
        if (dob.after(today)) return ProfileValidationError.DOB_FUTURE

        val age = ageInYears(dob, today)
        return if (age < MIN_AGE || age > MAX_AGE) ProfileValidationError.DOB_AGE_RANGE else null
    }

    /** Thu nhập hàng tháng: phải là số dương khi được nhập. */
    fun validateIncome(value: Long): ProfileValidationError? =
        if (value <= 0L) ProfileValidationError.INCOME_INVALID else null

    private fun daysInMonth(year: Int, month: Int): Int =
        Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

    private fun ageInYears(dob: Calendar, today: Calendar): Int {
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
        return age
    }
}
