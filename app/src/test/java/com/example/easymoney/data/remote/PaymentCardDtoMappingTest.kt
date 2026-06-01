package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.PaymentCardDto
import com.example.easymoney.data.remote.dto.toDomain
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentCardDtoMappingTest {

    private val gson = GsonBuilder().create()

    @Test
    fun `parse balance as fractional Double and map to Long VND`() {
        // Workflow #59 — backend trả balance là float (e.g. 14265851.0).
        val json = """
            {
                "id": "card_1",
                "card_number": "**** 1234",
                "card_type": "VISA",
                "bank_name": "VCB",
                "balance": 14265851.0
            }
        """.trimIndent()

        val dto = gson.fromJson(json, PaymentCardDto::class.java)
        val domain = dto.toDomain()

        assertEquals("card_1", domain.id)
        assertEquals("**** 1234", domain.cardNumber)
        assertEquals("VISA", domain.cardType)
        assertEquals("VCB", domain.bankName)
        assertEquals(14_265_851L, domain.balance)
    }

    @Test
    fun `null balance defaults to zero in domain`() {
        val json = """{ "id": "x", "card_number": "n", "card_type": "t", "bank_name": "b" }"""

        val dto = gson.fromJson(json, PaymentCardDto::class.java)
        val domain = dto.toDomain()

        assertEquals(0L, domain.balance)
    }
}
