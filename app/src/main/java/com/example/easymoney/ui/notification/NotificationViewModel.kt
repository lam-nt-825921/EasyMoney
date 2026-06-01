package com.example.easymoney.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.data.local.AppPreferences
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
    private val notificationRepository: NotificationRepository,
    appPreferences: AppPreferences
) : ViewModel() {

    private val currentUserId: String = appPreferences.currentUserId

    init {
        viewModelScope.launch {
            try {
                notificationRepository.refreshNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getNotificationsByType(type: String): Flow<List<NotificationGroup>> {
        val mappedType = when (type) {
            "transaction", "promotion", "reminder" -> type
            else -> "transaction"
        }

        return notificationRepository.getNotificationsByType(currentUserId, mappedType)
            .map { list ->
                try {
                    list.groupBy { entity ->
                        val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())
                        sdf.format(Date(entity.timestamp))
                    }.map { (label, items) ->
                        NotificationGroup(label, items)
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }
}
