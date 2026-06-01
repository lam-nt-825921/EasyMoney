package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.NotificationDto
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
    fun `parse snake_case backend response with fractional money`() {
        // Workflow #54 — backend trả về amount/balance_after dưới dạng float
        // (e.g. -983333.3333333334 do chia kỳ trả nợ). DTO phải parse Double.
        val json = """
            {
                "id": 42,
                "title": "Biến động số dư",
                "content": "Tài khoản nhận +5.000.000đ",
                "type": "transaction",
                "category": "repayment",
                "amount": -983333.3333333334,
                "balance_after": 12345678.5,
                "transaction_code": "TXN001",
                "target_id": "contract_1",
                "target_type": "CONTRACT",
                "timestamp": 1700000000000,
                "is_read": false
            }
        """.trimIndent()

        val dto = gson.fromJson(json, NotificationDto::class.java)

        assertEquals(42L, dto.id)
        assertEquals("Biến động số dư", dto.title)
        assertEquals("transaction", dto.type)
        assertEquals("repayment", dto.category)
        assertEquals(-983333.3333333334, dto.amount)
        assertEquals(12_345_678.5, dto.balanceAfter)
        assertEquals("TXN001", dto.transactionCode)
        assertEquals("contract_1", dto.targetId)
        assertEquals("CONTRACT", dto.targetType)
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
        assertNull(dto.category)
        assertEquals(true, dto.isRead)
    }
}
