package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.EkycStatusDto
import com.example.easymoney.data.remote.dto.toDomain
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EkycStatusDtoMappingTest {

    private val gson = GsonBuilder().create()

    @Test
    fun `parses rich backend response with status, session and match scores`() {
        // Workflow #59 — backend trả `status: VERIFIED` + rich fields.
        val json = """
            {
                "status": "VERIFIED",
                "session_id": "sess_abc123",
                "is_identified": true,
                "missing_documents": [],
                "document_method": "NFC",
                "verified_at": 1700000000,
                "face_match_score": 0.97,
                "document_match_score": 0.91,
                "message": "OK"
            }
        """.trimIndent()

        val domain = gson.fromJson(json, EkycStatusDto::class.java).toDomain()

        assertTrue(domain.isIdentified)
        assertEquals(emptyList<String>(), domain.missingDocuments)
        assertEquals("VERIFIED", domain.status)
        assertEquals("sess_abc123", domain.sessionId)
        assertEquals("NFC", domain.documentMethod)
        assertEquals(1_700_000_000L, domain.verifiedAt)
        assertEquals(0.97, domain.matchScore)
    }

    @Test
    fun `falls back to status=VERIFIED when is_identified omitted`() {
        // Backend chỉ trả status; FE phải derive isIdentified từ đó.
        val json = """{ "status": "VERIFIED" }"""

        val domain = gson.fromJson(json, EkycStatusDto::class.java).toDomain()

        assertTrue(domain.isIdentified)
        assertNull(domain.matchScore)
    }

    @Test
    fun `missing documents preserved when present`() {
        val json = """
            {
                "status": "PENDING",
                "is_identified": false,
                "missing_documents": ["ID_CARD_FRONT", "FACE_VIDEO"]
            }
        """.trimIndent()

        val domain = gson.fromJson(json, EkycStatusDto::class.java).toDomain()

        assertEquals(listOf("ID_CARD_FRONT", "FACE_VIDEO"), domain.missingDocuments)
        assertEquals(false, domain.isIdentified)
        assertEquals("PENDING", domain.status)
    }
}
