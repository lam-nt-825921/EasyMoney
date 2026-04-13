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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSwitchAccountClick: () -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "EasyMoney",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = topTextColor
                )
                Box(modifier = Modifier.size(48.dp))
            }

            Box(
                modifier = Modifier
                    .padding(top = 26.dp)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Text(
                text = "Chào mừng trở lại,\n$displayName.",
                style = MaterialTheme.typography.headlineMedium,
                color = welcomeTextColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (otherAccounts.isNotEmpty()) {
                Text(
                    text = "Chuy\u1EC3n t\u00E0i kho\u1EA3n",
                    style = MaterialTheme.typography.bodyLarge,
                    color = switchAccountColor,
                    modifier = Modifier
                        .padding(top = 8.dp)
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
                placeholder = { Text(text = "M\u1EADt kh\u1EA9u") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
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
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldColor,
                    unfocusedContainerColor = fieldColor,
                    disabledContainerColor = fieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .padding(top = 18.dp)
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "\u0110\u0103ng Nh\u1EADp", style = MaterialTheme.typography.titleMedium)
            }

            if (otherAccounts.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onSwitchAccountClick()
                        showAccountSheet = true
                    },
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = "Ho\u1EB7c \u0111\u0103ng nh\u1EADp v\u1EDBi t\u00E0i kho\u1EA3n kh\u00E1c",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
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
                text = "Ch\u1ECDn t\u00E0i kho\u1EA3n",
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

@Preview(name = "QuickLogin1 - Light", showBackground = true, widthDp = 392, heightDp = 824)
@Composable
private fun QuickLoginScreen1LightPreview() {
    EasyMoneyTheme {
        QuickLoginScreen1(
            displayName = "Tr\u1EA7n Minh Qu\u00E2n",
            onBackClick = {},
            onLoginClick = {},
            onSwitchAccountClick = {},
            otherAccounts = listOf(
                QuickLoginAccount(id = "a1", displayName = "Nguy\u1EC5n V\u0103n A", handle = "@van_a_01"),
                QuickLoginAccount(id = "b1", displayName = "Tr\u1EA7n Th\u1ECB B", handle = "@thi_b_banking")
            )
        )
    }
}

@Preview(name = "QuickLogin1 - Dark", showBackground = true, widthDp = 392, heightDp = 824)
@Composable
private fun QuickLoginScreen1DarkPreview() {
    EasyMoneyTheme(darkTheme = true) {
        QuickLoginScreen1(
            displayName = "Tr\u1EA7n Minh Qu\u00E2n",
            onBackClick = {},
            onLoginClick = {},
            onSwitchAccountClick = {},
            otherAccounts = listOf(
                QuickLoginAccount(id = "a1", displayName = "Nguy\u1EC5n V\u0103n A", handle = "@van_a_01"),
                QuickLoginAccount(id = "b1", displayName = "Tr\u1EA7n Th\u1ECB B", handle = "@thi_b_banking")
            )
        )
    }
}



