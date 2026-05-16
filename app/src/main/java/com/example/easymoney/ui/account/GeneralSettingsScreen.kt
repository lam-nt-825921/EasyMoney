package com.example.easymoney.ui.account

import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary

import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.example.easymoney.R

@Composable
fun GeneralSettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    appNotificationsEnabled: Boolean,
    onAppNotificationsChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val currentLanguage = AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
        .ifBlank { configuration.locales[0]?.language ?: "vi" }
        .substringBefore("-")
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsGroup(title = stringResource(id = R.string.settings_group_app)) {
            SettingsItem(
                title = stringResource(id = R.string.settings_item_language),
                subtitle = languageLabel(currentLanguage),
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF2F4F7))
            SettingsItem(
                title = stringResource(id = R.string.settings_item_dark_mode),
                subtitle = stringResource(
                    id = if (isDarkTheme) R.string.settings_item_dark_mode_on
                    else R.string.settings_item_dark_mode_off
                ),
                icon = Icons.Default.DarkMode,
                control = {
                    Switch(checked = isDarkTheme, onCheckedChange = onDarkThemeChange)
                }
            )
        }

        SettingsGroup(title = stringResource(id = R.string.settings_group_notifications)) {
            SettingsItem(
                title = stringResource(id = R.string.settings_item_notify),
                subtitle = stringResource(id = R.string.settings_item_notify_sub),
                icon = Icons.Default.Notifications,
                control = {
                    Switch(
                        checked = appNotificationsEnabled,
                        onCheckedChange = onAppNotificationsChange
                    )
                }
            )
        }

        SettingsGroup(title = stringResource(id = R.string.settings_group_info)) {
            SettingsItem(
                title = stringResource(id = R.string.settings_item_terms),
                icon = Icons.Default.Description,
                onClick = onTermsClick
            )
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF2F4F7))
            SettingsItem(
                title = stringResource(id = R.string.settings_item_version),
                subtitle = stringResource(id = R.string.settings_item_version_value),
                icon = Icons.Default.Info
            )
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = currentLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { languageTag ->
                showLanguageDialog = false
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageTag)
                )
            }
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.settings_language_dialog_title)) },
        text = {
            Column {
                LanguageOption(
                    label = stringResource(id = R.string.settings_language_vi),
                    selected = currentLanguage == "vi",
                    onClick = { onLanguageSelected("vi") }
                )
                LanguageOption(
                    label = stringResource(id = R.string.settings_language_en),
                    selected = currentLanguage == "en",
                    onClick = { onLanguageSelected("en") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.dialog_button_close))
            }
        }
    )
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun languageLabel(language: String): String = when (language) {
    "en" -> stringResource(id = R.string.settings_language_en)
    else -> stringResource(id = R.string.settings_language_vi)
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
