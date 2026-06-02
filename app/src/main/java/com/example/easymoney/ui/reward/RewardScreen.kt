package com.example.easymoney.ui.reward

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.domain.model.UserRewardVoucher
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RewardScreen(
    onBack: () -> Unit,
    viewModel: RewardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Points Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(TealPrimary, Color(0xFF0D5C6E))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.reward_points_current_label),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${uiState.totalPoints}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.common_points_unit),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.reward_tab_redeem)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.reward_tab_redeemed)) }
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedTab == 0) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.rewards) { reward ->
                    RewardCard(
                        reward = reward,
                        canRedeem = uiState.totalPoints >= reward.points,
                        onRedeem = { viewModel.onRedeemRequest(reward.id) }
                    )
                }
            }
        } else {
            if (uiState.redeemedRewards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.reward_empty_redeemed), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.redeemedRewards) { voucher ->
                        RedeemedRewardCard(voucher = voucher)
                    }
                }
            }
        }

        uiState.pendingConfirmId?.let { id ->
            val item = uiState.rewards.firstOrNull { it.id == id }
            if (item != null) {
                AlertDialog(
                    onDismissRequest = viewModel::onCancelRedeem,
                    title = { Text(stringResource(R.string.reward_confirm_title)) },
                    text = {
                        Column {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(
                                    R.string.reward_confirm_message,
                                    item.points,
                                    uiState.totalPoints - item.points
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = viewModel::onConfirmRedeem, enabled = !uiState.isRedeeming) {
                            Text(stringResource(R.string.action_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::onCancelRedeem) {
                            Text(stringResource(R.string.action_dismiss))
                        }
                    }
                )
            }
        }

        uiState.redeemSuccessMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = viewModel::consumeMessages,
                title = { Text(stringResource(R.string.reward_success_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(msg.asString())
                        uiState.latestRedeemedVoucher?.let { voucher ->
                            VoucherArtifactContent(voucher = voucher)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::consumeMessages) { Text(stringResource(R.string.action_ok)) }
                }
            )
        }

        uiState.errorMessage?.let { msg ->
            LaunchedEffect(msg) { /* could show snackbar */ }
            AlertDialog(
                onDismissRequest = viewModel::consumeMessages,
                title = { Text(stringResource(R.string.reward_notice_title)) },
                text = { Text(msg.asString()) },
                confirmButton = {
                    TextButton(onClick = viewModel::consumeMessages) { Text(stringResource(R.string.action_ok)) }
                }
            )
        }
    }
}

@Composable
private fun RedeemedRewardCard(voucher: UserRewardVoucher) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(voucher.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        redeemedSubtitle(voucher),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text(voucher.status) }
                )
            }
            VoucherArtifactContent(voucher = voucher)
            Text(
                text = stringResource(R.string.reward_redeemed_date, formatDate(voucher.issuedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoucherArtifactContent(voucher: UserRewardVoucher) {
    val isCodeReward = !voucher.code.isNullOrBlank() || !voucher.serial.isNullOrBlank()
    if (isCodeReward) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            voucher.faceValue?.let {
                ArtifactRow(stringResource(R.string.reward_artifact_face_value), formatMoney(it))
            }
            voucher.code?.let {
                ArtifactRow(stringResource(R.string.reward_artifact_code), it)
            }
            voucher.serial?.let {
                ArtifactRow(stringResource(R.string.reward_artifact_serial), it)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            voucher.benefitType?.let {
                ArtifactRow(stringResource(R.string.reward_artifact_benefit), benefitLabel(voucher))
            }
            voucher.expiresAt?.let {
                ArtifactRow(stringResource(R.string.reward_artifact_expires), formatDate(it))
            }
            voucher.usedApplicationId?.let {
                ArtifactRow(stringResource(R.string.reward_artifact_used_application), it)
            }
        }
    }
}

@Composable
private fun ArtifactRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun redeemedSubtitle(voucher: UserRewardVoucher): String = when {
    voucher.code != null -> stringResource(R.string.reward_subtitle_code)
    voucher.benefitType != null -> benefitLabel(voucher)
    else -> voucher.type
}

@Composable
private fun benefitLabel(voucher: UserRewardVoucher): String = when (voucher.benefitType) {
    "INTEREST_RATE_DISCOUNT" -> stringResource(
        R.string.reward_benefit_interest_discount,
        (voucher.benefitValue ?: 0.0).toString()
    )
    "INSURANCE_FEE_WAIVER" -> stringResource(R.string.reward_benefit_insurance_waiver)
    "EARLY_REPAYMENT_FEE_WAIVER" -> stringResource(R.string.reward_benefit_early_repayment_waiver)
    "PHONE_CARD" -> stringResource(R.string.reward_benefit_phone_card)
    "SHOPPING_VOUCHER" -> stringResource(R.string.reward_benefit_shopping_voucher)
    else -> voucher.benefitType.orEmpty()
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

@Composable
private fun formatMoney(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount) +
        stringResource(R.string.reward_money_suffix)

@Composable
fun RewardCard(
    reward: RewardItem,
    canRedeem: Boolean,
    onRedeem: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = reward.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reward.points} ${stringResource(R.string.common_points_unit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onRedeem,
                enabled = canRedeem,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(text = stringResource(R.string.reward_btn_redeem), fontSize = 12.sp)
            }
        }
    }
}

