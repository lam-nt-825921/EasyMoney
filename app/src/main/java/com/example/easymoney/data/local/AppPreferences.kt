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
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"
        private const val KEY_APP_NOTIFICATIONS_ENABLED = "app_notifications_enabled"

        // Workflow #94 — forced production runtime config. These win over any stale value an old
        // install may have persisted (e.g. MOCK, a LAN base URL, en locale, dark mode).
        val PRODUCTION_DATA_SOURCE_MODE = DataSourceMode.REMOTE
        const val PRODUCTION_API_BASE_URL = "https://easymoney.lamgd.dev/"
        const val PRODUCTION_LANGUAGE_TAG = "vi"
        const val PRODUCTION_DARK_THEME_ENABLED = false
    }

    // Workflow #94 — always seeded with the forced production mode, never the persisted value.
    private val _dataSourceModeFlow = MutableStateFlow(PRODUCTION_DATA_SOURCE_MODE)
    val dataSourceModeFlow: StateFlow<DataSourceMode> = _dataSourceModeFlow.asStateFlow()

    // Workflow #94 — data source is locked to REMOTE in production; the setter cannot move it off
    // the forced value, so Sandbox/old persisted MOCK can never re-activate.
    var dataSourceMode: DataSourceMode
        get() = PRODUCTION_DATA_SOURCE_MODE
        set(_) {
            prefs.edit().putString(KEY_DATA_SOURCE_MODE, PRODUCTION_DATA_SOURCE_MODE.name).apply()
            _dataSourceModeFlow.value = PRODUCTION_DATA_SOURCE_MODE
        }

    // Workflow #94 — base URL is locked to the public backend; the setter cannot point Retrofit at
    // a stale LAN URL.
    var apiBaseUrl: String
        get() = PRODUCTION_API_BASE_URL
        set(_) = prefs.edit().putString(KEY_API_BASE_URL, PRODUCTION_API_BASE_URL).apply()

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

    // Workflow #94 — theme is locked to light in production regardless of stale dark-mode setting.
    var darkThemeEnabled: Boolean
        get() = PRODUCTION_DARK_THEME_ENABLED
        set(_) = prefs.edit().putBoolean(KEY_DARK_THEME_ENABLED, PRODUCTION_DARK_THEME_ENABLED).apply()

    var appNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_APP_NOTIFICATIONS_ENABLED, value).apply()

    /**
     * Workflow #94 — overwrite only the runtime-config keys with forced production values once at
     * startup so diagnostic screens and any future code observe the same locked config. Auth tokens,
     * user id, profile, and cached data are intentionally untouched.
     */
    fun enforceProductionDefaults() {
        prefs.edit()
            .putString(KEY_DATA_SOURCE_MODE, PRODUCTION_DATA_SOURCE_MODE.name)
            .putString(KEY_API_BASE_URL, PRODUCTION_API_BASE_URL)
            .putBoolean(KEY_DARK_THEME_ENABLED, PRODUCTION_DARK_THEME_ENABLED)
            .apply()
        _dataSourceModeFlow.value = PRODUCTION_DATA_SOURCE_MODE
    }

    fun clearAuthData() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
        // Workflow #83 — logout đổi user id về anonymous → phát lại cho UI.
        _currentUserIdFlow.value = readCurrentUserId()
    }
}
