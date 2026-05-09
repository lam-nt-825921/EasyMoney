package com.example.easymoney.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.data.local.entity.NotificationEntity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun NotificationScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val tabs = remember { listOf("Biến động số dư", "Khuyến mại", "Nhắc nhở") }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        NotificationSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = scheme.surface,
            contentColor = scheme.primary,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (pagerState.currentPage == index) scheme.primary else scheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val tabTitle = tabs.getOrNull(page) ?: ""
            
            // Optimize Flow collection to avoid creating new flows on every recomposition
            val notificationFlow = remember(tabTitle, viewModel) {
                viewModel.getNotificationsByType(tabTitle)
            }
            val notificationGroups by notificationFlow.collectAsState(initial = emptyList())

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
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                filteredData.forEach { group ->
                    item(key = "header_${group.monthLabel}_$page") {
                        MonthHeader(label = group.monthLabel)
                    }
                    items(
                        items = group.items, 
                        key = { "${it.id}_$page" } // Use unique key to prevent LazyColumn crashes
                    ) { item ->
                        val onItemClick = {
                            when (page) {
                                0 -> onNavigateToHistory()
                                1 -> {
                                    if (!item.targetId.isNullOrBlank()) {
                                        onNavigateToEvent(item.targetId)
                                    }
                                }
                                else -> {}
                            }
                        }
                        
                        when (page) {
                            0 -> BalanceChangeItem(item = item, onClick = onItemClick)
                            else -> GenericNotificationItem(
                                item = item,
                                icon = if (page == 1) Icons.Default.Campaign else Icons.Default.NotificationsActive,
                                onClick = onItemClick
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationSearchBar(query: String, onQueryChange: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(scheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm kiếm thông báo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.6f)
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
    val scheme = MaterialTheme.colorScheme
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = scheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun BalanceChangeItem(item: NotificationEntity, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val amount = item.amount ?: 0L
    val isPositive = amount >= 0
    val amountColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val iconBg = (if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)).copy(alpha = 0.1f)
    
    // Proper way to handle locales in Compose with safety
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        if (!configuration.locales.isEmpty) {
            configuration.locales[0] ?: Locale.getDefault()
        } else {
            Locale.getDefault()
        }
    }
    
    val dateStr = remember(item.timestamp, locale) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
            sdf.format(Date(item.timestamp))
        } catch (e: Exception) {
            "--/--/---- --:--"
        }
    }
    
    val amountText = remember(amount, isPositive, locale) {
        val formatted = formatAmount(amount, locale)
        if (isPositive) "+${formatted}đ" else "-${formatted}đ"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(modifier = Modifier.padding(top = 2.dp)) {
                    item.transactionCode?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = dateStr.takeLast(5),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GenericNotificationItem(item: NotificationEntity, icon: ImageVector, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        if (!configuration.locales.isEmpty) {
            configuration.locales[0] ?: Locale.getDefault()
        } else {
            Locale.getDefault()
        }
    }
    
    val dateStr = remember(item.timestamp, locale) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
            sdf.format(Date(item.timestamp))
        } catch (e: Exception) {
            "--/--/---- --:--"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatAmount(amount: Long, locale: Locale): String {
    return try {
        val formatter = NumberFormat.getInstance(locale)
        formatter.format(abs(amount))
    } catch (e: Exception) {
        abs(amount).toString()
    }
}
