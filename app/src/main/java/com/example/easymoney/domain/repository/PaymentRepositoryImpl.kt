package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.PaymentRemoteDataSource
import com.example.easymoney.data.sample.SAMPLE_INITIAL_BALANCE
import com.example.easymoney.data.sample.SAMPLE_PAYMENT_CARDS
import com.example.easymoney.data.sample.sampleWalletInfo
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.AddCardOutcome
import com.example.easymoney.domain.model.AddCardRequest
import com.example.easymoney.domain.model.Bank
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.QrPayment
import com.example.easymoney.domain.model.QrPaymentStatus
import com.example.easymoney.domain.model.WalletInfo
import java.util.UUID
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"

// Workflow #75 — minimal bank list for MOCK mode; REMOTE uses GET /api/v1/payment/banks.
private val MOCK_BANKS = listOf(
    Bank("VCB", "Vietcombank", "VCB", listOf("970436"), null, listOf("DEBIT", "CREDIT")),
    Bank("TCB", "Techcombank", "TCB", listOf("970407"), null, listOf("DEBIT", "CREDIT")),
    Bank("BIDV", "BIDV", "BIDV", listOf("970418"), null, listOf("DEBIT")),
    Bank("MB", "MB Bank", "MB", listOf("970422"), null, listOf("DEBIT", "CREDIT"))
)

class PaymentRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val remoteDataSource: PaymentRemoteDataSource
) : PaymentRepository {

    private var isAutoDeduction = true
    private var balance: Long = SAMPLE_INITIAL_BALANCE
    private val cards: MutableList<PaymentCard> = SAMPLE_PAYMENT_CARDS.toMutableList()

    override fun clearUserScopedState() {
        // Workflow #62 — bỏ MOCK seed của user trước; REMOTE không bị ảnh hưởng.
        isAutoDeduction = true
        balance = SAMPLE_INITIAL_BALANCE
        cards.clear()
        mockQrPayments.clear()
    }

    override suspend fun getPaymentCards(): Resource<List<PaymentCard>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.getPaymentCards mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                Resource.Success(cards.toList(), isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getPaymentCards()
        }
    }

    override suspend fun getBanks(): Resource<List<Bank>> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.getBanks mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(200)
                Resource.Success(MOCK_BANKS, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getBanks()
        }
    }

    override suspend fun addCard(request: AddCardRequest): AddCardOutcome {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.addCard mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                cards.add(
                    PaymentCard(
                        id = "card_${System.currentTimeMillis()}",
                        cardNumber = request.cardNumber,
                        cardType = request.cardType,
                        bankName = request.bankName,
                        bankId = request.bankId,
                        cardHolderName = request.cardHolderName,
                        expiry = request.expiry
                    )
                )
                AddCardOutcome.Success
            }
            // Workflow #75 — REMOTE must not fabricate a card; the backend is the source of truth.
            DataSourceMode.REMOTE -> remoteDataSource.addCard(request)
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
            DataSourceMode.REMOTE -> remoteDataSource.deletePaymentCard(cardId)
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
            DataSourceMode.REMOTE -> remoteDataSource.getWalletInfo()
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
            DataSourceMode.REMOTE -> remoteDataSource.topUp(amount, cardId)
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
            DataSourceMode.REMOTE -> remoteDataSource.withdraw(amount, cardId, biometricToken)
        }
    }

    // Workflow #36 — track QR payments tạo trong MOCK (mô phỏng pending → success).
    private val mockQrPayments: MutableMap<String, QrPayment> = mutableMapOf()

    override suspend fun createQrPayment(amount: Long): Resource<QrPayment> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.createQrPayment mode=$mode amount=$amount")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(400)
                val id = UUID.randomUUID().toString()
                val qr = QrPayment(
                    id = id,
                    qrContent = "easymoney://pay?amount=$amount&txId=$id",
                    amount = amount,
                    status = QrPaymentStatus.PENDING,
                    expiresAt = System.currentTimeMillis() + 5 * 60_000L
                )
                mockQrPayments[id] = qr
                Resource.Success(qr, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.createQrPayment(amount)
        }
    }

    override suspend fun getQrPaymentStatus(qrPaymentId: String): Resource<QrPayment> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "PaymentRepository.getQrPaymentStatus mode=$mode id=$qrPaymentId")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                val qr = mockQrPayments[qrPaymentId]
                    ?: return Resource.Error("Giao dịch QR không tồn tại")
                // Mô phỏng: sau 1 lần poll → SUCCESS.
                val updated = qr.copy(status = QrPaymentStatus.SUCCESS)
                mockQrPayments[qrPaymentId] = updated
                Resource.Success(updated, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getQrPaymentStatus(qrPaymentId)
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
            DataSourceMode.REMOTE -> remoteDataSource.toggleAutoDeduction(enabled)
        }
    }
}
