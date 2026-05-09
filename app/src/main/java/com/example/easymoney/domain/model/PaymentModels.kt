package com.example.easymoney.domain.model

data class PaymentCard(
    val id: String,
    val cardNumber: String, // Masked
    val cardType: String, // VISA, MASTER, NAPAS
    val bankName: String
)

enum class FlowType {
    IN, OUT
}

data class BalanceFlow(
    val id: String,
    val amount: Long,
    val type: FlowType,
    val timestamp: Long,
    val description: String
)

data class WalletInfo(
    val availableBalance: Long,
    val isAutoDeductionEnabled: Boolean,
    val recentFlows: List<BalanceFlow>
)
