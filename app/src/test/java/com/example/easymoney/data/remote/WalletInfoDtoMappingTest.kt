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
        assertEquals(FlowType.IN, domain.recentFlows[0].type)
        assertEquals(500_000L, domain.recentFlows[0].amount)
        assertEquals(FlowType.OUT, domain.recentFlows[1].type)
    }
}
