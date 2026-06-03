package com.example.easymoney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Workflow #83 — backend notification id (null cho thông báo tạo từ FCM/local).
    // Tách khỏi PK local để id backend trùng nhau giữa các user không ghi đè cache.
    val remoteId: Long? = null,
    val userId: String,
    val title: String,
    val content: String? = null,
    val type: String,
    // Workflow #54 — backend nhóm thông báo bằng `category`; giữ field này để UI/filter dùng được.
    val category: String? = null,
    // Workflow #54 — money fields phải nhận decimal (backend chia kỳ trả nợ có thể ra float).
    val amount: Double? = null,
    val balanceAfter: Double? = null,
    val transactionCode: String? = null,
    val targetId: String? = null,
    val targetType: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
)
