package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.UserProfileDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.data.remote.dto.toDto
import com.example.easymoney.domain.model.ProfileVerificationStatus
import com.example.easymoney.domain.model.UserProfile
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

/** Workflow #52 — mapping test cho UserProfileDto (workflow #44). */
class UserProfileDtoMappingTest {

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @Test
    fun `parse snake_case profile and map to domain`() {
        val json = """
            {
                "avatar_uri": "http://x/a.png",
                "personal_info": { "full_name": "Nguyen Van A", "national_id": "012345678" },
                "job_info": { "job_title": "Engineer", "monthly_income": 25000000 },
                "verification_status": "VERIFIED",
                "identity_status": { "is_nfc_verified": true }
            }
        """.trimIndent()

        val dto = gson.fromJson(json, UserProfileDto::class.java)
        val domain = dto.toDomain()

        assertEquals("Nguyen Van A", domain.personalInfo.fullName)
        assertEquals("012345678", domain.personalInfo.nationalId)
        assertEquals(25_000_000L, domain.jobInfo.monthlyIncome)
        assertEquals(ProfileVerificationStatus.VERIFIED, domain.verificationStatus)
        // OR logic: NFC verified → identity document verified
        assertEquals(true, domain.identityStatus.isIdentityDocumentVerified)
    }

    @Test
    fun `unknown verification status falls back to INCOMPLETE`() {
        val dto = gson.fromJson("""{ "verification_status": "BOGUS" }""", UserProfileDto::class.java)
        assertEquals(ProfileVerificationStatus.INCOMPLETE, dto.toDomain().verificationStatus)
    }

    @Test
    fun `null fields map to safe defaults`() {
        val dto = gson.fromJson("{}", UserProfileDto::class.java)
        val domain = dto.toDomain()

        assertEquals("", domain.personalInfo.fullName)
        assertEquals(0L, domain.jobInfo.monthlyIncome)
        assertEquals(ProfileVerificationStatus.INCOMPLETE, domain.verificationStatus)
        assertEquals(false, domain.identityStatus.isIdentityDocumentVerified)
    }

    @Test
    fun `domain to dto round trips key fields`() {
        val profile = UserProfile().copy(
            education = "University"
        )
        val dto = profile.toDto()
        assertEquals("University", dto.education)
        assertEquals(ProfileVerificationStatus.INCOMPLETE.name, dto.verificationStatus)
    }
}
