package com.example.easymoney.navigation

import java.util.Locale

/**
 * Workflow #84 — a navigation target carried by a tapped notification.
 *
 * The target is parsed from the system-notification intent in `MainActivity` and must survive
 * process start, foreground/background transitions, and the login gate. When the user is already
 * authenticated, the app routes straight to [toRoute]; when login is required, the target is held
 * until login succeeds and then consumed instead of dropping to Home.
 */
data class PendingNavTarget(
    val targetType: String,
    val targetId: String?
) {
    /**
     * Maps the notification target to an existing in-app route, or `null` when the target type is
     * unknown / its id is missing. Routing is intentionally limited to already-registered routes so
     * a notification tap can never deep-link into a non-existent screen.
     */
    fun toRoute(): String? {
        val type = targetType.trim().uppercase(Locale.US)
        val id = targetId?.takeIf { it.isNotBlank() }
        return when (type) {
            "LOAN_DEBT" -> AppDestination.LoanManagement.createRoute(id)
            "CONTRACT" -> id?.let { AppDestination.Contract.createRoute(it) }
            "TRANSACTION" -> AppDestination.TransactionHistory.route
            "LOAN_PACKAGE" -> id?.let { AppDestination.LoanDetail.createRoute(it) }
            "EVENT" -> id?.let { AppDestination.EventDetail.createRoute(it) }
            else -> null
        }
    }

    companion object {
        /** Builds a target from raw intent extras, returning `null` when no target type is present. */
        fun fromExtras(targetType: String?, targetId: String?): PendingNavTarget? {
            val type = targetType?.trim().orEmpty()
            if (type.isBlank()) return null
            return PendingNavTarget(targetType = type, targetId = targetId?.trim())
        }
    }
}
