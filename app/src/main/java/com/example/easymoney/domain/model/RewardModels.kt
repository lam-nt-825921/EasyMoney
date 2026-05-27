package com.example.easymoney.domain.model

enum class RewardType {
    FINANCIAL, PRACTICAL
}

data class RewardItem(
    val id: String,
    val name: String,
    val type: RewardType,
    val pointsCost: Int,
    val description: String
)

data class PointHistory(
    val id: String,
    val points: Int,
    val reason: String,
    val timestamp: Long
)

data class UserRewards(
    val totalPoints: Int,
    val history: List<PointHistory>,
    val vouchers: List<UserRewardVoucher> = emptyList()
)

data class UserRewardVoucher(
    val id: String,
    val rewardId: String,
    val title: String,
    val type: String,
    val status: String,
    val code: String? = null,
    val serial: String? = null,
    val faceValue: Long? = null,
    val benefitType: String? = null,
    val benefitValue: Double? = null,
    val minAmount: Long? = null,
    val maxAmount: Long? = null,
    val issuedAt: Long,
    val expiresAt: Long? = null,
    val usedAt: Long? = null,
    val usedApplicationId: String? = null
)

data class RedeemRewardResult(
    val totalPoints: Int,
    val voucher: UserRewardVoucher
)

/** Reward item hiển thị trong catalog (workflow #7) — khác RewardItem (catalog backend cũ). */
data class RewardCatalogItem(
    val id: String,
    val title: String,
    val points: Int,
    val description: String,
    val category: String,
    val imageUrl: String? = null
)
