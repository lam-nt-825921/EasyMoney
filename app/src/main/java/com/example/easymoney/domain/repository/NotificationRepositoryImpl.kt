package com.example.easymoney.domain.repository

import android.app.Application
import android.util.Log
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
        // TODO(workflow_14): gọi API mark-read khi backend hỗ trợ — hiện chỉ update local DB
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun clearAll() {
        notificationDao.clearAll()
    }

    override suspend fun registerFcmToken(token: String): Resource<Unit> {
        Log.d("FCM", "registerFcmToken (workflow #14) token=${token.take(20)}... mode=${appPreferences.dataSourceMode}")
        // TODO(workflow_14): khi backend có endpoint, gọi remoteDataSource.registerToken(token)
        return Resource.Success(Unit)
    }

    override suspend fun triggerFcmTest(
        token: String,
        delay: Int,
        title: String,
        content: String,
        type: String,
        amount: Long?
    ): Resource<Unit> = remoteDataSource.triggerFcmTest(token, delay, title, content, type, amount)

    override suspend fun refreshNotifications() {
        val mode = appPreferences.dataSourceMode
        Log.d("DataSource", "NotificationRepository mode=$mode")
        if (mode == DataSourceMode.MOCK) return
        
        when (val result = remoteDataSource.getNotifications()) {
            is Resource.Success -> {
                val entities = result.data.map { dto ->
                    NotificationEntity(
                        id = dto.id.toLong(),
                        userId = currentUserId,
                        title = dto.title ?: "Thông báo",
                        content = dto.content,
                        type = dto.type ?: "transaction",
                        amount = dto.amount,
                        balanceAfter = dto.balanceAfter,
                        transactionCode = dto.transactionCode,
                        timestamp = if (dto.timestamp > 0) dto.timestamp else System.currentTimeMillis(),
                        isRead = dto.isRead
                    )
                }
                // Sync to Local Database with safety
                try {
                    notificationDao.insertNotifications(entities)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> { /* Handle Error */ }
        }
    }
}
