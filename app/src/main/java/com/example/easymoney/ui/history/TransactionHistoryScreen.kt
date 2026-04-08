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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary
import kotlin.math.abs

private data class TransactionItem(
    val description: String,
    val transactionCode: String,
    val amount: Long,
    val balance: Long,
    val time: String
)

private data class TransactionGroup(
    val date: String,
    val items: List<TransactionItem>
)

private val mockTransactions = listOf(
    TransactionGroup(
        date = "01/04/2026",
        items = listOf(
            TransactionItem("Nhận tiền từ ngân hàng", "2604750502176432", 41652L, 132376L, "07:45"),
            TransactionItem("GD thanh toán điện tử", "2604750502914339", -14181L, 90724L, "07:31"),
            TransactionItem("GD thanh toán điện tử", "2604750502680030", -50000L, 104905L, "06:31"),
            TransactionItem("Thanh toán EASY MONEY", "843397835046260475", 1500L, 154905L, "06:30"),
        )
    ),
    TransactionGroup(
        date = "31/03/2026",
        items = listOf(
            TransactionItem("GD thanh toán điện tử", "2604750597686372", -19868L, 153405L, "13:44"),
            TransactionItem("GD thanh toán điện tử", "2604750695990866", -23803L, 173273L, "13:44"),
            TransactionItem("Nhận tiền chuyển khoản", "2604750695990977", 15000L, 197076L, "09:12"),
        )
    ),
    TransactionGroup(
        date = "30/03/2026",
        items = listOf(
            TransactionItem("Giải ngân khoản vay", "2604750295880123", 5000000L, 212076L, "14:00"),
            TransactionItem("Phí dịch vụ tháng 3", "2604750295880124", -12000L, 207076L, "10:30"),
        )
    )
)

@Composable
fun TransactionHistoryScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        mockTransactions.forEach { group ->
            item(key = "header_${group.date}") {
                TransactionDateHeader(date = group.date)
            }
            items(items = group.items, key = { it.transactionCode }) { item ->
                TransactionItemRow(item = item)
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
                    text = "Số dư: ${formatAmount(item.balance)}đ",
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

@Preview(showBackground = true)
@Composable
private fun TransactionHistoryScreenPreview() {
    EasyMoneyTheme {
        TransactionHistoryScreen()
    }
}
