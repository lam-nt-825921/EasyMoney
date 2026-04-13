package com.example.easymoney.ui.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.repository.NotificationRepository
import com.example.easymoney.ui.notification.model.NotificationUiModel
import com.example.easymoney.ui.notification.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val currentUserId = "user_123" // TODO: Get from Auth session

    val notifications: StateFlow<List<NotificationUiModel>> = notificationRepository.getNotificationsForUser(currentUserId)
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = notificationRepository.getUnreadCountForUser(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationRepository.clearAll()
        }
    }
}
