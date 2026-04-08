package com.example.easymoney.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TealSecondary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary
import kotlin.math.abs

private data class NotificationItem(
    val id: String,
    val description: String,
    val transactionCode: String,
    val amount: Long,
    val balance: Long,
    val dateTime: String
)

private data class NotificationGroup(
    val monthLabel: String,
    val items: List<NotificationItem>
)

private val mockBalanceChanges = listOf(
    NotificationGroup(
        monthLabel = "Tháng 04/2026",
        items = listOf(
            NotificationItem("n1", "GD thanh toán điện tử", "2604750502914339", -14181L, 132376L, "01/04/2026 07:31"),
            NotificationItem("n2", "GD thanh toán điện tử", "2604750502680030", -50000L, 82376L, "01/04/2026 06:31"),
            NotificationItem("n3", "Thanh toán EASY MONEY", "843397835046260475", 1500L, 83876L, "01/04/2026 06:30"),
            NotificationItem("n4", "GD thanh toán điện tử", "2604750597686372", -19868L, 153405L, "31/03/2026 13:44"),
            NotificationItem("n5", "GD thanh toán điện tử", "2604750695990866", -23803L, 173273L, "31/03/2026 13:44"),
        )
    ),
    NotificationGroup(
        monthLabel = "Tháng 03/2026",
        items = listOf(
            NotificationItem("n6", "Giải ngân khoản vay", "2604750295880123", 5000000L, 212076L, "30/03/2026 14:00"),
            NotificationItem("n7", "Phí dịch vụ tháng 3", "2604750295880124", -12000L, 207076L, "30/03/2026 10:30"),
        )
    )
)

private val mockPromotions = listOf(
    NotificationGroup(
        monthLabel = "Tháng 04/2026",
        items = listOf(
            NotificationItem("p1", "Ưu đãi lãi suất 0% tháng đầu tiên", "PROMO2604001", 0L, 0L, "01/04/2026 09:00"),
            NotificationItem("p2", "Nhận ngay 500 điểm thưởng khi đăng ký vay", "PROMO2604002", 0L, 0L, "28/03/2026 10:00"),
        )
    )
)

private val mockReminders = listOf(
    NotificationGroup(
        monthLabel = "Tháng 04/2026",
        items = listOf(
            NotificationItem("r1", "Nhắc hạn thanh toán khoản vay", "DUE2604001", 0L, 0L, "05/04/2026 08:00"),
            NotificationItem("r2", "Cập nhật thông tin tài khoản", "REMIND001", 0L, 0L, "02/04/2026 09:00"),
        )
    )
)

@Composable
fun NotificationScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Biến động số dư", "Khuyến mại", "Nhắc nhở")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        NotificationSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = TealPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == index) TealPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        val filteredData = remember(selectedTab, searchQuery) {
            val currentData = when (selectedTab) {
                0 -> mockBalanceChanges
                1 -> mockPromotions
                else -> mockReminders
            }
            if (searchQuery.isBlank()) {
                currentData
            } else {
                currentData.map { group ->
                    group.copy(items = group.items.filter {
                        it.description.contains(searchQuery, ignoreCase = true) ||
                                it.transactionCode.contains(searchQuery, ignoreCase = true)
                    })
                }.filter { it.items.isNotEmpty() }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            filteredData.forEach { group ->
                item(key = "header_${group.monthLabel}_$selectedTab") {
                    MonthHeader(label = group.monthLabel)
                }
                items(items = group.items, key = { it.id }) { item ->
                    when (selectedTab) {
                        0 -> BalanceChangeItem(item = item)
                        else -> GenericNotificationItem(
                            item = item,
                            icon = if (selectedTab == 1) Icons.Default.Campaign else Icons.Default.NotificationsActive
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF2F4F7))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Nhập thông tin tìm kiếm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun MonthHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun BalanceChangeItem(item: NotificationItem) {
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
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
                if (item.balance > 0) {
                    Text(
                        text = "Số dư: ${formatAmount(item.balance)}đ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.dateTime.takeLast(5),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun GenericNotificationItem(item: NotificationItem, icon: ImageVector) {
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TealSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.dateTime,
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
private fun NotificationScreenPreview() {
    EasyMoneyTheme {
        NotificationScreen()
    }
}
