package com.example.easymoney.ui.loan

import java.text.NumberFormat
import java.util.*

fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}

fun formatCompactAmount(amount: Long): String = "${amount / 1_000_000}tr"
