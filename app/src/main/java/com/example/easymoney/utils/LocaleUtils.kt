package com.example.easymoney.utils

import androidx.appcompat.app.AppCompatDelegate

/** Workflow #30 — Lấy ngôn ngữ app hiện tại để truyền cho API master data. */
fun currentAppLanguage(default: String = "vi"): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    val tag = locales.toLanguageTags()
    return when {
        tag.startsWith("en") -> "en"
        tag.startsWith("vi") -> "vi"
        else -> default
    }
}
