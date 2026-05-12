package com.example.easymoney.ui.common.identity

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.res.stringResource
import com.example.easymoney.R

/**
 * Shared Module for System Biometric Authentication (Fingerprint/FaceID).
 */
@Composable
fun BiometricModule(
    onResult: (BiometricResult) -> Unit
) {
    val context = LocalContext.current as? FragmentActivity ?: return
    
    val failText = stringResource(R.string.biometric_auth_failed)
    val titleText = stringResource(R.string.security_biometric_auth)
    val subtitleText = stringResource(R.string.biometric_auth_subtitle)
    val cancelText = stringResource(R.string.action_dismiss)

    val biometricPrompt = remember {
        val executor = ContextCompat.getMainExecutor(context)
        BiometricPrompt(context, executor, object : BiometricPrompt.AuthenticationCallback() {
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
                onResult(BiometricResult(false, errorMessage = failText))
            }
        })
    }

    LaunchedEffect(Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titleText)
            .setSubtitle(subtitleText)
            .setNegativeButtonText(cancelText)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
