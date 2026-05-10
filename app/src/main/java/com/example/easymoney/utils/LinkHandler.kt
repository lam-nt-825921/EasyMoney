package com.example.easymoney.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Workflow #15 — helper xử lý click vào số điện thoại / link.
 */
object LinkHandler {

    /** Mở dialer với số điện thoại đã cho. */
    fun dial(context: Context, phone: String) {
        val sanitized = phone.filter { it.isDigit() || it == '+' || it == '-' || it == ' ' }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$sanitized")
        }
        runCatching { context.startActivity(intent) }
            .onFailure { android.util.Log.w("LinkHandler", "Cannot open dialer", it) }
    }

    /** Mở URL trong browser. */
    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching { context.startActivity(intent) }
            .onFailure { android.util.Log.w("LinkHandler", "Cannot open URL $url", it) }
    }
}
