package com.example.easymoney.ui.account.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MIN_PASSWORD_LENGTH = 6

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onOldPasswordChange(value: String) {
        _uiState.update { it.copy(oldPassword = value, validationError = null, submitError = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, validationError = null, submitError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, validationError = null, submitError = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(validationError = validationError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null, validationError = null) }
        viewModelScope.launch {
            when (val result = userRepository.changePassword(state.oldPassword, state.newPassword)) {
                is Resource.Success -> _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                is Resource.Error -> _uiState.update {
                    it.copy(isSubmitting = false, submitError = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun validate(state: ChangePasswordUiState): Int? = when {
        state.oldPassword.isBlank() || state.newPassword.isBlank() || state.confirmPassword.isBlank() ->
            R.string.change_password_error_empty
        state.newPassword.length < MIN_PASSWORD_LENGTH -> R.string.change_password_error_too_short
        state.newPassword == state.oldPassword -> R.string.change_password_error_same
        state.newPassword != state.confirmPassword -> R.string.change_password_error_mismatch
        else -> null
    }
}
