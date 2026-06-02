package com.example.easymoney

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.easymoney.ui.AppRoot
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.ui.notification.manager.AppNotificationManager
import com.example.easymoney.ui.theme.EasyMoneyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationManager: AppNotificationManager

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // AppNotificationManager sẽ tự khởi tạo channel trong init { ... } khi được inject
        
        enableEdgeToEdge()
        setContent {
            var appNotificationsEnabled by rememberSaveable {
                mutableStateOf(appPreferences.appNotificationsEnabled)
            }

            // Production: luôn dùng light theme — không cho phép chuyển dark mode.
            EasyMoneyTheme(darkTheme = false) {
                AppRoot(
                    appNotificationsEnabled = appNotificationsEnabled,
                    onAppNotificationsChange = { enabled ->
                        appNotificationsEnabled = enabled
                        appPreferences.appNotificationsEnabled = enabled
                    }
                )
            }
        }
    }
}

