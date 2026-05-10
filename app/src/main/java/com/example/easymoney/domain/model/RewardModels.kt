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
    val history: List<PointHistory>
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
