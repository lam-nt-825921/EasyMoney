package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.TransactionGroupDto
import com.example.easymoney.data.remote.dto.TransactionItemDto
import com.example.easymoney.data.remote.dto.toSortedDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionHistoryMappingTest {

    @Test
    fun `out-of-order backend data is sorted newest-first at group and item level`() {
        // Group A có item mới nhất (ts=300), group B có item cũ hơn (ts=200).
        // Trong group A, item ts=100 đứng trước item ts=300 → sort phải đảo lại.
        val groups = listOf(
            TransactionGroupDto(
                date = "10/03/2026",
                items = listOf(
                    TransactionItemDto(transactionCode = "A1", amount = 100.0, balance = 100.0, time = "10:00", timestamp = 100L),
                    TransactionItemDto(transactionCode = "A2", amount = 200.0, balance = 200.0, time = "12:00", timestamp = 300L)
                )
            ),
            TransactionGroupDto(
                date = "09/03/2026",
                items = listOf(
                    TransactionItemDto(transactionCode = "B1", amount = 50.0, balance = 50.0, time = "08:00", timestamp = 200L)
                )
            )
        )

        val domain = groups.toSortedDomain()

        // Group A (max ts=300) trước group B (max ts=200).
        assertEquals("10/03/2026", domain[0].date)
        assertEquals("09/03/2026", domain[1].date)
        // Trong group A: ts=300 đứng trước ts=100.
        assertEquals("A2", domain[0].items[0].transactionCode)
        assertEquals("A1", domain[0].items[1].transactionCode)
    }

    @Test
    fun `groups with missing timestamps fall back to zero and end up last`() {
        val groups = listOf(
            TransactionGroupDto(
                date = "no-ts",
                items = listOf(
                    TransactionItemDto(transactionCode = "X", amount = 0.0, balance = 0.0, time = "")
                )
            ),
            TransactionGroupDto(
                date = "has-ts",
                items = listOf(
                    TransactionItemDto(transactionCode = "Y", amount = 0.0, balance = 0.0, time = "", timestamp = 1000L)
                )
            )
        )

        val domain = groups.toSortedDomain()
        assertEquals("has-ts", domain[0].date)
        assertEquals("no-ts", domain[1].date)
    }
}
