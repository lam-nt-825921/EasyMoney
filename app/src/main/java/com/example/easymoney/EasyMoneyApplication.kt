package com.example.easymoney

import android.app.Application
import android.util.Log
import com.example.easymoney.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class EasyMoneyApplication : Application() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        registerFcmTokenAtStart()
    }

    private fun registerFcmTokenAtStart() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Failed to fetch token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result ?: return@addOnCompleteListener
            appScope.launch {
                notificationRepository.registerFcmToken(token)
            }
        }
    }
}
