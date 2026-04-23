package com.example.easymoney.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.R
import com.example.easymoney.ui.components.AppTextField
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
    val scheme = MaterialTheme.colorScheme
    
    // High Contrast color for titles (Deep Navy in Light mode)
    val titleTextColor = if (isDarkMode) scheme.onBackground else Color(0xFF1A1C1E)
    
    val topGradientColor = scheme.primary.copy(alpha = if (isDarkMode) 0.6f else 0.85f)
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to topGradientColor,
            0.15f to topGradientColor,
            0.45f to scheme.primary.copy(alpha = if (isDarkMode) 0.1f else 0.25f),
            0.65f to scheme.background
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // User Avatar Box with Switch Icon
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main Avatar
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displayLarge,
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Small Switch Account Icon (Swap/Sync)
                Surface(
                    onClick = { showAccountSheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 12.dp)
                        .size(44.dp),
                    shape = CircleShape,
                    color = scheme.primary,
                    contentColor = scheme.onPrimary,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Switch Account",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Back Text - GUARANTEED CONTRAST
            Text(
                text = stringResource(id = R.string.login_welcome_back, displayName),
                style = MaterialTheme.typography.headlineMedium,
                color = titleTextColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Password Input
            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(id = R.string.login_password_placeholder),
                isPassword = true,
                showPassword = showPassword,
                onTogglePassword = { showPassword = !showPassword }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = scheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = { onLoginClick(password) },
                enabled = !isLoading && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = scheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.welcome_login), 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Other Account Option
            TextButton(
                onClick = onLoginWithOtherAccountClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.login_other_account),
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Using the new separated BottomSheet file
        if (showAccountSheet) {
            AccountManagementBottomSheet(
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
