package com.example.easymoney.domain.repository

import com.example.easymoney.data.local.entity.NotificationEntity
import com.example.easymoney.domain.common.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>
    fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>>
    fun getUnreadCountForUser(userId: String): Flow<Int>
    suspend fun addNotificationRaw(notification: NotificationEntity)
    suspend fun markAsRead(id: Long)
    suspend fun markAllAsRead()
    suspend fun clearAll()

    /**
     * Đồng bộ thông báo từ API về máy
     */
    suspend fun refreshNotifications()

    /**
     * Trigger backend gửi FCM test notification về thiết bị (dùng cho sandbox).
     */
    /**
     * Workflow #14 — đăng ký FCM token với backend (mock log nếu chưa có endpoint).
     */
    suspend fun registerFcmToken(token: String): Resource<Unit>

    suspend fun triggerFcmTest(
        token: String,
        delay: Int,
        title: String,
        content: String,
        type: String,
        amount: Long?
    ): Resource<Unit>
}
