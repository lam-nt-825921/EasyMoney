package com.example.easymoney.ui.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.domain.repository.NotificationRepository
import com.example.easymoney.ui.notification.model.NotificationUiModel
import com.example.easymoney.ui.notification.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    // Workflow #83 — flatMapLatest theo user id hiện tại; đổi tài khoản → flow tự rebind,
    // không còn hiển thị cache của user trước.
    val notifications: StateFlow<List<NotificationUiModel>> = appPreferences.currentUserIdFlow
        .flatMapLatest { userId -> notificationRepository.getNotificationsForUser(userId) }
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = appPreferences.currentUserIdFlow
        .flatMapLatest { userId -> notificationRepository.getUnreadCountForUser(userId) }
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
