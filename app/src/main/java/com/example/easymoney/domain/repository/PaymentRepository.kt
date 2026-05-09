package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.WalletInfo

interface PaymentRepository {
    suspend fun getPaymentCards(): Resource<List<PaymentCard>>
    suspend fun addPaymentCard(card: PaymentCard): Resource<Unit>
    suspend fun deletePaymentCard(cardId: String): Resource<Unit>
    
    suspend fun getWalletInfo(): Resource<WalletInfo>
    suspend fun topUp(amount: Long, cardId: String): Resource<Unit>
    suspend fun withdraw(amount: Long, cardId: String, biometricToken: String?): Resource<Unit>
    suspend fun toggleAutoDeduction(enabled: Boolean): Resource<Unit>
}
