package com.example.easymoney.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthTokenDtoMappingTest {

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @Test
    fun `parse camelCase backend response thanks to SerializedName`() {
        val json = """
            {
                "accessToken": "abc123",
                "refreshToken": "refresh789",
                "expiresIn": 3600
            }
        """.trimIndent()

        val dto = gson.fromJson(json, AuthTokenDto::class.java)

        assertEquals("abc123", dto.accessToken)
        assertEquals("refresh789", dto.refreshToken)
        assertEquals(3600, dto.expiresIn)
    }

    @Test
    fun `serialize back to camelCase keys`() {
        val dto = AuthTokenDto("token1", "refresh1", 7200)
        val json = gson.toJson(dto)

        assertEquals(true, json.contains("\"accessToken\""))
        assertEquals(true, json.contains("\"refreshToken\""))
        assertEquals(true, json.contains("\"expiresIn\""))
    }
}
