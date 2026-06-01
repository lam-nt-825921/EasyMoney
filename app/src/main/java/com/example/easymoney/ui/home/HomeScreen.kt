package com.example.easymoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.easymoney.R
import com.example.easymoney.ui.home.components.*

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onBannerClick: (String, String) -> Unit,
    onRedeemClick: () -> Unit,
    onVerifyEkycClick: () -> Unit,
    onLoanProductClick: (String) -> Unit,
    onConsultLoanClick: () -> Unit,
    onManageLoanClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = uiState.isLoading

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
                onToggleTheme = onToggleTheme
            )

            if (isLoading) {
                HomeLoadingContent()
            } else {
                uiState.profileCompletion?.let { completion ->
                    if (!completion.canApplyLoan) {
                        EKycAlertSection(
                            message = completion.statusMessage,
                            onVerifyClick = onVerifyEkycClick
                        )
                    }
                } ?: uiState.eKycStatus?.let { status ->
                    if (!status.isIdentified) {
                        EKycAlertSection(message = status.message ?: "", onVerifyClick = onVerifyEkycClick)
                    }
                }

                uiState.profileCompletionErrorMessage?.let { message ->
                    ProfileCompletionRefreshError(message = message)
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

                uiState.recommendedLoan?.let { loan ->
                    MainBanner(
                        title = loan.name,
                        description = loan.description,
                        onRegistrationClick = { onLoanProductClick(loan.id) }
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ProfileCompletionRefreshError(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
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
