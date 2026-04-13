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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.data.local.entity.NotificationEntity
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TealSecondary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Biến động số dư", "Khuyến mại", "Nhắc nhở")

    val notificationGroups by viewModel.getNotificationsByType(tabs[selectedTab])
        .collectAsState(initial = emptyList())

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

        val filteredData = remember(notificationGroups, searchQuery) {
            if (searchQuery.isBlank()) {
                notificationGroups
            } else {
                notificationGroups.map { group ->
                    group.copy(items = group.items.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.transactionCode?.contains(searchQuery, ignoreCase = true) == true
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
private fun BalanceChangeItem(item: NotificationEntity) {
    val amount = item.amount ?: 0L
    val isPositive = amount >= 0
    val amountColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val iconBg = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val amountText = if (isPositive) "+${formatAmount(amount)}đ" else "-${formatAmount(amount)}đ"
    
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(item.timestamp))

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
                    text = item.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                item.transactionCode?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                item.balanceAfter?.let {
                    Text(
                        text = "Số dư: ${formatAmount(it)}đ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateStr.takeLast(5),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun GenericNotificationItem(item: NotificationEntity, icon: ImageVector) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(item.timestamp))

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
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateStr,
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
