package com.example.easymoney

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.easymoney.data.local.AppPreferences
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

    @Inject
    lateinit var appPreferences: AppPreferences

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Workflow #94 — lock runtime config (REMOTE, public base URL, light theme) and Vietnamese
        // locale BEFORE any repository/FCM work that reads dataSourceMode or apiBaseUrl.
        appPreferences.enforceProductionDefaults()
        forceVietnameseLocale()
        registerFcmTokenAtStart()
    }

    /** Production: app luôn chạy tiếng Việt, không cho phép đổi ngôn ngữ. */
    private fun forceVietnameseLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(AppPreferences.PRODUCTION_LANGUAGE_TAG))
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
