package com.example.easymoney.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoginRequest
import com.example.easymoney.domain.model.RegisterRequest
import com.example.easymoney.domain.model.RememberedAccount
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.utils.UiText
import com.example.easymoney.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val loginSuccess: Boolean = false,
    val lastAccount: RememberedAccount? = null,
    val rememberedAccounts: List<RememberedAccount> = emptyList()
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loadRememberedAccounts()
    }

    fun loadRememberedAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val lastAccountRes = repository.getLastRememberedAccount()
            val allAccountsRes = repository.getRememberedAccounts()

            val lastAccount = if (lastAccountRes is Resource.Success) lastAccountRes.data else null
            val allAccounts = if (allAccountsRes is Resource.Success) allAccountsRes.data else emptyList()

            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    lastAccount = lastAccount, 
                    rememberedAccounts = allAccounts
                ) 
            }
        }
    }

    fun login(phone: String, password: String, remember: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.login(LoginRequest(phone, password))
            
            if (result is Resource.Success) {
                if (remember) {
                    // Trong thực tế, fullName nên lấy từ API login trả về hoặc profile
                    // Ở đây mock tạm phone là name nếu chưa có
                    repository.saveRememberedAccount(RememberedAccount(phone, fullName = "User $phone"))
                }
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = UiText.DynamicString(result.message)) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = UiText.StringResource(R.string.login_error_unknown)) }
            }
        }
    }

    fun register(phone: String, fullName: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.register(RegisterRequest(phone, fullName, password))
            
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = UiText.DynamicString(result.message)) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = UiText.StringResource(R.string.login_error_unknown)) }
            }
        }
    }

    fun deleteAccount(phone: String) {
        viewModelScope.launch {
            repository.deleteRememberedAccount(phone)
            loadRememberedAccounts()
        }
    }

    fun selectAccount(account: RememberedAccount) {
        // Cập nhật lastLoginTimestamp để account này nhảy lên đầu
        viewModelScope.launch {
            repository.saveRememberedAccount(account.copy(lastLoginTimestamp = System.currentTimeMillis()))
            loadRememberedAccounts()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(lastAccount = null, rememberedAccounts = emptyList()) }
            loadRememberedAccounts()
        }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetLoginState() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
