package com.example.easymoney.ui.loan.configuration

import kotlin.math.roundToLong

/**
 * Workflow #91 — clean VND step policy for the loan amount slider.
 *
 * Steps are derived from the package range so large packages do not produce hundreds of tiny raw
 * stops and uneven min/max ranges still snap to product-friendly increments.
 */
object LoanAmountStep {

    private const val ONE_MILLION = 1_000_000L
    private const val STEP_SMALL = 500_000L      // range <= 5tr
    private const val STEP_MEDIUM = 1_000_000L   // range <= 20tr
    private const val STEP_LARGE = 5_000_000L    // range > 20tr

    /** Clean increment for a package whose selectable range is [range] VND. */
    fun stepFor(range: Long): Long = when {
        range <= 5 * ONE_MILLION -> STEP_SMALL
        range <= 20 * ONE_MILLION -> STEP_MEDIUM
        else -> STEP_LARGE
    }

    /**
     * Snap [raw] to the nearest clean increment relative to [minAmount] (not absolute zero),
     * clamped to `[minAmount, maxAmount]`. Exact min/max remain selectable.
     */
    fun snap(raw: Long, minAmount: Long, maxAmount: Long): Long {
        if (maxAmount <= minAmount) return minAmount
        val step = stepFor(maxAmount - minAmount)
        val steps = ((raw - minAmount).toDouble() / step).roundToLong()
        val snapped = minAmount + steps * step
        return snapped.coerceIn(minAmount, maxAmount)
    }
}
