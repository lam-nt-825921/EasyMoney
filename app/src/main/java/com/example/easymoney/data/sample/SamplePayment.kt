package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.BalanceFlow
import com.example.easymoney.domain.model.FlowType
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.WalletInfo

val SAMPLE_PAYMENT_CARDS: List<PaymentCard> = listOf(
    PaymentCard("c1", "**** **** **** 1234", "VISA", "Vietcombank"),
    PaymentCard("c2", "**** **** **** 5678", "MASTER", "Techcombank")
)

const val SAMPLE_INITIAL_BALANCE: Long = 5_000_000L

fun sampleRecentFlows(now: Long = System.currentTimeMillis()): List<BalanceFlow> = listOf(
    BalanceFlow("f1", 1_000_000, FlowType.IN, now - 86_400_000L, "Nạp tiền từ thẻ ****1234"),
    BalanceFlow("f2", 500_000, FlowType.OUT, now - 86_400_000L * 2, "Thanh toán kỳ hạn 05/2026")
)

fun sampleWalletInfo(balance: Long, isAutoDeduction: Boolean): WalletInfo = WalletInfo(
    availableBalance = balance,
    isAutoDeductionEnabled = isAutoDeduction,
    recentFlows = sampleRecentFlows()
)
