package com.example.easymoney.data.local.dao

import androidx.room.*
import com.example.easymoney.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCountForUser(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    // Workflow #83 — mutations phải scope theo user để không rò rỉ giữa các tài khoản.
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id AND userId = :userId")
    suspend fun markAsRead(id: Long, userId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    // Workflow #83 — chỉ xoá thông báo của user hiện tại, không xoá toàn bảng.
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearAll(userId: String)

    // Workflow #83 — reconcile cache: thay toàn bộ rows của user bằng kết quả backend mới nhất.
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)

    // Workflow #83 — lấy backend remoteId để sync read-status đúng id khi mark-read.
    @Query("SELECT remoteId FROM notifications WHERE id = :id")
    suspend fun getRemoteId(id: Long): Long?

    @Transaction
    suspend fun replaceForUser(userId: String, rows: List<NotificationEntity>) {
        deleteForUser(userId)
        if (rows.isNotEmpty()) insertNotifications(rows)
    }
}
