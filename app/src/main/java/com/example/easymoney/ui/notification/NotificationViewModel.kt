package com.example.easymoney.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.data.local.entity.NotificationEntity
import com.example.easymoney.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class NotificationGroup(
    val monthLabel: String,
    val items: List<NotificationEntity>
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val currentUserId = "user_123"

    init {
        viewModelScope.launch {
            notificationRepository.refreshNotifications()
        }
    }

    fun getNotificationsByType(type: String): Flow<List<NotificationGroup>> {
        val mappedType = when(type) {
            "Biến động số dư" -> "transaction"
            "Khuyến mại" -> "promotion"
            "Nhắc nhở" -> "reminder"
            else -> "transaction"
        }

        return notificationRepository.getNotificationsByType(currentUserId, mappedType)
            .map { list ->
                list.groupBy { entity ->
                    val sdf = SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN"))
                    sdf.format(Date(entity.timestamp))
                }.map { (label, items) ->
                    NotificationGroup(label, items)
                }
            }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }
}
