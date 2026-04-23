package com.example.easymoney.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.LocalDarkMode

data class QuickLoginAccount(
    val id: String,
    val displayName: String,
    val handle: String
)

@Composable
fun QuickLoginScreen1(
    displayName: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit,
    onLoginClick: (String) -> Unit,
    onSwitchAccountClick: () -> Unit,
    onLoginWithOtherAccountClick: () -> Unit = {},
    otherAccounts: List<QuickLoginAccount> = emptyList(),
    onSelectAccount: (QuickLoginAccount) -> Unit = {},
    onDeleteAccount: (QuickLoginAccount) -> Unit = {},
) {
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showAccountSheet by rememberSaveable { mutableStateOf(false) }

    val isDarkMode = LocalDarkMode.current
    val topGradientColor = MaterialTheme.colorScheme.primary
        .copy(alpha = if (isDarkMode) 0.78f else 0.95f)
        .compositeOver(MaterialTheme.colorScheme.background)
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to topGradientColor,
            0.04f to topGradientColor,
            0.12f to MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkMode) 0.42f else 0.60f),
            0.22f to MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkMode) 0.16f else 0.24f),
            0.32f to MaterialTheme.colorScheme.background
        )
    )
    val topTextColor = MaterialTheme.colorScheme.onPrimary
    val welcomeTextColor = if (isDarkMode) topTextColor else MaterialTheme.colorScheme.primary
    val switchAccountColor = Color(0xFF1B8F3A)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = topGradientColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer to fix "shifted upwards" issue
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .padding(top = 26.dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(id = R.string.login_welcome_back, displayName),
                style = MaterialTheme.typography.headlineSmall,
                color = welcomeTextColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )

            if (otherAccounts.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.login_switch_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = switchAccountColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable {
                            onSwitchAccountClick()
                            showAccountSheet = true
                        }
                )
            }

            val fieldColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
            } else {
                Color(0xFFD4DCE4)
            }

            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = stringResource(id = R.string.login_password_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (showPassword) {
                    androidx.compose.ui.text.input.VisualTransformation.None
                } else {
                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
                    .height(60.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldColor,
                    unfocusedContainerColor = fieldColor,
                    disabledContainerColor = fieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Button(
                onClick = { onLoginClick(password) },
                enabled = !isLoading && password.isNotBlank(),
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(id = R.string.welcome_login), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onLoginWithOtherAccountClick,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.login_other_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            )
        }

        if (showAccountSheet && otherAccounts.isNotEmpty()) {
            QuickLoginAccountSheet(
                accounts = otherAccounts,
                onDismiss = { showAccountSheet = false },
                onSelectAccount = {
                    showAccountSheet = false
                    onSelectAccount(it)
                },
                onDeleteAccount = onDeleteAccount
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickLoginAccountSheet(
    accounts: List<QuickLoginAccount>,
    onDismiss: () -> Unit,
    onSelectAccount: (QuickLoginAccount) -> Unit,
    onDeleteAccount: (QuickLoginAccount) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.login_select_account),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            accounts.forEachIndexed { index, account ->
                QuickLoginAccountRow(
                    account = account,
                    onClick = { onSelectAccount(account) },
                    onDelete = { onDeleteAccount(account) }
                )
                if (index != accounts.lastIndex) {
                    HorizontalDivider()
                }
            }

            Box(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun QuickLoginAccountRow(
    account: QuickLoginAccount,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = account.displayName.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(text = account.displayName, fontWeight = FontWeight.SemiBold)
            Text(
                text = account.handle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete account"
            )
        }
    }
}

@Preview(name = "QuickLogin1 - Light", showBackground = true)
@Composable
private fun QuickLoginScreen1LightPreview() {
    EasyMoneyTheme {
        QuickLoginScreen1(
            displayName = "Trần Minh Quân",
            onBackClick = {},
            onLoginClick = {},
            onSwitchAccountClick = {}
        )
    }
}
