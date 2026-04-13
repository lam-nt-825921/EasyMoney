package com.example.easymoney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val title: String, // Trong giao diện của bạn là description
    val content: String? = null,
    val type: String, // transaction, promotion, reminder
    val amount: Long? = null,
    val balanceAfter: Long? = null,
    val transactionCode: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
)
