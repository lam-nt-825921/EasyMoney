package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject

class RewardRepositoryImpl @Inject constructor() : RewardRepository {
    override suspend fun getRewardsCatalog(): Resource<UserRewards> {
        delay(500)
        return Resource.Success(
            UserRewards(
                totalPoints = 1250,
                history = listOf(
                    PointHistory("h1", 500, "Hoàn thành eKYC", System.currentTimeMillis() - 86400000 * 2),
                    PointHistory("h2", 200, "Thanh toán đúng hạn", System.currentTimeMillis() - 86400000),
                    PointHistory("h3", 550, "Tham gia sự kiện hè", System.currentTimeMillis())
                )
            )
        )
    }

    override suspend fun redeemReward(itemId: String): Resource<Unit> {
        delay(800)
        return Resource.Success(Unit)
    }
}
