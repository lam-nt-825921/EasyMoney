package com.example.easymoney

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.easymoney.ui.AppRoot
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.UserRepository
import com.example.easymoney.navigation.PendingNavTarget
import com.example.easymoney.ui.notification.manager.AppNotificationManager
import com.example.easymoney.ui.theme.EasyMoneyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationManager: AppNotificationManager

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var userRepository: UserRepository

    // Workflow #84 — pending notification target carried from the launching/new intent. Survives
    // cold start (read from onCreate intent) and live taps (onNewIntent), and is consumed by the
    // nav host once routed.
    private val pendingNavTarget = mutableStateOf<PendingNavTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppNotificationManager sẽ tự khởi tạo channel trong init { ... } khi được inject

        pendingNavTarget.value = parseNavTarget(intent)

        enableEdgeToEdge()
        setContent {
            var appNotificationsEnabled by rememberSaveable {
                mutableStateOf(appPreferences.appNotificationsEnabled)
            }
            val scope = rememberCoroutineScope()
            val navTarget by pendingNavTarget

            // Production: luôn dùng light theme — không cho phép chuyển dark mode.
            EasyMoneyTheme(darkTheme = false) {
                AppRoot(
                    appNotificationsEnabled = appNotificationsEnabled,
                    onAppNotificationsChange = { enabled ->
                        val previous = appNotificationsEnabled
                        appNotificationsEnabled = enabled
                        appPreferences.appNotificationsEnabled = enabled
                        scope.launch {
                            when (userRepository.updateNotificationSettings(enabled)) {
                                is Resource.Success -> Unit
                                is Resource.Error -> {
                                    appNotificationsEnabled = previous
                                    appPreferences.appNotificationsEnabled = previous
                                }
                                Resource.Loading -> Unit
                            }
                        }
                    },
                    pendingNavTarget = navTarget,
                    onPendingNavTargetConsumed = { pendingNavTarget.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // App đang sống và được mở lại từ thông báo: cập nhật target, giữ nguyên session.
        setIntent(intent)
        parseNavTarget(intent)?.let { pendingNavTarget.value = it }
    }

    private fun parseNavTarget(intent: Intent?): PendingNavTarget? {
        intent ?: return null
        return PendingNavTarget.fromExtras(
            targetType = intent.getStringExtra(AppNotificationManager.EXTRA_TARGET_TYPE),
            targetId = intent.getStringExtra(AppNotificationManager.EXTRA_TARGET_ID)
        )
    }
}

