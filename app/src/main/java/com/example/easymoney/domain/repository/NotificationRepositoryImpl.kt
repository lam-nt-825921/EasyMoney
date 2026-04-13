package com.example.easymoney.domain.repository

import android.app.Application
import com.example.easymoney.data.local.dao.NotificationDao
import com.example.easymoney.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val application: Application
) : NotificationRepository {

    override fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsForUser(userId)
    }

    override fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByType(userId, type)
    }

    override fun getUnreadCountForUser(userId: String): Flow<Int> {
        return notificationDao.getUnreadCountForUser(userId)
    }

    override suspend fun addNotificationRaw(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    override suspend fun markAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun clearAll() {
        notificationDao.clearAll()
    }
}
