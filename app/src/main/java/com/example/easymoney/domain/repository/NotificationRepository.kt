package com.example.easymoney.domain.repository

import com.example.easymoney.data.local.entity.NotificationEntity
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
}
