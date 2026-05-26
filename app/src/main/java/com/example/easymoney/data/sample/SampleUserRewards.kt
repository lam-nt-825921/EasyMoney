package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.PointHistory
import com.example.easymoney.domain.model.UserRewards

fun sampleUserRewards(now: Long = System.currentTimeMillis()): UserRewards = UserRewards(
    totalPoints = 1250,
    history = listOf(
        PointHistory("h1", 500, "Hoàn thành eKYC", now - 86_400_000L * 2),
        PointHistory("h2", 200, "Thanh toán đúng hạn", now - 86_400_000L),
        PointHistory("h3", 550, "Tham gia sự kiện hè", now)
    )
)
