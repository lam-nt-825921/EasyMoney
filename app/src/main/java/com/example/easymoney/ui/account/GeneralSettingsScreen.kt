package com.example.easymoney.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary

@Composable
fun GeneralSettingsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsGroup(title = "Ứng dụng") {
            SettingsItem(
                title = "Ngôn ngữ",
                subtitle = "Tiếng Việt",
                icon = Icons.Default.Language
            )
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF2F4F7))
            SettingsItem(
                title = "Chế độ tối",
                subtitle = "Tắt",
                icon = Icons.Default.DarkMode,
                control = {
                    Switch(checked = false, onCheckedChange = {})
                }
            )
        }

        SettingsGroup(title = "Thông báo") {
            SettingsItem(
                title = "Thông báo ứng dụng",
                subtitle = "Nhận tin tức và ưu đãi",
                icon = Icons.Default.Notifications,
                control = {
                    Switch(checked = true, onCheckedChange = {})
                }
            )
        }

        SettingsGroup(title = "Thông tin") {
            SettingsItem(
                title = "Điều khoản sử dụng",
                icon = Icons.Default.Description
            )
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF2F4F7))
            SettingsItem(
                title = "Phiên bản ứng dụng",
                subtitle = "v1.0.26 (Build 20260509)",
                icon = Icons.Default.Info
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    control: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        control?.invoke() ?: if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        } else {
            // Placeholder for alignment
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}
