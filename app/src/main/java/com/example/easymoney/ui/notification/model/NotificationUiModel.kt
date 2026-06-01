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
    val category: String? = null,
    val timeFormatted: String,
    val isRead: Boolean,
    val iconBackground: Color,
    val amount: Double? = null,
    val balanceAfter: Double? = null,
    val transactionCode: String? = null
)

fun NotificationEntity.toUiModel(): NotificationUiModel {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val date = Date(this.timestamp)

    val bgColor = when (this.type) {
        "transaction" -> Color(0xFF4CAF50)
        "promotion" -> Color(0xFFFF9800)
        "reminder" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    return NotificationUiModel(
        id = this.id,
        title = this.title,
        content = this.content ?: "",
        type = this.type,
        category = this.category,
        timeFormatted = sdf.format(date),
        isRead = this.isRead,
        iconBackground = bgColor,
        amount = this.amount,
        balanceAfter = this.balanceAfter,
        transactionCode = this.transactionCode
    )
}
