package com.example.easymoney.ui.common.identity

import android.net.Uri

/**
 * Result data models for shared identity verification modules.
 * These are used to pass data back to the orchestrating ViewModels.
 */

/**
 * Result from Face Capture (Camera + ML Kit)
 */
data class FaceCaptureResult(
    val imageUri: Uri?,
    val metadata: Map<String, Any> = emptyMap(),
    val livenessVerified: Boolean = false
)

/**
 * Result from NFC Chip Reading (CCCD)
 */
data class NfcResult(
    val rawData: String?,
    val extractedInfo: Map<String, String> = emptyMap(),
    val signature: String? = null,
    val isSuccess: Boolean = false
)

/**
 * Result from System Biometric Authentication
 */
data class BiometricResult(
    val isSuccess: Boolean,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    val cryptoToken: String? = null
)

/**
 * Result from Document Upload (Camera or File Picker)
 */
data class DocumentResult(
    val fileUri: Uri?,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val isFromCamera: Boolean
)

/**
 * Common event interface for Identity Modules
 */
sealed interface IdentityUiEvent {
    data object OnDismiss : IdentityUiEvent
    data class OnError(val message: String) : IdentityUiEvent
}
