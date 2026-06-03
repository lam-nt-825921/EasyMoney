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

    // Workflow #54 — userId derived from session token; no more hard-coded "user_123".
    private val currentUserId: String
        get() = appPreferences.currentUserId

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
        // Workflow #83 — mark local scope theo user; sync read-status backend bằng remoteId.
        val userId = currentUserId
        val remoteId = notificationDao.getRemoteId(id)
        notificationDao.markAsRead(id, userId)
        if (appPreferences.dataSourceMode == DataSourceMode.REMOTE && remoteId != null) {
            remoteDataSource.markNotificationRead(remoteId)
        }
    }

    override suspend fun markAllAsRead() {
        // Workflow #83 — chỉ mark thông báo của user hiện tại.
        notificationDao.markAllAsRead(currentUserId)
        if (appPreferences.dataSourceMode == DataSourceMode.REMOTE) {
            remoteDataSource.markAllNotificationsRead()
        }
    }

    override suspend fun clearAll() {
        // Workflow #83 — chỉ xoá thông báo của user hiện tại, không xoá toàn bảng.
        notificationDao.clearAll(currentUserId)
        // Workflow #54 — sync server-side clear in REMOTE mode.
        if (appPreferences.dataSourceMode == DataSourceMode.REMOTE) {
            remoteDataSource.clearAllNotifications()
        }
    }

    override suspend fun registerFcmToken(token: String): Resource<Unit> {
        Log.d("FCM", "registerFcmToken (workflow #46) token=${token.take(20)}... mode=${appPreferences.dataSourceMode}")
        // MOCK: chỉ log. REMOTE: đăng ký token với backend.
        return if (appPreferences.dataSourceMode == DataSourceMode.REMOTE) {
            remoteDataSource.registerFcmToken(token)
        } else {
            Resource.Success(Unit)
        }
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
        
        // Workflow #83 — chụp user id một lần cho lần refresh này; reconcile đúng user đó.
        val userId = currentUserId
        when (val result = remoteDataSource.getNotifications()) {
            is Resource.Success -> {
                val entities = result.data.map { dto ->
                    NotificationEntity(
                        // id = 0 → Room autoGenerate; tách backend id sang remoteId để tránh đụng PK.
                        remoteId = dto.id,
                        userId = userId,
                        title = dto.title ?: "Thông báo",
                        content = dto.content,
                        type = dto.type ?: "transaction",
                        category = dto.category,
                        amount = dto.amount,
                        balanceAfter = dto.balanceAfter,
                        transactionCode = dto.transactionCode,
                        targetId = dto.targetId,
                        targetType = dto.targetType,
                        timestamp = if (dto.timestamp > 0) dto.timestamp else System.currentTimeMillis(),
                        isRead = dto.isRead
                    )
                }
                // Workflow #83 — thay TOÀN BỘ cache của user bằng kết quả backend. Backend trả `[]`
                // cho user mới → cache của user đó thành rỗng (không còn rò rỉ thông báo user khác).
                try {
                    notificationDao.replaceForUser(userId, entities)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> { /* Handle Error */ }
        }
    }
}
