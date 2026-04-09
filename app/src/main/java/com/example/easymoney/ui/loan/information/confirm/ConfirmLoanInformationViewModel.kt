package com.example.easymoney.ui.loan.information.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanApplicationRequest
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.ui.loan.information.form.LoanInformationFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmLoanInformationViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmLoanInformationUiState())
    val uiState: StateFlow<ConfirmLoanInformationUiState> = _uiState.asStateFlow()

    fun setFormData(data: LoanInformationFormUiState) {
        _uiState.update { it.copy(formData = data) }
    }

}
