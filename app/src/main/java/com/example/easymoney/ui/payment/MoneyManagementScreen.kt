package com.example.easymoney.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun MoneyManagementScreen(
    onBack: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val wallet = uiState.walletInfo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Balance Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(TealPrimary, Color(0xFF0D5C6E))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Số dư khả dụng",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "%,d VNĐ".format(wallet?.availableBalance ?: 0L).replace(',', '.'),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        icon = Icons.Default.VerticalAlignTop,
                        label = "Nạp tiền",
                        onClick = onNavigateToTopUp
                    )
                    ActionButton(
                        icon = Icons.Default.VerticalAlignBottom,
                        label = "Rút tiền",
                        onClick = onNavigateToWithdraw
                    )
                }
            }
        }

        // Transactions List
        Text(
            text = "Giao dịch gần đây",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(wallet?.recentFlows ?: emptyList()) { flow ->
                TransactionItem(flow)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TransactionItem(flow: com.example.easymoney.domain.model.BalanceFlow) {
    val isIncome = flow.type == com.example.easymoney.domain.model.FlowType.IN
    val amountText = (if (isIncome) "+" else "-") + "%,dđ".format(abs(flow.amount)).replace(',', '.')
    val color = if (isIncome) Color(0xFF2E7D32) else TextPrimary
    
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateText = sdf.format(Date(flow.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isIncome) Color(0xFFE8F5E9) else Color(0xFFF2F4F7),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Default.Add else Icons.Default.History,
                contentDescription = null,
                tint = if (isIncome) Color(0xFF2E7D32) else TextSecondary,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = flow.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
