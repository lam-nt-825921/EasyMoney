package com.example.easymoney.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationDtoMappingTest {

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @Test
    fun `parse snake_case backend response without SerializedName`() {
        // NotificationDto không khai @SerializedName → fields camelCase Kotlin
        // sẽ map snake_case theo Gson policy.
        val json = """
            {
                "id": 42,
                "title": "Biến động số dư",
                "content": "Tài khoản nhận +5.000.000đ",
                "type": "transaction",
                "amount": 5000000,
                "balance_after": 12345678,
                "transaction_code": "TXN001",
                "timestamp": 1700000000000,
                "is_read": false
            }
        """.trimIndent()

        val dto = gson.fromJson(json, NotificationDto::class.java)

        assertEquals(42, dto.id)
        assertEquals("Biến động số dư", dto.title)
        assertEquals("transaction", dto.type)
        assertEquals(5_000_000L, dto.amount)
        assertEquals(12_345_678L, dto.balanceAfter)
        assertEquals("TXN001", dto.transactionCode)
        assertEquals(1_700_000_000_000L, dto.timestamp)
        assertEquals(false, dto.isRead)
    }

    @Test
    fun `null optional fields parse correctly`() {
        val json = """
            {
                "id": 1,
                "title": "T",
                "content": "C",
                "type": "promotion",
                "timestamp": 1700000000000,
                "is_read": true
            }
        """.trimIndent()

        val dto = gson.fromJson(json, NotificationDto::class.java)

        assertNull(dto.amount)
        assertNull(dto.balanceAfter)
        assertNull(dto.transactionCode)
        assertEquals(true, dto.isRead)
    }
}
