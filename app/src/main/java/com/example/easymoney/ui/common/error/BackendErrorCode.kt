package com.example.easymoney.ui.common.error

import androidx.annotation.StringRes
import com.example.easymoney.R

/**
 * Workflow #61 — well-known backend error codes that the frontend reacts to
 * specifically (e.g. trigger navigation, show a different copy).
 *
 * Backend responses are not fully standardised yet, so [detect] accepts all
 * current shapes: `{ "code": "..." }`, `{ "detail": "..." }`, `{ "message":
 * "..." }`, and legacy plain text containing a marker.
 */
enum class BackendErrorCode(val marker: String, @StringRes val resId: Int) {
    CARD_REQUIRED("CARD_REQUIRED", R.string.error_card_required),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", R.string.error_insufficient_balance),
    INVALID_AMOUNT("INVALID_AMOUNT", R.string.error_invalid_amount),
    NETWORK("NETWORK_ERROR", R.string.error_network);

    companion object {
        /** Returns the first known code found in backend JSON or plain text. */
        fun detect(rawMessage: String?): BackendErrorCode? {
            val message = rawMessage?.trim().orEmpty()
            if (message.isBlank()) return null

            val structuredCode = CODE_FIELD_REGEX.find(message)
                ?.groupValues
                ?.getOrNull(1)
            structuredCode?.let { code ->
                entries.firstOrNull { it.marker.equals(code, ignoreCase = true) }?.let { return it }
            }

            return entries.firstOrNull { message.contains(it.marker, ignoreCase = true) }
        }

        private val CODE_FIELD_REGEX = Regex(""""code"\s*:\s*"([A-Z0-9_]+)"""")
    }
}
