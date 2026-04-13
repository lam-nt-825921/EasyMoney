package com.example.easymoney.ui.notification.model

import androidx.compose.ui.graphics.Color
import com.example.easymoney.data.local.entity.NotificationEntity
import java.text.SimpleDateFormat
import java.util.*

data class NotificationUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val type: String,
    val timeFormatted: String,
    val isRead: Boolean,
    val iconBackground: Color,
    val amount: Long? = null,
    val balanceAfter: Long? = null,
    val transactionCode: String? = null
)

fun NotificationEntity.toUiModel(): NotificationUiModel {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val date = Date(this.timestamp)
    
    val bgColor = when(this.type) {
        "transaction" -> Color(0xFF4CAF50) // Green
        "promotion" -> Color(0xFFFF9800) // Orange
        "reminder" -> Color(0xFFF44336) // Red
        else -> Color(0xFF9E9E9E) // Grey
    }

    return NotificationUiModel(
        id = this.id,
        title = this.title,
        content = this.content ?: "",
        type = this.type,
        timeFormatted = sdf.format(date),
        isRead = this.isRead,
        iconBackground = bgColor,
        amount = this.amount,
        balanceAfter = this.balanceAfter,
        transactionCode = this.transactionCode
    )
}
