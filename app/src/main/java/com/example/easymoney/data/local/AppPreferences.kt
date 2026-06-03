package com.example.easymoney.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class DataSourceMode {
    MOCK, REMOTE
}

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("easy_money_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DATA_SOURCE_MODE = "data_source_mode"
        private const val KEY_API_BASE_URL = "api_base_url"
        private const val DEFAULT_BASE_URL = "https://easymoney.lamgd.dev/"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"
        private const val KEY_APP_NOTIFICATIONS_ENABLED = "app_notifications_enabled"
    }

    private val _dataSourceModeFlow = MutableStateFlow(readDataSourceMode())
    val dataSourceModeFlow: StateFlow<DataSourceMode> = _dataSourceModeFlow.asStateFlow()

    private fun readDataSourceMode(): DataSourceMode =
        DataSourceMode.valueOf(
            prefs.getString(KEY_DATA_SOURCE_MODE, DataSourceMode.MOCK.name) ?: DataSourceMode.MOCK.name
        )

    var dataSourceMode: DataSourceMode
        get() = readDataSourceMode()
        set(value) {
            prefs.edit().putString(KEY_DATA_SOURCE_MODE, value.name).apply()
            _dataSourceModeFlow.value = value
        }

    var apiBaseUrl: String
        get() = prefs.getString(KEY_API_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_API_BASE_URL, value).apply()

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
            // Workflow #83 — user id đổi theo token → phát lại để UI rebind đúng tài khoản.
            _currentUserIdFlow.value = readCurrentUserId()
        }

    /**
     * Workflow #54 — derive current user id from the mock access token
     * (`mock_access_token_{id}` → `user_{id}`). Returns a stable fallback
     * `user_anonymous` when there is no token, so local cache rows still
     * have a non-null userId column (Room schema requirement).
     */
    val currentUserId: String
        get() = readCurrentUserId()

    private fun readCurrentUserId(): String {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return "user_anonymous"
        val prefix = "mock_access_token_"
        return if (token.startsWith(prefix)) "user_" + token.removePrefix(prefix) else "user_anonymous"
    }

    // Workflow #83 — observable user id; notification flows flatMapLatest theo nó để
    // không giữ cache của tài khoản trước sau login/register/đổi tài khoản.
    private val _currentUserIdFlow = MutableStateFlow(readCurrentUserId())
    val currentUserIdFlow: StateFlow<String> = _currentUserIdFlow.asStateFlow()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    // Workflow #34 — Biometric 2FA optional toggle.
    var isBiometric2FAEnabled: Boolean
        get() = prefs.getBoolean("biometric_2fa_enabled", false)
        set(value) = prefs.edit().putBoolean("biometric_2fa_enabled", value).apply()

    var darkThemeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME_ENABLED, value).apply()

    var appNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_APP_NOTIFICATIONS_ENABLED, value).apply()

    fun clearAuthData() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
        // Workflow #83 — logout đổi user id về anonymous → phát lại cho UI.
        _currentUserIdFlow.value = readCurrentUserId()
    }
}
