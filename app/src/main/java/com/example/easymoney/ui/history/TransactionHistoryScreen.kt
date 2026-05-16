package com.example.easymoney.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.easymoney.R
import com.example.easymoney.domain.model.TransactionItem
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun TransactionHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.errorMessage != null -> Text(
                text = state.errorMessage.orEmpty(),
                modifier = Modifier.align(Alignment.Center),
                color = TextSecondary
            )
            state.groups.isEmpty() -> Text(
                text = stringResource(R.string.history_balance_label, "0"),
                modifier = Modifier.align(Alignment.Center),
                color = TextSecondary
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                state.groups.forEach { group ->
                    item(key = "header_${group.date}") {
                        TransactionDateHeader(date = group.date)
                    }
                    items(items = group.items, key = { it.transactionCode }) { item ->
                        TransactionItemRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionDateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun TransactionItemRow(item: TransactionItem) {
    val isPositive = item.amount >= 0
    val amountColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val iconBg = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val amountText = if (isPositive) "+${formatAmount(item.amount)}đ" else "-${formatAmount(item.amount)}đ"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.transactionCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.history_balance_label, formatAmount(item.balance)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun formatAmount(amount: Long): String =
    "%,d".format(abs(amount)).replace(',', '.')
