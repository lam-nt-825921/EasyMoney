package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor() : PaymentRepository {
    private var isAutoDeduction = true
    private var balance = 5000000L
    private val cards = mutableListOf(
        PaymentCard("c1", "**** **** **** 1234", "VISA", "Vietcombank"),
        PaymentCard("c2", "**** **** **** 5678", "MASTER", "Techcombank")
    )

    override suspend fun getPaymentCards(): Resource<List<PaymentCard>> {
        delay(300)
        return Resource.Success(cards)
    }

    override suspend fun addPaymentCard(card: PaymentCard): Resource<Unit> {
        delay(500)
        cards.add(card)
        return Resource.Success(Unit)
    }

    override suspend fun deletePaymentCard(cardId: String): Resource<Unit> {
        delay(300)
        cards.removeAll { it.id == cardId }
        return Resource.Success(Unit)
    }

    override suspend fun getWalletInfo(): Resource<WalletInfo> {
        delay(300)
        return Resource.Success(
            WalletInfo(
                availableBalance = balance,
                isAutoDeductionEnabled = isAutoDeduction,
                recentFlows = listOf(
                    BalanceFlow("f1", 1000000, FlowType.IN, System.currentTimeMillis() - 86400000, "Nạp tiền từ thẻ ****1234"),
                    BalanceFlow("f2", 500000, FlowType.OUT, System.currentTimeMillis() - 172800000, "Thanh toán kỳ hạn 05/2026")
                )
            )
        )
    }

    override suspend fun topUp(amount: Long, cardId: String): Resource<Unit> {
        delay(500)
        balance += amount
        return Resource.Success(Unit)
    }

    override suspend fun withdraw(amount: Long, cardId: String, biometricToken: String?): Resource<Unit> {
        delay(800)
        if (amount > balance) return Resource.Error("Số dư không đủ")
        balance -= amount
        return Resource.Success(Unit)
    }

    override suspend fun toggleAutoDeduction(enabled: Boolean): Resource<Unit> {
        delay(200)
        isAutoDeduction = enabled
        return Resource.Success(Unit)
    }
}
