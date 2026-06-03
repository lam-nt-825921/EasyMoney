package com.example.easymoney.utils

import com.example.easymoney.data.local.AppPreferences

/**
 * Workflow #30 — ngôn ngữ truyền cho API master data.
 * Workflow #94 — production khoá về tiếng Việt: một install cũ từng để `en` không được phép ảnh
 * hưởng tới master-data API. Trả thẳng về [AppPreferences.PRODUCTION_LANGUAGE_TAG].
 */
fun currentAppLanguage(): String = AppPreferences.PRODUCTION_LANGUAGE_TAG
