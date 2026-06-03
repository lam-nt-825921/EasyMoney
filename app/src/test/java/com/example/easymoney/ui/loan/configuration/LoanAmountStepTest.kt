package com.example.easymoney.ui.loan.configuration

import org.junit.Assert.assertEquals
import org.junit.Test

class LoanAmountStepTest {

    @Test
    fun `stepFor picks 500k for small ranges`() {
        assertEquals(500_000L, LoanAmountStep.stepFor(5_000_000L))
        assertEquals(500_000L, LoanAmountStep.stepFor(3_000_000L))
    }

    @Test
    fun `stepFor picks 1tr for medium ranges`() {
        assertEquals(1_000_000L, LoanAmountStep.stepFor(20_000_000L))
        assertEquals(1_000_000L, LoanAmountStep.stepFor(10_000_000L))
    }

    @Test
    fun `stepFor picks 5tr for large ranges`() {
        assertEquals(5_000_000L, LoanAmountStep.stepFor(50_000_000L))
        assertEquals(5_000_000L, LoanAmountStep.stepFor(100_000_000L))
    }

    @Test
    fun `snap rounds to clean increment relative to minimum`() {
        // range 50tr -> step 5tr, snapped relative to min (10tr)
        assertEquals(15_000_000L, LoanAmountStep.snap(16_200_000L, 10_000_000L, 60_000_000L))
        assertEquals(20_000_000L, LoanAmountStep.snap(18_000_000L, 10_000_000L, 60_000_000L))
    }

    @Test
    fun `snap keeps exact minimum and maximum selectable`() {
        assertEquals(10_000_000L, LoanAmountStep.snap(10_000_000L, 10_000_000L, 60_000_000L))
        assertEquals(60_000_000L, LoanAmountStep.snap(60_000_000L, 10_000_000L, 60_000_000L))
    }

    @Test
    fun `snap clamps values outside the range`() {
        assertEquals(10_000_000L, LoanAmountStep.snap(5_000_000L, 10_000_000L, 60_000_000L))
        assertEquals(60_000_000L, LoanAmountStep.snap(99_000_000L, 10_000_000L, 60_000_000L))
    }

    @Test
    fun `snap returns minimum for degenerate range`() {
        assertEquals(10_000_000L, LoanAmountStep.snap(20_000_000L, 10_000_000L, 10_000_000L))
    }
}
