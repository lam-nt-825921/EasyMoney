package com.example.easymoney.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.model.LoanProviderInfoModel
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val initialPackageName: String? = null,
    val productInfo: LoanPackageModel? = null,
    val providerInfo: LoanProviderInfoModel? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {
    private val packageId: String? = savedStateHandle[AppDestination.Onboarding.PACKAGE_ID_ARG]
    private val initialPackageName: String? = savedStateHandle[AppDestination.Onboarding.PACKAGE_NAME_ARG]

    private val _uiState = MutableStateFlow(OnboardingUiState(initialPackageName = initialPackageName))
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadOnboardingData()
    }

    fun loadOnboardingData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val packageResult = if (packageId != null) {
                loanRepository.getLoanPackageById(packageId)
            } else {
                loanRepository.getMyPackage()
            }
            val providerResult = loanRepository.getLoanProviderInfo()

            var loadedPackage: LoanPackageModel? = null
            var loadedProvider: LoanProviderInfoModel? = null
            val errors = mutableListOf<String>()

            when (packageResult) {
                is Resource.Success -> loadedPackage = packageResult.data
                is Resource.Error -> errors.add(packageResult.message)
                is Resource.Loading -> Unit
            }

            when (providerResult) {
                is Resource.Success -> loadedProvider = providerResult.data
                is Resource.Error -> errors.add(providerResult.message)
                is Resource.Loading -> Unit
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    initialPackageName = initialPackageName,
                    productInfo = loadedPackage,
                    providerInfo = loadedProvider,
                    errorMessage = errors.firstOrNull()
                )
            }
        }
    }
}
