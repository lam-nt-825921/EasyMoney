package com.example.easymoney.messaging

import android.util.Log
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

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
        // TODO: Gửi token này lên backend của bạn để lưu lại
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Lấy dữ liệu từ payload "data" (Khuyên dùng loại này để tùy biến cao)
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val title = data["title"] ?: "Easy Money"
            val content = data["content"] ?: ""
            val type = data["type"] ?: "transaction"
            val amount = data["amount"]?.toLongOrNull()
            val transactionCode = data["transactionCode"]
            val userId = "user_123" // Trong thực tế lấy từ Session/Auth

            // 1. Lưu vào Database local ngay lập tức
            scope.launch {
                val entity = NotificationEntity(
                    userId = userId,
                    title = title,
                    content = content,
                    type = type,
                    amount = amount,
                    transactionCode = transactionCode,
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
                transactionCode = transactionCode
            )
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
