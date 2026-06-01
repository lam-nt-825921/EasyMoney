package com.example.easymoney.messaging

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.entity.NotificationEntity
import com.example.easymoney.domain.repository.NotificationRepository
import com.example.easymoney.ui.notification.manager.AppNotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EasyMoneyMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: AppNotificationManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var appPreferences: AppPreferences

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
        scope.launch {
            notificationRepository.registerFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Lấy dữ liệu từ payload "data" (Khuyên dùng loại này để tùy biến cao)
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val title = data["title"] ?: "Easy Money"
            val content = data["content"] ?: ""
            val type = data["type"] ?: "transaction"
            // Workflow #54 — amount can be fractional VND from backend repayment splits.
            val amount = data["amount"]?.toDoubleOrNull()
            val balanceAfter = (data["balanceAfter"] ?: data["balance_after"])?.toDoubleOrNull()
            val category = data["category"]
            val transactionCode = data["transactionCode"] ?: data["transaction_code"]
            val targetId = data["targetId"] ?: data["target_id"]
            val targetType = data["targetType"] ?: data["target_type"]
            val userId = appPreferences.currentUserId

            // 1. Lưu vào Database local ngay lập tức
            scope.launch {
                val entity = NotificationEntity(
                    userId = userId,
                    title = title,
                    content = content,
                    type = type,
                    category = category,
                    amount = amount,
                    balanceAfter = balanceAfter,
                    transactionCode = transactionCode,
                    targetId = targetId,
                    targetType = targetType,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                notificationRepository.addNotificationRaw(entity)
            }

            // 2. Hiển thị thông báo nổi cho người dùng
            notificationManager.showNotification(
                userId = userId,
                title = title,
                content = content,
                type = type,
                amount = amount,
                balanceAfter = balanceAfter,
                transactionCode = transactionCode
            )
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
