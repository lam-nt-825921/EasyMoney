package com.example.easymoney.ui.common.error

import androidx.annotation.StringRes
import com.example.easymoney.R

/**
 * Workflow #61 — well-known backend error markers that the frontend must react
 * to specifically (e.g. trigger navigation, show a different copy).
 *
 * Backend still returns plain text in `message` today; we string-match here.
 * When backend adds a structured `code` field (see `documents/API_SPEC.md`
 * "Asks for Backend" #2), switch [detect] to read `code` directly.
 */
enum class BackendErrorCode(val marker: String, @StringRes val resId: Int) {
    CARD_REQUIRED("CARD_REQUIRED", R.string.error_card_required),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", R.string.error_insufficient_balance),
    INVALID_AMOUNT("INVALID_AMOUNT", R.string.error_invalid_amount),
    NETWORK("NETWORK_ERROR", R.string.error_network);

    companion object {
        /**
         * Returns the first known marker found in [rawMessage], or null if the
         * message is plain text without any recognised marker.
         */
        fun detect(rawMessage: String?): BackendErrorCode? {
            if (rawMessage.isNullOrBlank()) return null
            return entries.firstOrNull { rawMessage.contains(it.marker, ignoreCase = true) }
        }
    }
}
