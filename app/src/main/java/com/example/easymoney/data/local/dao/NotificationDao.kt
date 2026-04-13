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

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
