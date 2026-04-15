package com.example.easymoney.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
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
        private const val KEY_API_BASE_URL = "https://easymoney.lamgd.dev/"
        private const val DEFAULT_BASE_URL = "https://easymoney.lamgd.dev/" // Default for Emulator to host
    }

    var dataSourceMode: DataSourceMode
        get() = DataSourceMode.valueOf(prefs.getString(KEY_DATA_SOURCE_MODE, DataSourceMode.MOCK.name) ?: DataSourceMode.MOCK.name)
        set(value) = prefs.edit().putString(KEY_DATA_SOURCE_MODE, value.name).apply()

    var apiBaseUrl: String
        get() = prefs.getString(KEY_API_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_API_BASE_URL, value).apply()
}
