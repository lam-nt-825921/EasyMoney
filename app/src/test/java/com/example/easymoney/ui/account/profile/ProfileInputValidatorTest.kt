package com.example.easymoney.ui.account.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ProfileInputValidatorTest {

    // ---- Name ----

    @Test
    fun `full name with digit is rejected`() {
        assertEquals(
            ProfileValidationError.NAME_INVALID,
            ProfileInputValidator.validateName("Nguyen Van A1")
        )
    }

    @Test
    fun `full name with special char is rejected`() {
        assertEquals(
            ProfileValidationError.NAME_INVALID,
            ProfileInputValidator.validateName("Nguyen@A")
        )
    }

    @Test
    fun `single char name is too short`() {
        assertEquals(
            ProfileValidationError.NAME_TOO_SHORT,
            ProfileInputValidator.validateName("A")
        )
    }

    @Test
    fun `blank name is required`() {
        assertEquals(
            ProfileValidationError.REQUIRED,
            ProfileInputValidator.validateName("   ")
        )
    }

    @Test
    fun `valid vietnamese name passes`() {
        assertNull(ProfileInputValidator.validateName("Nguyễn Văn Anh"))
    }

    @Test
    fun `name with repeated spaces is normalized and valid`() {
        assertNull(ProfileInputValidator.validateName("Nguyen   Van   A"))
    }

    // ---- Phone ----

    @Test
    fun `phone with letters is rejected`() {
        assertEquals(
            ProfileValidationError.PHONE_INVALID,
            ProfileInputValidator.validatePhone("090abc1234")
        )
    }

    @Test
    fun `phone with wrong length is rejected`() {
        assertEquals(
            ProfileValidationError.PHONE_INVALID,
            ProfileInputValidator.validatePhone("0901")
        )
    }

    @Test
    fun `valid local phone passes`() {
        assertNull(ProfileInputValidator.validatePhone("0901234567"))
    }

    @Test
    fun `contact picker phone with spaces and dashes is normalized`() {
        assertNull(ProfileInputValidator.validatePhone("090 123 45 67"))
    }

    @Test
    fun `plus84 phone is converted to local and passes`() {
        assertNull(ProfileInputValidator.validatePhone("+84 901 234 567"))
        assertEquals("0901234567", ProfileInputValidator.normalizePhone("+84 901 234 567"))
    }

    // ---- National ID ----

    @Test
    fun `national id with non digits is rejected`() {
        assertEquals(
            ProfileValidationError.NATIONAL_ID_INVALID,
            ProfileInputValidator.validateNationalId("0123abcd9012")
        )
    }

    @Test
    fun `national id with wrong length is rejected`() {
        assertEquals(
            ProfileValidationError.NATIONAL_ID_INVALID,
            ProfileInputValidator.validateNationalId("123456789")
        )
    }

    @Test
    fun `valid 12 digit cccd passes`() {
        assertNull(ProfileInputValidator.validateNationalId("012345678912"))
    }

    @Test
    fun `digits only input removes hardware keyboard letters`() {
        assertEquals(
            "012345678912",
            ProfileInputValidator.digitsOnlyInput("0123abcd45678912", maxLength = 12)
        )
    }

    // ---- Gender ----

    @Test
    fun `typed gender is rejected`() {
        assertEquals(
            ProfileValidationError.GENDER_REQUIRED,
            ProfileInputValidator.validateGender("nam ")
        )
    }

    @Test
    fun `fixed choice gender passes`() {
        assertNull(ProfileInputValidator.validateGender("Nam"))
        assertNull(ProfileInputValidator.validateGender("Nữ"))
    }

    // ---- Date of birth ----

    @Test
    fun `malformed dob is rejected`() {
        assertEquals(
            ProfileValidationError.DOB_INVALID,
            ProfileInputValidator.validateDateOfBirth("12/05/1990")
        )
    }

    @Test
    fun `date of birth input keeps digits and inserts separators`() {
        assertEquals(
            "15-06-1990",
            ProfileInputValidator.dateOfBirthInput("15abc06-1990xyz")
        )
    }

    @Test
    fun `backend date is converted to display date`() {
        assertEquals(
            "15-06-1990",
            ProfileInputValidator.backendDateToDisplay("1990-06-15")
        )
    }

    @Test
    fun `display date is converted to backend date`() {
        assertEquals(
            "1990-06-15",
            ProfileInputValidator.displayDateToBackend("15-06-1990")
        )
    }

    @Test
    fun `future dob is rejected`() {
        val nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1
        assertEquals(
            ProfileValidationError.DOB_FUTURE,
            ProfileInputValidator.validateDateOfBirth("01-01-$nextYear")
        )
    }

    @Test
    fun `too young dob is rejected`() {
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        assertEquals(
            ProfileValidationError.DOB_AGE_RANGE,
            ProfileInputValidator.validateDateOfBirth("01-01-${thisYear - 5}")
        )
    }

    @Test
    fun `realistic adult dob passes`() {
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        assertNull(ProfileInputValidator.validateDateOfBirth("15-06-${thisYear - 30}"))
    }

    // ---- Income ----

    @Test
    fun `zero income is rejected`() {
        assertEquals(
            ProfileValidationError.INCOME_INVALID,
            ProfileInputValidator.validateIncome(0L)
        )
    }

    @Test
    fun `positive income passes`() {
        assertNull(ProfileInputValidator.validateIncome(15_000_000L))
    }
}
