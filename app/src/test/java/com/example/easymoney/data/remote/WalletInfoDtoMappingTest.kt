package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.WalletInfoDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.domain.model.FlowType
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class WalletInfoDtoMappingTest {

    private val gson = GsonBuilder().create()

    @Test
    fun `wallet parses fractional balance and maps flows`() {
        val json = """
            {
                "available_balance": 1234567.89,
                "is_auto_deduction_enabled": true,
                "recent_flows": [
                    {
                        "id": "f1",
                        "amount": 500000.0,
                        "type": "IN",
                        "timestamp": 1700000000000,
                        "description": "Nạp tiền"
                    },
                    {
                        "id": "f2",
                        "amount": -123456.5,
                        "type": "OUT",
                        "timestamp": 1700000010000,
                        "description": "Trả nợ"
                    }
                ]
            }
        """.trimIndent()

        val dto = gson.fromJson(json, WalletInfoDto::class.java)
        val domain = dto.toDomain()

        assertEquals(1_234_567L, domain.availableBalance)
        assertEquals(true, domain.isAutoDeductionEnabled)
        assertEquals(2, domain.recentFlows.size)
        // Workflow #74 — recent flows are ordered newest-first by timestamp. f2 (1700000010000)
        // is newer than f1 (1700000000000), so it must come first.
        assertEquals("f2", domain.recentFlows[0].id)
        assertEquals(FlowType.OUT, domain.recentFlows[0].type)
        assertEquals("f1", domain.recentFlows[1].id)
        assertEquals(FlowType.IN, domain.recentFlows[1].type)
        assertEquals(500_000L, domain.recentFlows[1].amount)
    }

    @Test
    fun `recent flows are sorted newest first regardless of backend order`() {
        val json = """
            {
                "available_balance": 1000.0,
                "recent_flows": [
                    { "id": "old", "amount": 100.0, "type": "IN", "timestamp": 1000, "description": "old" },
                    { "id": "new", "amount": 200.0, "type": "IN", "timestamp": 5000, "description": "new" },
                    { "id": "mid", "amount": 150.0, "type": "IN", "timestamp": 3000, "description": "mid" }
                ]
            }
        """.trimIndent()

        val domain = gson.fromJson(json, WalletInfoDto::class.java).toDomain()

        assertEquals(listOf("new", "mid", "old"), domain.recentFlows.map { it.id })
    }
}
