package com.example.easymoney.ui.sandbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.LoanRemoteDataSource
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.ui.notification.manager.AppNotificationManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SandboxUiState(
    val dataSourceMode: DataSourceMode = DataSourceMode.MOCK,
    val apiBaseUrl: String = "",
    val countdown: Int? = null,
    val isTriggering: Boolean = false
)

sealed class SandboxEffect {
    data object CloseApp : SandboxEffect()
}

@HiltViewModel
class SandBoxViewModel @Inject constructor(
    private val notificationManager: AppNotificationManager,
    private val appPreferences: AppPreferences,
    private val remoteDataSource: LoanRemoteDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(SandboxUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SandboxEffect>()
    val effect = _effect.asSharedFlow()

    private val currentUserId = "user_123"

    init {
        _uiState.update { 
            it.copy(
                dataSourceMode = appPreferences.dataSourceMode,
                apiBaseUrl = appPreferences.apiBaseUrl
            )
        }
    }

    fun toggleDataSourceMode(mode: DataSourceMode) {
        appPreferences.dataSourceMode = mode
        _uiState.update { it.copy(dataSourceMode = mode) }
    }

    fun updateApiBaseUrl(url: String) {
        appPreferences.apiBaseUrl = url
        _uiState.update { it.copy(apiBaseUrl = url) }
    }

    fun startFcmTestFlow() {
        if (_uiState.value.isTriggering) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isTriggering = true) }
            
            try {
                // 1. Lấy FCM Token hiện tại của máy
                val token = FirebaseMessaging.getInstance().token.await()
                
                // 2. Gọi API Backend (Yêu cầu gửi sau 5 giây)
                val result = remoteDataSource.triggerFcmTest(
                    token = token,
                    delay = 5,
                    title = "Biến động số dư (Test)",
                    content = "Tài khoản *0123 vừa nhận được +10.000.000đ từ hệ thống.",
                    type = "transaction",
                    amount = 10_000_000L
                )

                if (result is Resource.Success) {
                    // 3. Bắt đầu đếm ngược 3 giây trên UI
                    _uiState.update { it.copy(countdown = 3) }
                    repeat(3) { i ->
                        delay(1000)
                        _uiState.update { it.copy(countdown = 3 - (i + 1)) }
                    }
                    
                    // 4. Phát tín hiệu tắt App
                    _effect.emit(SandboxEffect.CloseApp)
                } else {
                    _uiState.update { it.copy(isTriggering = false, countdown = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isTriggering = false, countdown = null) }
            }
        }
    }

    fun simulateTransactionNotification() {
        notificationManager.showNotification(
            userId = currentUserId,
            title = "GD thanh toán điện tử",
            content = "Tài khoản của bạn vừa nhận được +5.000.000đ từ hệ thống EasyMoney.",
            type = "transaction",
            amount = 5000000L,
            balanceAfter = 5212076L,
            transactionCode = "2604750295880123"
        )
    }

    fun simulateLoanNotification() {
        notificationManager.showNotification(
            userId = currentUserId,
            title = "Nhắc hạn thanh toán khoản vay",
            content = "Khoản vay LH-8899 của bạn sắp đến hạn thanh toán vào ngày 15/04. Vui lòng kiểm tra.",
            type = "reminder",
            transactionCode = "DUE2604001"
        )
    }

    fun simulatePromotionNotification() {
        notificationManager.showNotification(
            userId = currentUserId,
            title = "Ưu đãi lãi suất 0% tháng đầu tiên",
            content = "Nhận ngay ưu đãi lãi suất cực sốc khi đăng ký vay trong hôm nay.",
            type = "promotion",
            transactionCode = "PROMO2604001"
        )
    }
}
