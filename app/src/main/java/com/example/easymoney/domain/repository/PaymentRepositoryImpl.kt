package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.sample.SAMPLE_INITIAL_BALANCE
import com.example.easymoney.data.sample.SAMPLE_PAYMENT_CARDS
import com.example.easymoney.data.sample.sampleWalletInfo
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.WalletInfo
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"
private const val REMOTE_NOT_READY = "Endpoint REMOTE chưa sẵn sàng — vui lòng chuyển Sandbox sang MOCK"

class PaymentRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences
) : PaymentRepository {

    private var isAutoDeduction = true
    private var balance: Long = SAMPLE_INITIAL_BALANCE
    private val cards: MutableList<PaymentCard> = SAMPLE_PAYMENT_CARDS.toMutableList()

    override suspend fun getPaymentCards(): Resource<List<PaymentCard>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.getPaymentCards mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                Resource.Success(cards.toList(), isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire GET /payment/cards
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun addPaymentCard(card: PaymentCard): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.addPaymentCard mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                cards.add(card)
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire POST /payment/cards với card verification
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun deletePaymentCard(cardId: String): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.deletePaymentCard mode=$mode cardId=$cardId")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                cards.removeAll { it.id == cardId }
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire DELETE /payment/cards/{id}
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun getWalletInfo(): Resource<WalletInfo> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.getWalletInfo mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                Resource.Success(sampleWalletInfo(balance, isAutoDeduction), isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire GET /payment/wallet
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun topUp(amount: Long, cardId: String): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.topUp mode=$mode amount=$amount")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                balance += amount
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire POST /payment/topup
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun withdraw(amount: Long, cardId: String, biometricToken: String?): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.withdraw mode=$mode amount=$amount")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(800)
                if (amount > balance) {
                    Resource.Error("Số dư không đủ")
                } else {
                    balance -= amount
                    Resource.Success(Unit, isFromMock = true)
                }
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire POST /payment/withdraw
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun toggleAutoDeduction(enabled: Boolean): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.toggleAutoDeduction mode=$mode enabled=$enabled")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(200)
                isAutoDeduction = enabled
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire PATCH /payment/auto-deduction
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }
}
