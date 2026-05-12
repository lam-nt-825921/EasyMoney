package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.MasterDataItemDto
import com.example.easymoney.data.remote.dto.MasterDataMetadataDto
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MasterDataDtoMappingTest {

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @Test
    fun `MasterDataItemDto parses snake_case parent_id`() {
        val json = """
            { "id": "HCM", "name": "Hồ Chí Minh", "parent_id": "VN" }
        """.trimIndent()

        val dto = gson.fromJson(json, MasterDataItemDto::class.java)

        assertEquals("HCM", dto.id)
        assertEquals("Hồ Chí Minh", dto.name)
        assertEquals("VN", dto.parentId)
    }

    @Test
    fun `MasterDataItemDto allows null parent_id`() {
        val json = """{ "id": "VN", "name": "Vietnam" }"""

        val dto = gson.fromJson(json, MasterDataItemDto::class.java)

        assertEquals("VN", dto.id)
        assertNull(dto.parentId)
    }

    @Test
    fun `MasterDataMetadataDto parses expired_at and master_data correctly`() {
        val json = """
            {
                "version": "1.0",
                "expired_at": "2026-12-31T00:00:00Z",
                "master_data": {
                    "provinces": [{"id": "HN", "name": "Hà Nội"}],
                    "professions": [],
                    "positions": [],
                    "education_levels": [],
                    "marital_statuses": [],
                    "relationships": []
                }
            }
        """.trimIndent()

        val dto = gson.fromJson(json, MasterDataMetadataDto::class.java)

        assertEquals("1.0", dto.version)
        assertEquals("2026-12-31T00:00:00Z", dto.expiredAt)
        assertEquals(1, dto.masterData.provinces.size)
        assertEquals("Hà Nội", dto.masterData.provinces[0].name)
    }
}
