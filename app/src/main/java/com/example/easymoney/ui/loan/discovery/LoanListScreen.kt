package com.example.easymoney.ui.loan.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.easymoney.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun LoanListScreen(
    onBack: () -> Unit,
    onPackageClick: (String) -> Unit,
    viewModel: LoanDiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPackages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Filter Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.loan_list_filter_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(
                        R.string.loan_list_filter_amount,
                        "%,dđ".format(uiState.maxAmount ?: 100_000_000L).replace(',', '.')
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = (uiState.maxAmount ?: 100_000_000L).toFloat(),
                    onValueChange = { viewModel.updateFilters(max = it.toLong()) },
                    valueRange = 0f..100_000_000f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.loan_list_filter_eligible_only), style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.eligibleOnly,
                        onCheckedChange = { viewModel.updateFilters(eligibleOnly = it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Workflow #29 — filter chips: hot / new / promotional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = uiState.hotOnly,
                        onClick = { viewModel.updateFilters(hotOnly = !uiState.hotOnly) },
                        label = { Text(stringResource(R.string.loan_list_filter_hot)) }
                    )
                    FilterChip(
                        selected = uiState.newOnly,
                        onClick = { viewModel.updateFilters(newOnly = !uiState.newOnly) },
                        label = { Text(stringResource(R.string.loan_list_filter_new)) }
                    )
                    FilterChip(
                        selected = uiState.promotionalOnly,
                        onClick = { viewModel.updateFilters(promotionalOnly = !uiState.promotionalOnly) },
                        label = { Text(stringResource(R.string.loan_list_filter_promotional)) }
                    )
                    if (uiState.isAnyFilterActive()) {
                        TextButton(onClick = { viewModel.resetFilters() }) {
                            Text(stringResource(R.string.loan_list_filter_reset))
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "Lỗi tải dữ liệu",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.packages) { loanPackage ->
                        LoanPackageCard(
                            loanPackage = loanPackage,
                            onClick = { onPackageClick(loanPackage.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoanPackageCard(
    loanPackage: LoanPackageModel,
    onClick: () -> Unit
) {
    val borderColor = if (loanPackage.isEligible) TealPrimary else Color.Transparent
    val backgroundColor = if (loanPackage.isEligible) Color.White else Color(0xFFF2F4F7)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (loanPackage.isEligible) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null,
        shadowElevation = if (loanPackage.isEligible) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (loanPackage.id == "1") {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = loanPackage.packageName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                // Eligibility Badge
                Surface(
                    color = if (loanPackage.isEligible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (loanPackage.isEligible) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (loanPackage.isEligible) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (loanPackage.isEligible) "Đủ điều kiện" else "Chưa phù hợp",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (loanPackage.isEligible) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoColumn(
                    label = "Hạn mức lên tới",
                    value = "%,dđ".format(loanPackage.maxAmount).replace(',', '.'),
                    modifier = Modifier.weight(1f)
                )
                InfoColumn(
                    label = "Lãi suất từ",
                    value = "${loanPackage.interest}%/năm",
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (!loanPackage.isEligible && loanPackage.ineligibilityReason != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when(loanPackage.ineligibilityReason) {
                        "MISSING_PROFILE" -> "Cần hoàn thiện hồ sơ"
                        "LOW_CREDIT_SCORE" -> "Điểm tín dụng chưa đủ"
                        else -> "Không đủ điều kiện"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TealPrimary)
    }
}
