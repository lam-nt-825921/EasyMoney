package com.example.easymoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.ui.home.components.*
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLoanRegistrationClick: (String, Boolean) -> Unit,
    onToggleSandbox: () -> Unit,
    onBannerClick: (String, String) -> Unit,
    onRedeemClick: () -> Unit,
    onVerifyEkycClick: () -> Unit,
    onLoanProductClick: (String) -> Unit,
    onConsultLoanClick: () -> Unit,
    onManageLoanClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val isLoading = uiState.isLoading

    // Handle Eligibility results
    LaunchedEffect(uiState.eligibilityState) {
        when (val state = uiState.eligibilityState) {
            is EligibilityUiState.Success -> {
                onLoanRegistrationClick(state.packageId, state.skipDetail)
                viewModel.resetEligibilityState()
            }
            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            HeaderSection(
                userName = uiState.userName.ifBlank { "Khách hàng" },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onDevClick = onToggleSandbox
            )

            if (isLoading) {
                HomeLoadingContent()
            } else {
                // eKYC Alert if not identified
                uiState.eKycStatus?.let { status ->
                    if (!status.isIdentified) {
                        EKycAlertSection(
                            message = status.message ?: "",
                            onVerifyClick = onVerifyEkycClick
                        )
                    }
                }

                // Banner Carousel — workflow #21: banner LOAN điều hướng thẳng sang LoanDetail,
                // không kiểm tra eligibility tại Home.
                BannerCarousel(
                    banners = uiState.banners,
                    onBannerClick = { banner ->
                        onBannerClick(banner.targetType, banner.targetId ?: "")
                    }
                )

                // Rewards Section
                RewardsSection(
                    points = uiState.rewardPoints,
                    onRedeemClick = onRedeemClick
                )

                // Feature Grid
                GridSection(
                    onManageLoanClick = onManageLoanClick,
                    onSuggestLoanClick = { onLoanProductClick("ALL") }
                )

                // Consult Loan (Chat bot)
                WideBanner(onClick = onConsultLoanClick)

                // Hot Loans Section — workflow #21: click → LoanDetail (eligibility check chỉ tại đó).
                HotLoansSection(
                    loans = uiState.hotLoans,
                    onLoanClick = { product -> onLoanProductClick(product.id) },
                    onSeeAllClick = { onLoanProductClick("ALL") }
                )

                // Bottom Banner — workflow #21: điều hướng sang LoanDetail thay vì skip detail.
                MainBanner(onRegistrationClick = { onLoanProductClick("1") })
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (uiState.eligibilityState is EligibilityUiState.Checking) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    // Eligibility Dialogs
    if (uiState.eligibilityState is EligibilityUiState.MissingInfo) {
        val state = uiState.eligibilityState as EligibilityUiState.MissingInfo
        AlertDialog(
            onDismissRequest = { viewModel.resetEligibilityState() },
            title = { Text(stringResource(id = R.string.dialog_incomplete_profile_title)) },
            text = { Text(state.message) },
            confirmButton = {
                Button(onClick = { 
                    viewModel.resetEligibilityState()
                    onNavigateToProfile() 
                }) {
                    Text(stringResource(id = R.string.dialog_incomplete_profile_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetEligibilityState() }) {
                    Text(stringResource(id = R.string.dialog_button_close))
                }
            }
        )
    }

    if (uiState.eligibilityState is EligibilityUiState.Rejected) {
        val state = uiState.eligibilityState as EligibilityUiState.Rejected
        AlertDialog(
            onDismissRequest = { viewModel.resetEligibilityState() },
            title = { Text(stringResource(id = R.string.dialog_ineligible_title)) },
            text = { Text(state.message) },
            confirmButton = {
                Button(onClick = { viewModel.resetEligibilityState() }) {
                    Text(stringResource(id = R.string.dialog_button_understand))
                }
            }
        )
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onDevClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.home_welcome, userName),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                onClick = onDevClick,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = stringResource(id = R.string.home_developer_mode),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        IconButton(
            onClick = onToggleTheme,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = stringResource(
                    id = if (isDarkTheme) R.string.home_theme_light else R.string.home_theme_dark
                ),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
