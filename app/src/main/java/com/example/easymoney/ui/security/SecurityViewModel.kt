package com.example.easymoney.ui.security

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Workflow #70 — biometric 2FA removed from production UX. Security settings now only
 * exposes password change and device-management entries, none of which need ViewModel state.
 */
@HiltViewModel
class SecurityViewModel @Inject constructor() : ViewModel()
