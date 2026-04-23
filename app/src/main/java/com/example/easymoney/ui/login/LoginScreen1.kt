package com.example.easymoney.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.components.AppTextField
import com.example.easymoney.ui.theme.EasyMoneyTheme
import com.example.easymoney.ui.theme.LocalDarkMode

@Composable
fun LoginScreen1(
    onLoginClick: (String, String, Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberAccount by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val isDarkMode = LocalDarkMode.current
    val scheme = MaterialTheme.colorScheme
    
    val topGradientColor = scheme.primary.copy(alpha = if (isDarkMode) 0.7f else 0.9f)
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to topGradientColor,
            0.08f to topGradientColor,
            0.30f to scheme.primary.copy(alpha = if (isDarkMode) 0.15f else 0.35f),
            0.50f to scheme.background
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(130.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // High contrast title
                Text(
                    text = stringResource(id = R.string.welcome_login),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isDarkMode) scheme.onBackground else scheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(id = R.string.login_account_label),
                        style = MaterialTheme.typography.titleLarge,
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    AppTextField(
                        value = account,
                        onValueChange = { account = it },
                        placeholder = stringResource(id = R.string.login_account_placeholder)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(id = R.string.login_password_label),
                        style = MaterialTheme.typography.titleLarge,
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    AppTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = stringResource(id = R.string.login_password_placeholder),
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = scheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = rememberAccount,
                        onCheckedChange = { rememberAccount = it },
                        colors = CheckboxDefaults.colors(checkedColor = scheme.primary)
                    )
                    Text(
                        text = stringResource(id = R.string.login_remember_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp, top = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onLoginClick(account, password, rememberAccount) },
                    enabled = !isLoading && account.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
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

                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.welcome_register),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                }
            }
        }
    }
}

@Preview(name = "Login1 - Light", showSystemUi = true)
@Composable
private fun LoginScreen1LightPreview() {
    EasyMoneyTheme {
        LoginScreen1(onLoginClick = { _, _, _ -> }, onRegisterClick = {})
    }
}
