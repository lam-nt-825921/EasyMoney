package com.example.easymoney.ui.security

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary

@Composable
fun SecuritySettingsScreen(
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SecurityGroup(title = stringResource(R.string.security_group_auth)) {
            SecurityItem(
                title = stringResource(R.string.security_biometric_auth),
                subtitle = if (uiState.isBiometricSupported) stringResource(R.string.security_item_bio_subtitle_supported) else stringResource(R.string.security_item_bio_subtitle_unsupported),
                icon = Icons.Default.Fingerprint,
                control = {
                    Switch(
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) },
                        enabled = uiState.isBiometricSupported
                    )
                }
            )
        }

        SecurityGroup(title = stringResource(R.string.security_group_password)) {
            SecurityItem(
                title = stringResource(R.string.security_change_password),
                subtitle = stringResource(R.string.security_item_pwd_subtitle),
                icon = Icons.Default.Lock,
                onClick = onChangePassword
            )
        }

        SecurityGroup(title = stringResource(R.string.security_group_device)) {
            SecurityItem(
                title = stringResource(R.string.security_item_device_title),
                subtitle = stringResource(R.string.security_item_device_subtitle),
                icon = Icons.Default.Devices
            )
        }
    }
}

@Composable
private fun SecurityGroup(
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
private fun SecurityItem(
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
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}
