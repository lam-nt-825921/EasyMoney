package com.example.easymoney.ui.common.identity

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

/**
 * Shared Module for System Biometric Authentication (Fingerprint/FaceID).
 */
@Composable
fun BiometricModule(
    onResult: (BiometricResult) -> Unit
) {
    val context = LocalContext.current as? FragmentActivity ?: return
    val executor = Executors.newSingleThreadExecutor()

    val biometricPrompt = BiometricPrompt(context, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onResult(BiometricResult(false, errorCode, errString.toString()))
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            // CryptoObject would be used here for secure transaction signing
            onResult(BiometricResult(true))
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onResult(BiometricResult(false, errorMessage = "Xác thực thất bại"))
        }
    })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Xác thực sinh trắc học")
        .setSubtitle("Vui lòng xác thực để tiếp tục")
        .setNegativeButtonText("Hủy")
        .build()

    // Usage: biometricPrompt.authenticate(promptInfo)
}
