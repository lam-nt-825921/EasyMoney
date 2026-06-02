package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.BalanceFlow
import com.example.easymoney.domain.model.FlowType
import com.example.easymoney.domain.model.WalletInfo
import com.google.gson.annotations.SerializedName

/**
 * Workflow #59 — backend wallet money fields có thể là float (do chia kỳ).
 * Domain model giữ Long, ở đây mapper round Double → Long.
 */
data class WalletInfoDto(
    @SerializedName("available_balance") val availableBalance: Double? = null,
    @SerializedName("is_auto_deduction_enabled") val isAutoDeductionEnabled: Boolean = false,
    @SerializedName("recent_flows") val recentFlows: List<BalanceFlowDto> = emptyList()
)

data class BalanceFlowDto(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0L,
    @SerializedName("description") val description: String? = null
)

fun WalletInfoDto.toDomain(): WalletInfo = WalletInfo(
    availableBalance = (availableBalance ?: 0.0).toLong(),
    isAutoDeductionEnabled = isAutoDeductionEnabled,
    // Workflow #74 — recent transactions must be newest-first. sortedByDescending is stable,
    // so when timestamps are missing (all 0) the backend's own ordering is preserved.
    recentFlows = recentFlows.map { it.toDomain() }.sortedByDescending { it.timestamp }
)

fun BalanceFlowDto.toDomain(): BalanceFlow = BalanceFlow(
    id = id,
    amount = (amount ?: 0.0).toLong(),
    type = if (type.equals("OUT", ignoreCase = true)) FlowType.OUT else FlowType.IN,
    timestamp = timestamp,
    description = description.orEmpty()
)
