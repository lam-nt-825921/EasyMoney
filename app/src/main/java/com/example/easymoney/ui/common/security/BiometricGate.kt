package com.example.easymoney.ui.common.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.easymoney.ui.common.identity.BiometricModule

/**
 * Workflow #64 — reusable biometric gate.
 *
 * When [pendingAction] flips to non-null, this composable runs the action immediately if
 * [is2FAEnabled] is false, otherwise it shows [BiometricModule] and only invokes the
 * action on a successful biometric result. On cancel/fail it invokes [onCancelled].
 *
 * The pending action is always cleared via [onConsumed] regardless of outcome, so the
 * caller can use a single nullable state holder as the trigger.
 */
@Composable
fun BiometricGate(
    is2FAEnabled: Boolean,
    pendingAction: (() -> Unit)?,
    onConsumed: () -> Unit,
    onCancelled: (errorMessage: String?) -> Unit = {}
) {
    var showPrompt by remember { mutableStateOf(false) }
    var actionRef by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(pendingAction) {
        val action = pendingAction ?: return@LaunchedEffect
        if (!is2FAEnabled) {
            action()
            onConsumed()
        } else {
            actionRef = action
            showPrompt = true
        }
    }

    if (showPrompt) {
        BiometricModule { result ->
            showPrompt = false
            val action = actionRef
            actionRef = null
            if (result.isSuccess) {
                action?.invoke()
            } else {
                onCancelled(result.errorMessage)
            }
            onConsumed()
        }
    }
}
