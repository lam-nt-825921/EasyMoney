package com.example.easymoney.messaging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Workflow #72 — in-memory holder for the most recent contract-signing OTP delivered via FCM.
 *
 * The FCM service may receive a `CONTRACT_SIGN_OTP` data message before the signing screen is
 * visible, so the OTP is stored here keyed by `contractId` with a short expiry. The contract
 * ViewModel observes [latest] and only auto-fills the field when the contract id matches the
 * one being signed. The OTP is never auto-submitted — the user still confirms.
 */
@Singleton
class ContractOtpHolder @Inject constructor() {

    data class PendingOtp(
        val contractId: String,
        val otp: String,
        val expiresAt: Long
    )

    private val _latest = MutableStateFlow<PendingOtp?>(null)
    val latest: StateFlow<PendingOtp?> = _latest.asStateFlow()

    /** Store an OTP for [contractId]. [expiresAtMillis] falls back to a [DEFAULT_TTL_MS] window. */
    fun submit(contractId: String, otp: String, expiresAtMillis: Long?) {
        if (contractId.isBlank() || otp.isBlank()) return
        val expiry = expiresAtMillis?.takeIf { it > 0 } ?: (System.currentTimeMillis() + DEFAULT_TTL_MS)
        _latest.value = PendingOtp(contractId, otp, expiry)
    }

    /** Returns a non-expired OTP for [contractId], or null when none matches. */
    fun otpFor(contractId: String): String? {
        val pending = _latest.value ?: return null
        if (pending.contractId != contractId) return null
        if (System.currentTimeMillis() > pending.expiresAt) return null
        return pending.otp
    }

    /** Clear the stored OTP once consumed by the matching contract. */
    fun consume(contractId: String) {
        if (_latest.value?.contractId == contractId) _latest.value = null
    }

    private companion object {
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L
    }
}
