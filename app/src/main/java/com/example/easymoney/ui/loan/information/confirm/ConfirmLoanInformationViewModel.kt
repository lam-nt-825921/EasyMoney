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

    fun onConfirmClick(onSuccess: () -> Unit) {
        val data = _uiState.value.formData
        val request = LoanApplicationRequest(
            loanAmount = 10_000_000L, // Mock từ Step 1
            tenorMonth = 6,           // Mock từ Step 1
            hasInsurance = true,
            permanentProvince = data.permanentProvince?.name ?: "",
            permanentDistrict = data.permanentDistrict?.name ?: "",
            permanentWard = data.permanentWard?.name ?: "",
            permanentDetail = data.permanentDetail,
            currentProvince = if (data.isCurrentSameAsPermanent) data.permanentProvince?.name ?: "" else data.currentProvince?.name ?: "",
            currentDistrict = if (data.isCurrentSameAsPermanent) data.permanentDistrict?.name ?: "" else data.currentDistrict?.name ?: "",
            currentWard = if (data.isCurrentSameAsPermanent) data.permanentWard?.name ?: "" else data.currentWard?.name ?: "",
            currentDetail = if (data.isCurrentSameAsPermanent) data.permanentDetail else data.currentDetail,
            monthlyIncome = data.monthlyIncome.toLongOrNull() ?: 0L,
            profession = data.profession?.name ?: "",
            position = data.position?.name ?: "",
            education = data.education?.name ?: "",
            maritalStatus = data.maritalStatus?.name ?: "",
            contactName = data.contactName,
            contactRelationship = data.contactRelationship?.name ?: "",
            contactPhone = data.contactPhone
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = loanRepository.submitApplication(request)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                }
                else -> {}
            }
        }
    }
}
