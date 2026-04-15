package com.example.easymoney.domain.repository

import android.app.Application
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.local.dao.NotificationDao
import com.example.easymoney.data.local.entity.NotificationEntity
import com.example.easymoney.data.remote.LoanRemoteDataSource
import com.example.easymoney.domain.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val remoteDataSource: LoanRemoteDataSource,
    private val appPreferences: AppPreferences,
    private val application: Application
) : NotificationRepository {

    private val currentUserId = "user_123"

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
        // TODO: Gọi API mark read nếu cần
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun clearAll() {
        notificationDao.clearAll()
    }

    override suspend fun refreshNotifications() {
        if (appPreferences.dataSourceMode == DataSourceMode.MOCK) return
        
        when (val result = remoteDataSource.getNotifications()) {
            is Resource.Success -> {
                val entities = result.data.map { dto ->
                    NotificationEntity(
                        id = dto.id.toLong(),
                        userId = currentUserId,
                        title = dto.title,
                        content = dto.content,
                        type = dto.type,
                        amount = dto.amount,
                        balanceAfter = dto.balanceAfter,
                        transactionCode = dto.transactionCode,
                        timestamp = dto.timestamp,
                        isRead = dto.isRead
                    )
                }
                // Sync to Local Database
                notificationDao.insertNotifications(entities)
            }
            else -> { /* Handle Error */ }
        }
    }
}
