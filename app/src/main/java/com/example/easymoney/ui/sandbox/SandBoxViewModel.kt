package com.example.easymoney.ui.sandbox

import androidx.lifecycle.ViewModel
import com.example.easymoney.ui.notification.manager.AppNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SandBoxViewModel @Inject constructor(
    private val notificationManager: AppNotificationManager
) : ViewModel() {

    private val currentUserId = "user_123"

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
